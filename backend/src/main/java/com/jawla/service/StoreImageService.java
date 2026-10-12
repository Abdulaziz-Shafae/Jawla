package com.jawla.service;

import com.jawla.api.ApiException;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;

@Service
public class StoreImageService {
    private static final Pattern META = Pattern.compile("<meta\\b[^>]{0,2000}>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR = Pattern.compile("([\\w:-]+)\\s*=\\s*([\"'])(.*?)\\2", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();

    public Map<String, String> findStoreImage(String websiteUrl, String instagramUrl) {
        String image = "";
        String source = "";
        if (websiteUrl != null && !websiteUrl.isBlank()) {
            image = findPublicPreview(websiteUrl, false);
            if (!image.isBlank()) source = "website";
        }
        if (image.isBlank() && instagramUrl != null && !instagramUrl.isBlank()) {
            image = findPublicPreview(instagramUrl, true);
            if (!image.isBlank()) source = "instagram";
        }
        return Map.of("imageUrl", image, "source", source, "message", image.isBlank() ? "No public image found. You can provide an image link or keep the default icon." : "Review this preview before saving; external images may change.");
    }

    private String findPublicPreview(String url, boolean instagram) {
        URI uri = validateWebsiteUrl(url, instagram);
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).header("Accept", "text/html").header("User-Agent", "Mozilla/5.0").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200 || !response.headers().firstValue("Content-Type").orElse("").toLowerCase().contains("text/html")) return "";
            String html = response.body();
            if (html.length() > 500000) html = html.substring(0, 500000);
            Matcher matcher = META.matcher(html);
            while (matcher.find()) {
                Map<String,String> attrs = new HashMap<>();
                Matcher a = ATTR.matcher(matcher.group());
                while (a.find()) attrs.put(a.group(1).toLowerCase(), a.group(3));
                String property = attrs.getOrDefault("property", attrs.getOrDefault("name", "")).toLowerCase();
                if (!Set.of("og:image", "og:image:url", "twitter:image").contains(property)) continue;
                String value = attrs.getOrDefault("content", "").replace("&amp;", "&");
                try {
                    URI picture = uri.resolve(value);
                    if ("https".equalsIgnoreCase(picture.getScheme()) && picture.getHost() != null && picture.getUserInfo() == null && picture.getPort() == -1 && !isUnsafeHost(picture.getHost())) return picture.toString();
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return "";
    }

    private URI validateWebsiteUrl(String url, boolean instagram) {
        if (url == null || url.length() > 2048) throw new ApiException("Invalid website URL");
        try {
            URI uri = URI.create(url.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null || uri.getPort() != -1 || isUnsafeHost(uri.getHost())) throw new ApiException("A public HTTPS website URL is required");
            if (instagram && !Set.of("www.instagram.com", "instagram.com").contains(uri.getHost().toLowerCase())) throw new ApiException("Invalid Instagram URL");
            return uri;
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid website URL");
        }
    }

    private boolean isUnsafeHost(String host) {
        try {
            if (host.equalsIgnoreCase("localhost") || host.endsWith(".local") || host.endsWith(".internal") || host.matches("[0-9.]+") || host.contains(":")) return true;
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress() || addr.isMulticastAddress()) return true;
                byte[] ip = addr.getAddress();
                if (ip.length == 4) {
                    int a = ip[0] & 255, b = ip[1] & 255;
                    if (a == 0 || a == 10 || a == 127 || a >= 224 || a == 169 && b == 254 || a == 172 && b >= 16 && b <= 31 || a == 192 && b == 168 || a == 100 && b >= 64 && b <= 127) return true;
                } else if ((ip[0] & 254) == 252) return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
