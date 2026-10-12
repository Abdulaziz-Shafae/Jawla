package com.jawla.service;

import com.jawla.api.ApiException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleMapsService {
    private static final Set<String> MAP_HOSTS = Set.of("google.com", "www.google.com", "maps.google.com", "maps.app.goo.gl", "goo.gl");
    private static final Pattern PLACE_COORDINATES = Pattern.compile("!3d(-?\\d{1,3}(?:\\.\\d+)?)!4d(-?\\d{1,3}(?:\\.\\d+)?)");
    private static final Pattern VIEW_COORDINATES = Pattern.compile("@(-?\\d{1,3}(?:\\.\\d+)?),(-?\\d{1,3}(?:\\.\\d+)?)");
    private static final Pattern COORDINATE_PAIR = Pattern.compile("^\\s*(-?\\d{1,3}(?:\\.\\d+)?),\\s*(-?\\d{1,3}(?:\\.\\d+)?)\\s*$");
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();

    public Map<String, Object> resolveGoogleMapsLocation(String mapsUrl) {
        URI uri = validateGoogleMapsUrl(mapsUrl);
        double[] coordinates = extractCoordinates(uri);
        for (int redirect = 0; coordinates == null && redirect < 4; redirect++) {
            if (!isShortLink(uri)) break;
            URI next = resolveShortLink(uri);
            if (next == null) break;
            uri = validateGoogleMapsUrl(next.toString());
            coordinates = extractCoordinates(uri);
        }
        if (coordinates == null) {
            throw new ApiException("Could not find coordinates in this Google Maps link. Select the store location manually on the map.");
        }
        return Map.of("latitude", coordinates[0], "longitude", coordinates[1], "mapsUrl", mapsUrl);
    }

    private URI validateGoogleMapsUrl(String mapsUrl) {
        if (mapsUrl == null || mapsUrl.isBlank() || mapsUrl.length() > 2048) throw new ApiException("Enter a valid Google Maps URL");
        try {
            URI uri = URI.create(mapsUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1 || uri.getHost() == null || !MAP_HOSTS.contains(uri.getHost().toLowerCase())) {
                throw new ApiException("Only HTTPS Google Maps links are supported");
            }
            if ("goo.gl".equalsIgnoreCase(uri.getHost()) && !uri.getPath().startsWith("/maps/")) throw new ApiException("Invalid Google Maps short link");
            return uri;
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid Google Maps URL");
        }
    }

    private boolean isShortLink(URI uri) {
        String host = uri.getHost().toLowerCase();
        return "maps.app.goo.gl".equals(host) || "goo.gl".equals(host);
    }

    private URI resolveShortLink(URI uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8)).GET().header("User-Agent", "Mozilla/5.0").build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location").orElse("");
                if (location.isBlank()) return null;
                URI target = uri.resolve(location);
                validateGoogleMapsUrl(target.toString());
                return target;
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Google Maps lookup interrupted");
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new ApiException("Could not resolve the short Google Maps link. Select a location manually.");
        }
    }

    private double[] extractCoordinates(URI uri) {
        String raw = uri.toString();
        Matcher place = PLACE_COORDINATES.matcher(raw);
        if (place.find()) return checkedCoordinates(place.group(1), place.group(2));
        String query = uri.getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                int separator = parameter.indexOf('=');
                if (separator < 1) continue;
                String name = URLDecoder.decode(parameter.substring(0, separator), StandardCharsets.UTF_8);
                if (!Set.of("q", "query", "ll", "destination", "daddr", "center").contains(name)) continue;
                String value = URLDecoder.decode(parameter.substring(separator + 1), StandardCharsets.UTF_8);
                Matcher pair = COORDINATE_PAIR.matcher(value);
                if (pair.matches()) return checkedCoordinates(pair.group(1), pair.group(2));
            }
        }
        Matcher view = VIEW_COORDINATES.matcher(raw);
        if (view.find()) return checkedCoordinates(view.group(1), view.group(2));
        String path = uri.getPath();
        if (path != null) {
            Matcher pair = Pattern.compile("/(?:place|search)/(-?\\d{1,3}(?:\\.\\d+)?),(-?\\d{1,3}(?:\\.\\d+)?)(?:/|$)").matcher(path);
            if (pair.find()) return checkedCoordinates(pair.group(1), pair.group(2));
        }
        return null;
    }

    private double[] checkedCoordinates(String latitudeText, String longitudeText) {
        double latitude = Double.parseDouble(latitudeText);
        double longitude = Double.parseDouble(longitudeText);
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new ApiException("Invalid coordinates in the Google Maps link");
        }
        return new double[]{latitude, longitude};
    }
}
