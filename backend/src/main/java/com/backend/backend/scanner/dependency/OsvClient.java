package com.backend.backend.scanner.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Thin client for the free OSV.dev vulnerability database. */
@Component
public class OsvClient {

    private final RestClient client;
    private final String url;

    public OsvClient(@Value("${vulnscan.osv-url}") String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(15_000);
        this.client = RestClient.builder().requestFactory(factory).build();
        this.url = url;
    }

    /** Returns the raw OSV vulnerability records affecting this exact package version. */
    public List<JsonNode> query(Dependency dep) {
        Map<String, Object> body = Map.of(
                "version", dep.version(),
                "package", Map.of("name", dep.name(), "ecosystem", dep.ecosystem()));

        JsonNode response = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        List<JsonNode> vulns = new ArrayList<>();
        if (response != null && response.has("vulns")) {
            response.get("vulns").forEach(vulns::add);
        }
        return vulns;
    }
}