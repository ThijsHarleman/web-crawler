package com.example.web_crawler.config;

import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.fetch.RateLimitedPageFetcher;
import com.example.web_crawler.ratelimit.DefaultRateLimiter;
import com.example.web_crawler.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CrawlerConfigurationTest {
    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private PageFetcher pageFetcher;

    @Test
    void createsRateLimiter() {
        assertNotNull(rateLimiter);

        assertInstanceOf(
            DefaultRateLimiter.class,
            rateLimiter
        );
    }

    @Test
    void createsRateLimitedPageFetcher() {
        assertNotNull(pageFetcher);

        assertInstanceOf(
            RateLimitedPageFetcher.class,
            pageFetcher
        );
    }
}