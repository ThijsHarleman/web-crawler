package com.example.web_crawler.model;

public record PageKeyword(
    long pageId,
    Keyword keyword,
    int frequency,
    double score
) {
}
