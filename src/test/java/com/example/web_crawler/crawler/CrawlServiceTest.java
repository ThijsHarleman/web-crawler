package com.example.web_crawler.crawler;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.model.Page;
import com.example.web_crawler.parser.HtmlPageParser;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageRepository;
import com.example.web_crawler.robots.RobotsPolicyFetcher;
import com.example.web_crawler.url.UrlNormalizer;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrawlServiceTest {
    private static final URI START_URI = URI.create("https://example.com/");

    private static final Instant START_TIME = Instant.parse(
        "2026-01-01T00:00:00Z"
    );

    @Test
    void startsAndCompletesCrawl() {
        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        InMemoryPageRepository pageRepository = new InMemoryPageRepository();

        PageFetcher pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "text/html",
            "<html><body>Hello</body></html>"
        );

        RobotsPolicyFetcher robotsPolicyFetcher = new RobotsPolicyFetcher(
            pageFetcher
        );

        Clock clock = Clock.fixed(
            START_TIME,
            java.time.ZoneOffset.UTC
        );

        BreadthFirstCrawlerFactory crawlerFactory =
            new BreadthFirstCrawlerFactory(
                crawlRepository,
                pageRepository,
                robotsPolicyFetcher,
                pageFetcher,
                new HtmlPageParser(),
                new UrlNormalizer(),
                clock
            );
        
        CrawlExecutor crawlExecutor = new SynchronousCrawlExecutor(
            crawlerFactory,
            crawlRepository
        );

        CrawlService crawlService = new CrawlService(
            crawlRepository,
            crawlExecutor
        );

        Crawl result = crawlService.start(
            START_URI,
            0,
            Duration.ofMinutes(1)
        );

        assertNotNull(result);

        assertEquals(
            CrawlStatus.COMPLETED,
            result.getStatus()
        );

        assertEquals(
            3,
            crawlRepository.getSaveCount()
        );

        assertEquals(
            2,
            pageRepository.getSavedPages().size()
        );
    }

    @Test
    void returnsThePersistedCrawl() {
        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        InMemoryPageRepository pageRepository = new InMemoryPageRepository();

        PageFetcher pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "text/html",
            "<html><body>Hello</body></html>"
        );

        Clock clock =  Clock.fixed(
            START_TIME,
            java.time.ZoneOffset.UTC
        );

        RobotsPolicyFetcher robotsPolicyFetcher = new RobotsPolicyFetcher(
            pageFetcher
        );

        BreadthFirstCrawlerFactory crawlerFactory =
            new BreadthFirstCrawlerFactory(
                crawlRepository,
                pageRepository,
                robotsPolicyFetcher,
                pageFetcher,
                new HtmlPageParser(),
                new UrlNormalizer(),
                clock
            );
        
        CrawlExecutor crawlExecutor = new SynchronousCrawlExecutor(
            crawlerFactory,
            crawlRepository
        );

        CrawlService crawlService = new CrawlService(
            crawlRepository,
            crawlExecutor
        );

        Crawl result = crawlService.start(
            START_URI,
            0,
            Duration.ofMinutes(1)
        );

        assertSame(
            crawlRepository.getLastSaved(),
            result
        );
    }

    @Test
    void persistsStoppedCrawl() {
        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        InMemoryPageRepository pageRepository = new InMemoryPageRepository();

        MutableClock clock = new MutableClock(START_TIME);

        PageFetcher pageFetcher = uri -> {
            clock.advance(
                Duration.ofSeconds(10)
            );

            return new FetchResult(
                uri,
                200,
                "text/html",
                """
                <a href="/child">
                    Child
                </a>
                """
            );
        };

        RobotsPolicyFetcher robotsPolicyFetcher = new RobotsPolicyFetcher(
            pageFetcher
        );

        BreadthFirstCrawlerFactory crawlerFactory =
            new BreadthFirstCrawlerFactory(
                crawlRepository,
                pageRepository,
                robotsPolicyFetcher,
                pageFetcher,
                new HtmlPageParser(),
                new UrlNormalizer(),
                clock
            );
        
        CrawlExecutor crawlExecutor = new SynchronousCrawlExecutor(
            crawlerFactory,
            crawlRepository
        );

        CrawlService crawlService = new CrawlService(
            crawlRepository,
            crawlExecutor
        );

        Crawl result = crawlService.start(
            START_URI,
            10,
            Duration.ofSeconds(5)
        );

        assertEquals(
            CrawlStatus.STOPPED,
            result.getStatus()
        );

        assertEquals(
            CrawlStatus.STOPPED,
            crawlRepository
                .getLastSaved()
                .getStatus()
        );
    }

    @Test
    void persistsFailedCrawlWhenUnexpectedExceptionOccurs() {
        InMemoryCrawlRepository crawlRepository = new InMemoryCrawlRepository();

        PageRepository pageRepository = new PageRepository() {
            @Override
            public Page save(Page page) {
                throw new IllegalStateException(
                    "Database failure"
                );
            }

            @Override
            public Optional<Page> findById(
                long id
            ) {
                return Optional.empty();
            }

            @Override
            public boolean existsByCrawlIdAndUri(
                long crawlId,
                URI uri
            ) {
                return false;
            }

            @Override
            public List<Page> findByCrawlId(long crawlId) {
                return List.of();
            }
        };

        PageFetcher pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "text/html",
            "<html><body>Hello</body></html>"
        );

        Clock clock = Clock.fixed(
            START_TIME,
            java.time.ZoneOffset.UTC
        );

        RobotsPolicyFetcher robotsPolicyFetcher = new RobotsPolicyFetcher(
            pageFetcher
        );

        BreadthFirstCrawlerFactory crawlerFactory =
            new BreadthFirstCrawlerFactory(
                crawlRepository,
                pageRepository,
                robotsPolicyFetcher,
                pageFetcher,
                new HtmlPageParser(),
                new UrlNormalizer(),
                clock
            );
        
        CrawlExecutor crawlExecutor = new SynchronousCrawlExecutor(
            crawlerFactory,
            crawlRepository
        );

        CrawlService crawlService = new CrawlService(
            crawlRepository,
            crawlExecutor
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> crawlService.start(
                START_URI,
                0,
                Duration.ofMinutes(1)
            )
        );

        assertEquals(
            "Database failure",
            exception.getMessage()
        );

        Crawl savedCrawl = crawlRepository.getLastSaved();

        assertNotNull(savedCrawl);

        assertEquals(
            CrawlStatus.FAILED,
            savedCrawl.getStatus()
        );
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

        private int getSaveCount() {
            return savedCrawls.size();
        }

        private Crawl getLastSaved() {
            return savedCrawls.stream()
                .reduce(
                    (first, second) -> second
                )
                .orElseThrow();
        }
    }

    private static class InMemoryPageRepository implements PageRepository {
        private final List<Page> savedPages = new ArrayList<>();

        @Override
        public Page save(Page page) {
            savedPages.add(page);
            return page;
        }

        @Override
        public Optional<Page> findById(long id) {
            return savedPages.stream()
                .filter(page ->
                    page.getId() == id
                )
                .findFirst();
        }

        @Override
        public boolean existsByCrawlIdAndUri(
            long crawlId,
            URI uri
        ) {
            return savedPages.stream()
                .anyMatch(page ->
                    page.getCrawlId() == crawlId
                        && page.getUri()
                            .toString()
                            .equals(uri)
                );
        }

        @Override
        public List<Page> findByCrawlId(long crawlId) {
            return savedPages.stream()
                .filter(page -> page.getCrawlId() == crawlId)
                .toList();
        }

        private List<Page> getSavedPages() {
            return savedPages;
        }
    }

    private static class MutableClock extends Clock {
        private Instant currentInstant;

        private MutableClock(
            Instant currentInstant
        ) {
            this.currentInstant = currentInstant;
        }

        @Override
        public java.time.ZoneId getZone() {
            return java.time.ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(
            java.time.ZoneId zone
        ) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        private void advance(
            Duration duration
        ) {
            currentInstant = currentInstant.plus(duration);
        }
    }

    private static class SynchronousCrawlExecutor implements CrawlExecutor {
        private final CrawlerFactory crawlerFactory;
        private final CrawlRepository crawlRepository;

        SynchronousCrawlExecutor(
            CrawlerFactory crawlerFactory,
            CrawlRepository crawlRepository
        ) {
            this.crawlerFactory =
                crawlerFactory;

            this.crawlRepository =
                crawlRepository;
        }

        @Override
        public void execute(Crawl crawl) {
            try {
                crawlerFactory
                    .create()
                    .crawl(crawl);
            } finally {
                crawlRepository.save(crawl);
            }
        }
    }
}
