package com.backend.backend.scanner.web;

import com.backend.backend.security.SsrfGuard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fetches a handful of pages from one site, starting at a URL, following same-site links. */
public final class Crawler {

    private static final int MAX_PAGES = 100;
    private static final int MAX_BODY_BYTES = 500_000;   // don't hold huge pages in memory
    private static final Pattern LINK_PATTERN =
            Pattern.compile("href=[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)   // we check each redirect target ourselves
            .build();

    /** Crawls starting at startUrl, staying on the same host, up to MAX_PAGES pages. */
    public List<CrawledPage> crawl(String startUrl) throws Exception {
        URI start = URI.create(startUrl);
        String host = start.getHost();

        List<CrawledPage> pages = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(startUrl);

        while (!queue.isEmpty() && pages.size() < MAX_PAGES) {
            String url = queue.poll();
            if (!visited.add(url)) continue;

            CrawledPage page = fetch(url);
            if (page == null) continue;   // blocked by SsrfGuard, or failed to fetch
            pages.add(page);

            if (page.statusCode() >= 200 && page.statusCode() < 300) {
                for (String link : extractLinks(page.body(), url)) {
                    URI linkUri;
                    try {
                        linkUri = URI.create(link);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    if (host.equals(linkUri.getHost()) && !visited.contains(link) && pages.size() + queue.size() < MAX_PAGES) {
                        queue.add(link);
                    }
                }
            }
        }
        return pages;
    }

    /** Fetches one URL, checking it with SsrfGuard first. Returns null if blocked or unreachable. */
    private CrawledPage fetch(String url) {
        try {
            SsrfGuard.check(url);
        } catch (SsrfGuard.SsrfBlockedException e) {
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "VulnScan/1.0 (authorized security scan)")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body != null && body.length() > MAX_BODY_BYTES) {
                body = body.substring(0, MAX_BODY_BYTES);
            }
            return new CrawledPage(url, response.statusCode(), response.headers(), body);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> extractLinks(String html, String baseUrl) {
        List<String> links = new ArrayList<>();
        if (html == null) return links;
        Matcher m = LINK_PATTERN.matcher(html);
        while (m.find()) {
            String href = m.group(1);
            try {
                links.add(URI.create(baseUrl).resolve(href).toString());
            } catch (IllegalArgumentException ignored) {
                // malformed href, skip it
            }
        }
        return links;
    }
}