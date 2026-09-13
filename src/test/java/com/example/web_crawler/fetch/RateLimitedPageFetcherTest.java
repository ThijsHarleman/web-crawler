package com.example.web_crawler.fetch;

import com.example.web_crawler.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitedPageFetcherTest {
    @Test
    void appliesRateLimiterBeforeFetching() {
        AtomicBoolean rateLimiterCalled =
            new AtomicBoolean(false);

        RateLimiter rateLimiter = uri -> {
            rateLimiterCalled.set(true);
        };

        PageFetcher delegate = uri -> {
            assertTrue(
                rateLimiterCalled.get(),
                "Rate limiter should run before the delegate"
            );

            return new FetchResult(
                uri,
                200,
                "text/html",
                "<html></html>"
            );
        };

        RateLimitedPageFetcher fetcher =
            new RateLimitedPageFetcher(
                delegate,
                rateLimiter
            );

        URI uri = URI.create("https://example.com/page");

        FetchResult result = fetcher.fetch(uri);

        assertTrue(
            rateLimiterCalled.get()
        );

        assertEquals(
            200,
            result.statusCode()
        );
    }
}
