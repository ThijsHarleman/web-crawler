package com.example.web_crawler.crawler;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.repository.CrawlRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;

@Service
public class CrawlService {
    private final CrawlRepository crawlRepository;
    private final CrawlExecutor crawlExecutor;

    public CrawlService(
        CrawlRepository crawlRepository,
        CrawlExecutor crawlExecutor
    ) {
        this.crawlRepository = crawlRepository;

        this.crawlExecutor = crawlExecutor;
    }

    public Crawl start(
        URI startUrl,
        int maxDepth,
        Duration maxDuration
    ) {
        Crawl crawl = new Crawl(
            0,
            startUrl,
            maxDepth,
            maxDuration,
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        Crawl savedCrawl = crawlRepository.save(crawl);

        crawlExecutor.execute(
            savedCrawl
        );

        return savedCrawl;
    }
}
