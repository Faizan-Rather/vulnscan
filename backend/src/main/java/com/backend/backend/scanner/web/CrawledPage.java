package com.backend.backend.scanner.web;

import java.net.http.HttpHeaders;

/** One fetched page: the URL, status code, headers, and body (truncated for safety). */
public record CrawledPage(String url, int statusCode, HttpHeaders headers, String body) {}