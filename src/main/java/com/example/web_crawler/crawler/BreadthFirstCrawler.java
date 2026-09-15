package com.example.web_crawler.crawler;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.fetch.PageFetchException;
import com.example.web_crawler.keyword.KeywordAnalysisService;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlTarget;
import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageStatus;
import com.example.web_crawler.parser.HtmlPageParser;
import com.example.web_crawler.parser.ParsedPage;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageRepository;
import com.example.web_crawler.robots.RobotsPolicyProvider;
import com.example.web_crawler.url.UrlNormalizer;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BreadthFirstCrawler {
    private final CrawlRepository crawlRepository;
    private final PageRepository pageRepository;
    private final RobotsPolicyProvider robotsPolicyProvider;
    private final PageFetcher pageFetcher;
    private final HtmlPageParser pageParser;
    private final UrlNormalizer urlNormalizer;
    private final KeywordAnalysisService keywordAnalysisService;
    private final Clock clock;
    private static final Logger logger = LoggerFactory.getLogger(
        BreadthFirstCrawler.class
    );

    public BreadthFirstCrawler(
        CrawlRepository crawlRepository,
        PageRepository pageRepository,
        RobotsPolicyProvider robotsPolicyProvider,
        PageFetcher pageFetcher,
        HtmlPageParser pageParser,
        UrlNormalizer urlNormalizer,
        KeywordAnalysisService keywordAnalysisService,
        Clock clock
    ) {
        this.crawlRepository = crawlRepository;
        this.pageRepository = pageRepository;
        this.robotsPolicyProvider = robotsPolicyProvider;
        this.pageFetcher = pageFetcher;
        this.pageParser = pageParser;
        this.urlNormalizer = urlNormalizer;
        this.keywordAnalysisService = keywordAnalysisService;
        this.clock = clock;
    }

    public void crawl(Crawl crawl) {
        Instant startedAt = clock.instant();

        crawl.start(startedAt);

        crawlRepository.save(crawl);

        logger.info(
            "Crawl {} started: {}",
            crawl.getId(),
            crawl.getStartUrl()
        );

        Instant deadline = startedAt.plus(
            crawl.getMaxDuration()
        );

        try {
            Queue<CrawlTarget> queue = new ArrayDeque<>();

            Set<URI> visited = new HashSet<>();

            queue.add(
                new CrawlTarget(
                    crawl.getStartUrl(),
                    0
                )
            );

            while (!queue.isEmpty()) {
                if (!clock.instant().isBefore(deadline)) {
                    crawl.stop(
                        clock.instant()
                    );

                    logger.info(
                        "Crawl {} stopped because the maximum duration was reached",
                        crawl.getId()
                    );
                    return;
                }

                CrawlTarget target = queue.remove();

                if (!visited.add(target.uri())) {
                    continue;
                }

                logger.info(
                    "Crawl {} processing {} at depth {}",
                    crawl.getId(),
                    target.uri(),
                    target.depth()
                );

                Page page = new Page(
                    0,
                    crawl.getId(),
                    target.uri(),
                    target.depth(),
                    PageStatus.DISCOVERED,
                    null,
                    null,
                    clock.instant(),
                    null,
                    null
                );

                Page savedPage = pageRepository.save(page);

                if (target.depth() > crawl.getMaxDepth()) {
                    savedPage.markSkipped();

                    pageRepository.save(savedPage);

                    continue;
                }

                if (!robotsPolicyProvider
                    .getPolicy(target.uri())
                    .isAllowed(target.uri())) {

                    logger.info(
                        "Crawl {} blocked by robots.txt: {}",
                        crawl.getId(),
                        target.uri()
                    );

                    savedPage.markBlockedByRobots();

                    pageRepository.save(savedPage);

                    continue;
                }

                FetchResult fetchResult;

                try {
                    fetchResult = pageFetcher.fetch(target.uri());
                } catch (PageFetchException exception) {
                    logger.warn(
                        "Crawl {} failed to fetch {}: {}",
                        crawl.getId(),
                        target.uri(),
                        exception.getMessage()
                    );

                    savedPage.markFailed(
                        exception.getMessage()
                    );

                    pageRepository.save(savedPage);
                    continue;
                }

                if (!isHtmlContentType(fetchResult.contentType())) {
                    savedPage.markSkipped();
                    pageRepository.save(savedPage);
                    continue;
                }

                ParsedPage parsedPage = pageParser.parse(fetchResult);

                savedPage.markCrawled(
                    fetchResult.statusCode(),
                    parsedPage.title(),
                    clock.instant()
                );

                pageRepository.save(savedPage);

                logger.info(
                    "Starting keyword analysis for page {} ({})",
                    savedPage.getId(),
                    savedPage.getUri()
                );

                keywordAnalysisService.analyze(
                    savedPage.getId(),
                    parsedPage.text(),
                    parsedPage.language()
                );

                logger.info(
                    "Finished keyword analysis for page {} ({})",
                    savedPage.getId(),
                    savedPage.getUri()
                );

                logger.info(
                    "Crawl {} crawled {} (HTTP {})",
                    crawl.getId(),
                    target.uri(),
                    fetchResult.statusCode()
                );

                for (String link : parsedPage.links()) {
                    URI normalizedUri =
                        urlNormalizer.normalize(
                            target.uri(),
                            link
                        );

                    if (normalizedUri == null) {
                        continue;
                    }

                    if (visited.contains(normalizedUri)) {
                        continue;
                    }

                    queue.add(
                        new CrawlTarget(
                            normalizedUri,
                            target.depth() + 1
                        )
                    );
                }
            }

            logger.info(
                "Crawl {} completed",
                crawl.getId()
            );

            crawl.complete(
                clock.instant()
            );
        } catch (RuntimeException exception) {
            crawl.fail(
                clock.instant()
            );

            throw exception;
        }
    }

    private boolean isHtmlContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        String normalized = contentType.toLowerCase(Locale.ROOT);

        return normalized.startsWith("text/html")
            || normalized.startsWith("application/xhtml+xml");
    }
}
