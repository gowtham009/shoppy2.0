package com.gkshoppy.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SerpApiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${serpapi.key:}")
    private String serpApiKey;

    // Calls SerpAPI Google Shopping endpoint and returns raw JSON string
    public String searchShopping(String query) throws IOException, InterruptedException {
        if (serpApiKey == null || serpApiKey.isBlank()) {
            throw new IllegalStateException("SERP API key not configured (set environment variable SERPAPI_KEY or property serpapi.key)");
        }

        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String uri = String.format("https://serpapi.com/search?engine=google_shopping&q=%s&api_key=%s", encoded, URLEncoder.encode(serpApiKey, StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .header("Accept", "application/json")
            .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new IOException("SerpAPI request failed: HTTP " + resp.statusCode() + " - " + resp.body());
    }
}
