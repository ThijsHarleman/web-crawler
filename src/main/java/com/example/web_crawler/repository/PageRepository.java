package com.example.web_crawler.repository;

import com.example.web_crawler.model.Page;

import java.util.Optional;

public interface PageRepository {
    Page save(Page page);

    Optional<Page> findById(long id);

    boolean existsByCrawlIdAndUri(long crawlId, String uri);
}
