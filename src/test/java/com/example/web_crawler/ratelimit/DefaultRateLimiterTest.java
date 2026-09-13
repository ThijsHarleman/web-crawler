package com.example.web_crawler.ratelimit;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRateLimiterTest {
    @Test
    void firstRequestDoesNotWait() {
        RateLimiter rateLimiter = new DefaultRateLimiter(
            Duration.ofMillis(100)
        );

        URI uri = URI.create("https://example.com/page");

        long start = System.nanoTime();

        rateLimiter.waitIfNecessary(uri);

        long elapsedMillis = Duration.ofNanos(
            System.nanoTime() - start
        ).toMillis();

        assertTrue(
            elapsedMillis < 50,
            "First request should not wait"
        );
    }

    @Test
    void waitsBetweenRequestsToSameOrigin() {
        RateLimiter rateLimiter = new DefaultRateLimiter(
            Duration.ofMillis(100)
        );

        URI uri = URI.create("https://example.com/page");

        rateLimiter.waitIfNecessary(uri);

        long start = System.nanoTime();

        rateLimiter.waitIfNecessary(uri);

        long elapsedMillis = Duration.ofNanos(
            System.nanoTime() - start
        ).toMillis();

        assertTrue(
            elapsedMillis >= 80,
            "Second request should respect the minimum delay"
        );
    }

    @Test
    void differentOriginsHaveIndependentDelays() {
        RateLimiter rateLimiter = new DefaultRateLimiter(
            Duration.ofMillis(100)
        );

        URI firstUri = URI.create("https://example.com/page");

        URI secondUri = URI.create("https://other.example.com/page");

        rateLimiter.waitIfNecessary(firstUri);

        long start = System.nanoTime();

        rateLimiter.waitIfNecessary(secondUri);

        long elapsedMillis = Duration.ofNanos(
            System.nanoTime() - start
        ).toMillis();

        assertTrue(
            elapsedMillis < 50,
            "Different origins should have independent delays"
        );
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new DefaultRateLimiter(
                Duration.ofMillis(-1)
            )
        );
    }

    @Test
    void preservesInterruptStatus() {
        RateLimiter rateLimiter = new DefaultRateLimiter(
            Duration.ofSeconds(1)
        );

        URI uri = URI.create("https://example.com/page");

        rateLimiter.waitIfNecessary(uri);

        Thread.currentThread().interrupt();

        try {
            assertThrows(
                RateLimitInterruptedException.class,
                () -> rateLimiter.waitIfNecessary(uri)
            );

            assertTrue(
                Thread.currentThread().isInterrupted()
            );

        } finally {
            Thread.interrupted();
        }
    }
}
