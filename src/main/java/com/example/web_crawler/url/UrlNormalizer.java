package com.example.web_crawler.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class UrlNormalizer {
    public URI normalize(URI baseUri, String link) {
        if (link == null || link.isBlank()) {
            return null;
        }

        try {
            URI resolved = baseUri.resolve(link.trim());

            String scheme = resolved.getScheme();

            if (scheme == null ||
                (
                    !scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))
                ) {
                return null;
            }

            return canonicalize(resolved);

        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private URI canonicalize(URI uri) {
        try {
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);

            String host = uri.getHost();

            if (host != null) {
                host = host.toLowerCase(Locale.ROOT);
            }

            int port = uri.getPort();

            if (isDefaultPort(scheme, port)) {
                port = -1;
            }

            return new URI(
                scheme,
                uri.getUserInfo(),
                host,
                port,
                uri.getPath(),
                uri.getQuery(),
                null
            );

        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private boolean isDefaultPort(
        String scheme,
        int port
    ) {
        return (scheme.equals("http") && port == 80)
            || (scheme.equals("https") && port == 443);
    }
}
