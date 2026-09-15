package com.example.web_crawler.keyword;

public interface KeywordAnalysisService {
    void analyze(long pageId, String text, String language);
}
