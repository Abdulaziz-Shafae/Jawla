package com.jawla.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jawla.api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeoapifyService {
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeoapifyService(@Value("${GEOAPIFY_API_KEY:}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public List<Map<String, Object>> searchFurnitureStoreLocations(String name, String city) {
        if (apiKey.isBlank()) throw new ApiException("Geoapify API key is not configured");
        if (name == null || name.isBlank() || name.length() > 150) throw new ApiException("Enter a store name to search");
        if (city != null && city.length() > 100) throw new ApiException("City is too long");
        String query = name.trim() + (city == null || city.isBlank() ? ", Saudi Arabia" : ", " + city.trim() + ", Saudi Arabia");
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.geoapify.com/v1/geocode/search?text=" + encoded + "&filter=countrycode:sa&format=json&limit=8&lang=en&apiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) throw new ApiException("Geoapify search failed (HTTP " + response.statusCode() + ")");
            JsonNode results = objectMapper.readTree(response.body()).path("results");
            List<Map<String, Object>> matches = new ArrayList<>();
            if (!results.isArray()) return matches;
            for (JsonNode item : results) {
                if (!item.path("lat").isNumber() || !item.path("lon").isNumber()) continue;
                String foundName = item.path("name").asText("");
                String address = item.path("formatted").asText("");
                String resultType = item.path("result_type").asText("");
                double confidence = item.path("rank").path("confidence").asDouble(0);
                // A city-only geocoding match is NOT the store location.
                if (foundName.isBlank() || !("amenity".equals(resultType) || "building".equals(resultType) || "address".equals(resultType) || "street".equals(resultType) || "unknown".equals(resultType))) continue;
                if (!similarStoreNames(name, foundName)) continue;
                Map<String, Object> candidate = new LinkedHashMap<>();
                candidate.put("name", foundName);
                candidate.put("address", address);
                candidate.put("latitude", item.path("lat").asDouble());
                candidate.put("longitude", item.path("lon").asDouble());
                candidate.put("confidence", confidence);
                candidate.put("source", "Geoapify");
                matches.add(candidate);
            }
            return matches;
        } catch (ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Geoapify lookup interrupted");
        } catch (IOException e) {
            throw new ApiException("Geoapify search unavailable. Try again later");
        }
    }

    private boolean similarStoreNames(String expected, String actual) {
        String a = expected.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");
        String b = actual.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");
        return a.length() >= 3 && b.length() >= 3 && (a.contains(b) || b.contains(a));
    }
}
