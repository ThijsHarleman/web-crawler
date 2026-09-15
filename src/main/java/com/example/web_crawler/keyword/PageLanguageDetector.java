package com.example.web_crawler.keyword;

import org.jsoup.nodes.Document;

import java.util.Optional;

public interface PageLanguageDetector {
    Optional<String> detect(Document document);
}
