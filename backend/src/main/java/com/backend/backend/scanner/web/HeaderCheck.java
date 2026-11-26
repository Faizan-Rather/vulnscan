package com.backend.backend.scanner.web;

import com.backend.backend.finding.Finding;
import com.backend.backend.finding.Severity;
import com.backend.backend.scanner.ScanContext;
import com.backend.backend.scanner.ScanType;
import com.backend.backend.scanner.SecurityCheck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Flags missing security-relevant HTTP response headers. */
@Component
public class HeaderCheck implements SecurityCheck {

    private static final String OWASP_CATEGORY = "A05:2021 Security Misconfiguration";

    @Override
    public String id() {
        return "WEB-HEADERS-001";
    }

    @Override
    public ScanType type() {
        return ScanType.WEB;
    }

    @Override
    public List<Finding> run(ScanContext ctx) throws Exception {
        List<CrawledPage> pages = new Crawler().crawl(ctx.targetUrl());
        List<Finding> findings = new ArrayList<>();

        for (CrawledPage page : pages) {
            if (page.statusCode() < 200 || page.statusCode() >= 300) continue;

            checkMissingHeader(page, "content-security-policy", Severity.MEDIUM,
                    "Missing Content-Security-Policy header",
                    "Without a CSP, the browser has no restriction on which scripts, styles, or resources can run, making cross-site scripting attacks more damaging.",
                    "Add a Content-Security-Policy header that restricts script and resource sources to trusted origins.",
                    findings);

            checkMissingHeader(page, "strict-transport-security", Severity.MEDIUM,
                    "Missing Strict-Transport-Security (HSTS) header",
                    "Without HSTS, browsers may connect over plain HTTP, exposing traffic to downgrade or man-in-the-middle attacks.",
                    "Add a Strict-Transport-Security header, e.g. max-age=63072000; includeSubDomains.",
                    findings);

            checkMissingHeader(page, "x-frame-options", Severity.LOW,
                    "Missing X-Frame-Options header",
                    "Without this header, the page can be embedded in an iframe on another site, enabling clickjacking attacks.",
                    "Add X-Frame-Options: DENY or SAMEORIGIN.",
                    findings);

            checkMissingHeader(page, "x-content-type-options", Severity.LOW,
                    "Missing X-Content-Type-Options header",
                    "Without this header, some browsers may try to guess a file's type, which can lead to content being executed unexpectedly.",
                    "Add X-Content-Type-Options: nosniff.",
                    findings);
        }
        return findings;
    }

    private void checkMissingHeader(CrawledPage page, String headerName, Severity severity,
                                    String title, String description, String remediation,
                                    List<Finding> findings) {
        if (page.headers().firstValue(headerName).isPresent()) return;

        findings.add(Finding.builder(id(), severity, title)
                .description(description)
                .file(page.url(), null)
                .remediation(remediation)
                .owasp(OWASP_CATEGORY)
                .build());
    }
}