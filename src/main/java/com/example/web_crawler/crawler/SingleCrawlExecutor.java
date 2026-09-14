package com.example.web_crawler.crawler;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.repository.CrawlRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleCrawlExecutor
    implements CrawlExecutor, AutoCloseable {

    private final ExecutorService executorService;
    private final CrawlerFactory crawlerFactory;
    private final CrawlRepository crawlRepository;

    public SingleCrawlExecutor(
        CrawlerFactory crawlerFactory,
        CrawlRepository crawlRepository
    ) {
        this.crawlerFactory = crawlerFactory;

        this.crawlRepository = crawlRepository;

        this.executorService =  Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(Crawl crawl) {
        executorService.submit(
            () -> run(crawl)
        );
    }

    private void run(Crawl crawl) {
        try {
            BreadthFirstCrawler crawler = crawlerFactory.create();

            crawler.crawl(crawl);
        } finally {
            crawlRepository.save(crawl);
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
