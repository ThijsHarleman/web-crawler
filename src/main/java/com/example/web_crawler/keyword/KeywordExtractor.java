package com.example.web_crawler.keyword;

import java.util.Map;

public interface KeywordExtractor {
    Map<String, Integer> extract(String text);
}
