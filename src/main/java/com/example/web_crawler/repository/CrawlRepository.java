package com.example.web_crawler.repository;

import com.example.web_crawler.model.Crawl;

import java.util.Optional;

public interface CrawlRepository {
    Crawl save(Crawl crawl);

    Optional<Crawl> findById(long id);
}
