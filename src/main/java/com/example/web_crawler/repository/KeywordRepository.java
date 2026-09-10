package com.example.web_crawler.repository;

import com.example.web_crawler.model.Keyword;

import java.util.Optional;

public interface KeywordRepository {
    Keyword save(Keyword keyword);

    Optional<Keyword> findByWord(String word);

    Optional<Keyword> findById(long id);
}
