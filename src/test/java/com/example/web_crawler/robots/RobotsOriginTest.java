package com.example.web_crawler.robots;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotsOriginTest {
    @Test
    void usesDefaultHttpPort() {
        RobotsOrigin origin = RobotsOrigin.from(
            URI.create("http://example.com/page")
        );

        assertEquals(
            new RobotsOrigin(
                "http",
                "example.com",
                80
            ),
            origin
        );
    }

    @Test
    void usesDefaultHttpsPort() {
        RobotsOrigin origin = RobotsOrigin.from(
            URI.create("https://example.com/page")
        );

        assertEquals(
            new RobotsOrigin(
                "https",
                "example.com",
                443
            ),
            origin
        );
    }

    @Test
    void preservesExplicitPort() {
        RobotsOrigin origin = RobotsOrigin.from(
            URI.create("https://example.com:8443/page")
        );

        assertEquals(
            new RobotsOrigin(
                "https",
                "example.com",
                8443
            ),
            origin
        );
    }

    @Test
    void normalizesSchemeAndHostCase() {
        RobotsOrigin origin = RobotsOrigin.from(
            URI.create("HTTPS://EXAMPLE.COM/page")
        );

        assertEquals(
            new RobotsOrigin(
                "https",
                "example.com",
                443
            ),
            origin
        );
    }
}
