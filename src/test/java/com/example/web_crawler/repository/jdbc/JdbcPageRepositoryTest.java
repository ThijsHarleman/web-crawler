package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcPageRepositoryTest {
    @Autowired
    private JdbcPageRepository pageRepository;

    @Test
    void savesAndFindsPage() {
        Instant discoveredAt = Instant.now();

        Page page = new Page(
            0L,
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

        Page saved = pageRepository.save(page);

        assertTrue(saved.getId() > 0);
        assertEquals(1L, saved.getCrawlId());
        assertEquals(
            URI.create("https://example.com"),
            saved.getUri()
        );
        assertEquals(0, saved.getDepth());
        assertEquals(
            PageStatus.DISCOVERED,
            saved.getStatus()
        );
        assertEquals(discoveredAt, saved.getDiscoveredAt());

        Optional<Page> found = pageRepository.findById(saved.getId());

        assertTrue(found.isPresent());

        Page loaded = found.get();

        assertEquals(saved.getId(), loaded.getId());
        assertEquals(saved.getCrawlId(), loaded.getCrawlId());
        assertEquals(saved.getUri(), loaded.getUri());
        assertEquals(saved.getDepth(), loaded.getDepth());
        assertEquals(saved.getStatus(), loaded.getStatus());
        assertEquals(
            saved.getDiscoveredAt(),
            loaded.getDiscoveredAt()
        );
    }

    @Test
    void updatesExistingPage() {
        Page page = new Page(
            0L,
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

        Page saved = pageRepository.save(page);

        Instant crawledAt = Instant.now();

        saved.markCrawled(
            200,
            "Example Domain",
            crawledAt
        );

        pageRepository.save(saved);

        Page loaded = pageRepository.findById(saved.getId()).orElseThrow();

        assertEquals(
            PageStatus.CRAWLED,
            loaded.getStatus()
        );
        assertEquals(
            200,
            loaded.getHttpStatusCode()
        );
        assertEquals(
            "Example Domain",
            loaded.getTitle()
        );
        assertEquals(
            crawledAt,
            loaded.getCrawledAt()
        );
        assertEquals(
            null,
            loaded.getErrorMessage()
        );
    }

    @Test
    void detectsExistingPageForCrawl() {
        Page page = new Page(
            0L,
            1L,
            URI.create("https://example.com/about"),
            1,
            PageStatus.DISCOVERED,
            null,
            null,
            Instant.now(),
            null,
            null
        );

        Page saved = pageRepository.save(page);

        assertTrue(
            pageRepository.existsByCrawlIdAndUri(
                saved.getCrawlId(),
                saved.getUri().toString()
            )
        );
    }

    @Test
    void doesNotFindPageForDifferentCrawl() {
        Page page = new Page(
            0L,
            1L,
            URI.create("https://example.com/about"),
            1,
            PageStatus.DISCOVERED,
            null,
            null,
            Instant.now(),
            null,
            null
        );

        Page saved = pageRepository.save(page);

        assertFalse(
            pageRepository.existsByCrawlIdAndUri(
                999L,
                saved.getUri().toString()
            )
        );
    }
}
