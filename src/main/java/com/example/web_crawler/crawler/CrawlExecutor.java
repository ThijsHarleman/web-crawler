package com.example.web_crawler.crawler;

import com.example.web_crawler.model.Crawl;

public interface CrawlExecutor {
    void execute(
        Crawl crawl
    );
}
