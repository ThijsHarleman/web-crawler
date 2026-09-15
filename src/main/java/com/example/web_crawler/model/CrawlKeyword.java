package com.example.web_crawler.model;

public record CrawlKeyword(
    Keyword keyword,
    int frequency,
    double score
) {
}
