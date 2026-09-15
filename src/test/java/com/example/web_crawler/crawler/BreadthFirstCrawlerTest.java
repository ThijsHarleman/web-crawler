package com.example.web_crawler.crawler;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.fetch.PageFetchException;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.keyword.HtmlPageLanguageDetector;
import com.example.web_crawler.keyword.KeywordAnalysisService;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageStatus;
import com.example.web_crawler.parser.HtmlPageParser;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageRepository;
import com.example.web_crawler.robots.RobotsPolicyProvider;
import com.example.web_crawler.robots.SimpleRobotsPolicy;
import com.example.web_crawler.url.UrlNormalizer;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BreadthFirstCrawlerTest {
    private static final Instant START_TIME =
        Instant.parse("2026-01-01T00:00:00Z");

    private InMemoryCrawlRepository crawlRepository = 
        new InMemoryCrawlRepository();

    private InMemoryPageRepository pageRepository =
        new InMemoryPageRepository();

    private PageFetcher pageFetcher = uri -> new FetchResult(
        uri,
        200,
        "text/html",
        "<html><body>Hello</body></html>"
    );

    private RobotsPolicyProvider robotsPolicyProvider =
        uri -> new SimpleRobotsPolicy(List.of());

    private KeywordAnalysisService keywordAnalysisService = 
        mock(KeywordAnalysisService.class);;

    private final MutableClock clock =
        new MutableClock(START_TIME);

    @Test
    void crawlsPagesInBreadthFirstOrder() {
        URI startUri = URI.create("https://example.com/");

        URI firstChildUri = URI.create("https://example.com/b");

        URI secondChildUri = URI.create("https://example.com/c");

        URI grandchildUri = URI.create("https://example.com/d");

        List<URI> fetchedUris = new ArrayList<>();

        pageFetcher =
            uri -> {
                fetchedUris.add(uri);

                String body;

                if (uri.equals(startUri)) {
                    body = """
                        <a href="/b">B</a>
                        <a href="/c">C</a>
                        """;
                } else if (uri.equals(firstChildUri)) {
                    body = """
                        <a href="/d">D</a>
                        """;
                } else {
                    body = "";
                }

                return htmlResponse(uri, body);
            };

        Crawl crawl = createCrawl(
            startUri,
            2,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            List.of(
                startUri,
                firstChildUri,
                secondChildUri,
                grandchildUri
            ),
            fetchedUris
        );
    }

    @Test
    void doesNotFetchPageBlockedByRobots() {
        URI startUri = URI.create("https://example.com/");

        List<URI> fetchedUris = new ArrayList<>();

        pageFetcher = uri -> {
            fetchedUris.add(uri);

            return htmlResponse(
                uri,
                ""
            );
        };

        robotsPolicyProvider = uri -> new SimpleRobotsPolicy(
            List.of(
                new SimpleRobotsPolicy.Rule(
                    "/",
                    false
                )
            )
        );

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            List.of(),
            fetchedUris
        );

        assertEquals(
            PageStatus.BLOCKED_BY_ROBOTS,
            latestPage(startUri).getStatus()
        );
    }

    @Test
    void doesNotFetchPagesBeyondMaximumDepth() {
        URI startUri =  URI.create("https://example.com/");

        URI childUri = URI.create("https://example.com/child");

        URI grandchildUri = URI.create("https://example.com/grandchild");

        List<URI> fetchedUris = new ArrayList<>();

        pageFetcher = uri -> {
            fetchedUris.add(uri);

            if (uri.equals(startUri)) {
                return htmlResponse(
                    uri,
                    "<a href=\"/child\">Child</a>"
                );
            }

            if (uri.equals(childUri)) {
                return htmlResponse(
                    uri,
                    "<a href=\"/grandchild\">Grandchild</a>"
                );
            }

            return htmlResponse(
                uri,
                ""
            );
        };

        Crawl crawl = createCrawl(
            startUri,
            1,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            List.of(
                startUri,
                childUri
            ),
            fetchedUris
        );

        assertEquals(
            PageStatus.SKIPPED,
            latestPage(grandchildUri).getStatus()
        );
    }

    @Test
    void marksPageAsFailedWhenFetchingFails() {
        URI startUri = URI.create("https://example.com/");

        pageFetcher = uri -> {
            throw new PageFetchException(
                "Connection failed",
                new RuntimeException(
                    "Test failure"
                )
            );
        };

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        Page page = latestPage(startUri);

        assertEquals(
            PageStatus.FAILED,
            page.getStatus()
        );

        assertEquals(
            "Connection failed",
            page.getErrorMessage()
        );
    }

    @Test
    void skipsNonHtmlContent() {
        URI startUri = URI.create("https://example.com/document.pdf");

        pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "application/pdf",
            "fake pdf content"
        );

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            PageStatus.SKIPPED,
            latestPage(startUri).getStatus()
        );
    }

    @Test
    void acceptsXhtmlContent() {
        URI startUri = URI.create("https://example.com/");

        pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "application/xhtml+xml",
            """
            <html>
                <body>
                    Hello
                </body>
            </html>
            """
        );

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            PageStatus.CRAWLED,
            latestPage(startUri).getStatus()
        );
    }

    @Test
    void marksCrawlAsCompletedAfterSuccessfulCrawl() {
        URI startUri = URI.create("https://example.com/");

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            CrawlStatus.COMPLETED,
            crawl.getStatus()
        );

        assertEquals(
            START_TIME,
            crawl.getStartedAt()
        );

        assertEquals(
            START_TIME,
            crawl.getFinishedAt()
        );
    }

    @Test
    void stopsWhenMaximumDurationIsReached() {
        URI startUri = URI.create("https://example.com/");

        URI childUri = URI.create("https://example.com/child");

        List<URI> fetchedUris = new ArrayList<>();

        pageFetcher = uri -> {
            fetchedUris.add(uri);

            clock.advance(
                Duration.ofSeconds(6)
            );

            return htmlResponse(
                uri,
                "<a href=\"/child\">Child</a>"
            );
        };

        Crawl crawl = createCrawl(
            startUri,
            10,
            Duration.ofSeconds(5)
        );

        createCrawler().crawl(crawl);

        assertEquals(
            List.of(startUri),
            fetchedUris
        );

        assertEquals(
            CrawlStatus.STOPPED,
            crawl.getStatus()
        );

        assertEquals(
            START_TIME.plusSeconds(6),
            crawl.getFinishedAt()
        );

        // The child was discovered but never fetched.
        // We deliberately do not expect a Page record for it.
        assertEquals(
            Optional.empty(),
            pageRepository.findLatestByUri(childUri)
        );
    }

    @Test
    void marksCrawlAsFailedWhenUnexpectedExceptionOccurs() {
        URI startUri = URI.create("https://example.com/");

        pageRepository = new InMemoryPageRepository() {
            @Override
            public Page save(Page page) {
                throw new IllegalStateException(
                    "Database failure"
                );
            }
        };

        Crawl crawl = createCrawl(
            startUri,
            0,
            Duration.ofMinutes(1)
        );

        assertThrows(
            IllegalStateException.class,
            () -> createCrawler().crawl(crawl)
        );

        assertEquals(
            CrawlStatus.FAILED,
            crawl.getStatus()
        );
    }

    @Test
    void analyzesSuccessfullyCrawledPage() {
        URI startUri = URI.create("https://example.com/");

        Crawl crawl = new Crawl(
            1L,
            startUri,
            0,
            Duration.ofSeconds(60),
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        pageFetcher = uri -> new FetchResult(
            uri,
            200,
            "text/html",
            """
            <html>
                <head>
                    <title>Example</title>
                </head>
                <body>
                    Java Java Spring crawler
                </body>
            </html>
            """
        );

        robotsPolicyProvider = uri -> target -> true;

        BreadthFirstCrawler crawler = createCrawler();

        crawler.crawl(crawl);

        verify(keywordAnalysisService).analyze(
            0L,
            "Java Java Spring crawler",
            "en"
        );
    }

    private BreadthFirstCrawler createCrawler() {
        return new BreadthFirstCrawler(
            crawlRepository,
            pageRepository,
            robotsPolicyProvider,
            pageFetcher,
            new HtmlPageParser(new HtmlPageLanguageDetector()),
            new UrlNormalizer(),
            keywordAnalysisService,
            clock
        );
    }

    private Crawl createCrawl(
        URI startUri,
        int maxDepth,
        Duration maxDuration
    ) {
        return new Crawl(
            1L,
            startUri,
            maxDepth,
            maxDuration,
            CrawlStatus.NOT_STARTED,
            null,
            null
        );
    }

    private Page latestPage(URI uri) {
        return pageRepository
            .findLatestByUri(uri)
            .orElseThrow();
    }

    private FetchResult htmlResponse(
        URI uri,
        String body
    ) {
        return new FetchResult(
            uri,
            200,
            "text/html",
            body
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
                .filter(crawl -> crawl.getId() == id)
                .findFirst();
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
                        && page.getUri().toString().equals(uri)
                );
        }

        private Optional<Page> findLatestByUri(
            URI uri
        ) {
            return savedPages.stream()
                .filter(page ->
                    page.getUri().equals(uri)
                )
                .reduce(
                    (first, second) -> second
                );
        }

        @Override
        public List<Page> findByCrawlId(long crawlId) {
            return savedPages.stream()
                .filter(page -> page.getCrawlId() == crawlId)
                .toList();
        }
    }

    private static class MutableClock
        extends Clock {

        private Instant currentInstant;

        private MutableClock(
            Instant currentInstant
        ) {
            this.currentInstant = currentInstant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(
            ZoneId zone
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
}
