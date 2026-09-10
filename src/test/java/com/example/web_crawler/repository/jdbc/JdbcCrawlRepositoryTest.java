package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcCrawlRepositoryTest {
    @Autowired
    private JdbcCrawlRepository crawlRepository;

    @Test
    void savesAndFindsCrawl() {
        Crawl crawl = new Crawl(
            0L,
            URI.create("https://example.com"),
            3,
            Duration.ofMinutes(5),
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        Crawl saved = crawlRepository.save(crawl);

        assertTrue(saved.getId() > 0);
        assertEquals(
            URI.create("https://example.com"),
            saved.getStartUrl()
        );
        assertEquals(3, saved.getMaxDepth());
        assertEquals(
            Duration.ofMinutes(5),
            saved.getMaxDuration()
        );
        assertEquals(
            CrawlStatus.NOT_STARTED,
            saved.getStatus()
        );

        Optional<Crawl> found = crawlRepository.findById(saved.getId());

        assertTrue(found.isPresent());

        Crawl loaded = found.get();

        assertEquals(saved.getId(), loaded.getId());
        assertEquals(
            saved.getStartUrl(),
            loaded.getStartUrl()
        );
        assertEquals(
            saved.getMaxDepth(),
            loaded.getMaxDepth()
        );
        assertEquals(
            saved.getMaxDuration(),
            loaded.getMaxDuration()
        );
        assertEquals(
            saved.getStatus(),
            loaded.getStatus()
        );
    }

    @Test
    void updatesExistingCrawl() {
        Crawl crawl = new Crawl(
            0L,
            URI.create("https://example.com"),
            3,
            Duration.ofMinutes(5),
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        Crawl saved = crawlRepository.save(crawl);

        Instant startedAt = Instant.now();

        saved.start(startedAt);

        crawlRepository.save(saved);

        Crawl loaded = crawlRepository.findById(saved.getId()).orElseThrow();

        assertEquals(
            CrawlStatus.RUNNING,
            loaded.getStatus()
        );

        assertEquals(
            startedAt,
            loaded.getStartedAt()
        );
    }
}
