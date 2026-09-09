package com.example.web_crawler.model;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrawlTest {
    @Test
    void crawlStartsSuccessfully() {
        Crawl crawl = new Crawl(
            1L,
            URI.create("https://example.com"),
            3,
            Duration.ofMinutes(5),
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        Instant startedAt = Instant.now();

        crawl.start(startedAt);

        assertEquals(CrawlStatus.RUNNING, crawl.getStatus());
        assertEquals(startedAt, crawl.getStartedAt());
        assertNull(crawl.getFinishedAt());
    }

    @Test
    void crawlCompletesSuccessfully() {
        Crawl crawl = new Crawl(
            1L,
            URI.create("https://example.com"),
            3,
            Duration.ofMinutes(5),
            CrawlStatus.RUNNING,
            Instant.now(),
            null
        );

        Instant finishedAt = Instant.now();

        crawl.complete(finishedAt);

        assertEquals(CrawlStatus.COMPLETED, crawl.getStatus());
        assertEquals(finishedAt, crawl.getFinishedAt());
    }
}
