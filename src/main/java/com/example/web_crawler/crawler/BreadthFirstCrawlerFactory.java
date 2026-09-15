package com.example.web_crawler.crawler;

import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.keyword.KeywordAnalysisService;
import com.example.web_crawler.parser.HtmlPageParser;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageRepository;
import com.example.web_crawler.robots.RobotsPolicyCache;
import com.example.web_crawler.robots.RobotsPolicyFetcher;
import com.example.web_crawler.url.UrlNormalizer;

import java.time.Clock;

public class BreadthFirstCrawlerFactory implements CrawlerFactory {
    private final CrawlRepository crawlRepository;
    private final PageRepository pageRepository;
    private final RobotsPolicyFetcher robotsPolicyFetcher;
    private final PageFetcher pageFetcher;
    private final HtmlPageParser pageParser;
    private final KeywordAnalysisService keywordAnalysisService;
    private final UrlNormalizer urlNormalizer;
    private final Clock clock;

    public BreadthFirstCrawlerFactory(
        CrawlRepository crawlRepository,
        PageRepository pageRepository,
        RobotsPolicyFetcher robotsPolicyFetcher,
        PageFetcher pageFetcher,
        HtmlPageParser pageParser,
        UrlNormalizer urlNormalizer,
        KeywordAnalysisService keywordAnalysisService,
        Clock clock
    ) {
        this.crawlRepository = crawlRepository;
        this.pageRepository = pageRepository;
        this.robotsPolicyFetcher = robotsPolicyFetcher;
        this.pageFetcher = pageFetcher;
        this.pageParser = pageParser;
        this.urlNormalizer = urlNormalizer;
        this.keywordAnalysisService = keywordAnalysisService;
        this.clock = clock;
    }

    public BreadthFirstCrawler create() {
        RobotsPolicyCache robotsPolicyCache = new RobotsPolicyCache(
            robotsPolicyFetcher
        );

        return new BreadthFirstCrawler(
            crawlRepository,
            pageRepository,
            robotsPolicyCache,
            pageFetcher,
            pageParser,
            urlNormalizer,
            keywordAnalysisService,
            clock
        );
    }
}
