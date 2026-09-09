package com.example.web_crawler.model;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PageTest {
    @Test
    void pageCanBeMarkedAsCrawled() {
        Instant discoveredAt = Instant.now();

        Page page = new Page(
            1L,
            1L,
            URI.create("https://example.com"),
            0,
            PageStatus.DISCOVERED,
            null,
            null,
            discoveredAt,
            null,
            null
        );

        Instant crawledAt = Instant.now();

        page.markCrawled(
            200,
            "Example",
            crawledAt
        );

        assertEquals(PageStatus.CRAWLED, page.getStatus());
        assertEquals(200, page.getHttpStatusCode());
        assertEquals("Example", page.getTitle());
        assertEquals(crawledAt, page.getCrawledAt());
        assertNull(page.getErrorMessage());
    }

    @Test
    void pageCanBeMarkedAsFailed() {
        Page page = new Page(
            1L,
            1L,
            URI.create("https://example.com"),
            0,
            PageStatus.DISCOVERED,
            null,
            null,
            Instant.now(),
            null,
            null
        );

        page.markFailed("Connection timed out");

        assertEquals(PageStatus.FAILED, page.getStatus());
        assertEquals("Connection timed out", page.getErrorMessage());
    }
}
