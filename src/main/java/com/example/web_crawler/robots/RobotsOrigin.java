package com.example.web_crawler.robots;

import java.net.URI;
import java.util.Locale;

public record RobotsOrigin(
    String scheme,
    String host,
    int port
) {
    public static RobotsOrigin from(URI uri) {
        return new RobotsOrigin(
            uri.getScheme().toLowerCase(Locale.ROOT),
            uri.getHost().toLowerCase(Locale.ROOT),
            effectivePort(uri)
        );
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }

        return switch (uri.getScheme().toLowerCase(Locale.ROOT)) {
            case "http" -> 80;
            case "https" -> 443;
            default -> -1;
        };
    }
}
