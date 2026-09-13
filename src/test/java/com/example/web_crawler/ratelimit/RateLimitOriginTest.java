package com.example.web_crawler.ratelimit;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitOriginTest {
    @Test
    void usesDefaultHttpPort() {
        RateLimitOrigin origin = RateLimitOrigin.from(
            URI.create("http://example.com/page")
        );

        assertEquals(
            new RateLimitOrigin(
                "http",
                "example.com",
                80
            ),
            origin
        );
    }

    @Test
    void usesDefaultHttpsPort() {
        RateLimitOrigin origin = RateLimitOrigin.from(
            URI.create("https://example.com/page")
        );

        assertEquals(
            new RateLimitOrigin(
                "https",
                "example.com",
                443
            ),
            origin
        );
    }

    @Test
    void preservesExplicitPort() {
        RateLimitOrigin origin = RateLimitOrigin.from(
            URI.create("https://example.com:8443/page")
        );

        assertEquals(
            new RateLimitOrigin(
                "https",
                "example.com",
                8443
            ),
            origin
        );
    }

    @Test
    void normalizesSchemeAndHostCase() {
        RateLimitOrigin origin = RateLimitOrigin.from(
            URI.create("HTTPS://EXAMPLE.COM/page")
        );

        assertEquals(
            new RateLimitOrigin(
                "https",
                "example.com",
                443
            ),
            origin
        );
    }
}
