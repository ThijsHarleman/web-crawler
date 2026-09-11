package com.example.web_crawler.url;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UrlNormalizerTest {
    private final UrlNormalizer normalizer = new UrlNormalizer();

    @Test
    void resolvesRelativePath() {
        URI base = URI.create(
            "https://example.com/articles/page.html"
        );

        URI result = normalizer.normalize(
            base,
            "../about"
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void resolvesRootRelativePath() {
        URI base = URI.create(
            "https://example.com/articles/page.html"
        );

        URI result = normalizer.normalize(
            base,
            "/contact"
        );

        assertEquals(
            URI.create("https://example.com/contact"),
            result
        );
    }

    @Test
    void preservesAbsoluteUrl() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "https://other.example.com/about"
        );

        assertEquals(
            URI.create("https://other.example.com/about"),
            result
        );
    }

    @Test
    void removesFragment() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "/about#team"
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void preservesQueryParameters() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "/search?q=java&page=2"
        );

        assertEquals(
            URI.create(
                "https://example.com/search?q=java&page=2"
            ),
            result
        );
    }

    @Test
    void preservesTrailingSlash() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "/about/"
        );

        assertEquals(
            URI.create("https://example.com/about/"),
            result
        );
    }

    @Test
    void returnsNullForNullLink() {
        URI base = URI.create(
            "https://example.com/page"
        );

        assertNull(
            normalizer.normalize(base, null)
        );
    }

    @Test
    void returnsNullForBlankLink() {
        URI base = URI.create(
            "https://example.com/page"
        );

        assertNull(
            normalizer.normalize(base, "   ")
        );
    }

    @Test
    void trimsWhitespaceAroundLink() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "  /about  "
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void rejectsMailtoLinks() {
        URI base = URI.create(
            "https://example.com/page"
        );

        assertNull(
            normalizer.normalize(
                base,
                "mailto:test@example.com"
            )
        );
    }

    @Test
    void rejectsJavascriptLinks() {
        URI base = URI.create(
            "https://example.com/page"
        );

        assertNull(
            normalizer.normalize(
                base,
                "javascript:void(0)"
            )
        );
    }

    @Test
    void normalizesSchemeToLowercase() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "HTTPS://example.com/about"
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void normalizesHostToLowercase() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "https://EXAMPLE.COM/about"
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void removesDefaultHttpsPort() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "https://example.com:443/about"
        );

        assertEquals(
            URI.create("https://example.com/about"),
            result
        );
    }

    @Test
    void removesDefaultHttpPort() {
        URI base = URI.create(
            "http://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "http://example.com:80/about"
        );

        assertEquals(
            URI.create("http://example.com/about"),
            result
        );
    }

    @Test
    void preservesNonDefaultPort() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "https://example.com:8443/about"
        );

        assertEquals(
            URI.create("https://example.com:8443/about"),
            result
        );
    }

    @Test
    void preservesPathCase() {
        URI base = URI.create(
            "https://example.com/page"
        );

        URI result = normalizer.normalize(
            base,
            "/About"
        );

        assertEquals(
            URI.create("https://example.com/About"),
            result
        );
    }
}
