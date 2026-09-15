package com.example.web_crawler.crawler;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.repository.CrawlRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleCrawlExecutorTest {
    @Test
    void executesCrawlOnBackgroundThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        CrawlerFactory crawlerFactory = () -> new BreadthFirstCrawler(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ) {
            @Override
            public void crawl(Crawl crawl) {
                latch.countDown();
            }
        };

        try (
            SingleCrawlExecutor executor = new SingleCrawlExecutor(
                crawlerFactory,
                crawlRepository
            )
        ) {
            Crawl crawl = new Crawl(
                1,
                URI.create(
                    "https://example.com/"
                ),
                0,
                Duration.ofMinutes(1),
                CrawlStatus.NOT_STARTED,
                null,
                null
            );

            executor.execute(crawl);

            assertTrue(
                latch.await(
                    2,
                    TimeUnit.SECONDS
                )
            );
        }
    }

    @Test
    void persistsCrawlAfterExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        CrawlerFactory crawlerFactory = () -> new BreadthFirstCrawler(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ) {
            @Override
            public void crawl(Crawl crawl) {
                crawl.complete(
                    Instant.now()
                );

                latch.countDown();
            }
        };

        try (
            SingleCrawlExecutor executor = new SingleCrawlExecutor(
                crawlerFactory,
                crawlRepository
            )
        ) {
            Crawl crawl = new Crawl(
                1,
                URI.create(
                    "https://example.com/"
                ),
                0,
                Duration.ofMinutes(1),
                CrawlStatus.NOT_STARTED,
                null,
                null
            );

            executor.execute(crawl);

            assertTrue(
                latch.await(
                    2,
                    TimeUnit.SECONDS
                )
            );

            assertEquals(
                1,
                crawlRepository.getSaveCount()
            );

            assertEquals(
                CrawlStatus.COMPLETED,
                crawlRepository
                    .getLastSaved()
                    .getStatus()
            );
        }
    }

    private static class InMemoryCrawlRepository implements CrawlRepository {
        private final List<Crawl> savedCrawls = new ArrayList<>();

        @Override
        public Crawl save(Crawl crawl) {
            savedCrawls.add(crawl);
            return crawl;
        }

        @Override
        public Optional<Crawl> findById(long id) {
            return savedCrawls.stream()
                .filter(crawl ->
                    crawl.getId() == id
                )
                .findFirst();
        }

        int getSaveCount() {
            return savedCrawls.size();
        }

        Crawl getLastSaved() {
            return savedCrawls.get(
                savedCrawls.size() - 1
            );
        }
    }
}
