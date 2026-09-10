package com.example.web_crawler.repository;

import com.example.web_crawler.model.PageKeyword;

import java.util.List;

public interface PageKeywordRepository {
    void save(PageKeyword pageKeyword);

    List<PageKeyword> findByPageId(long pageId);
}
