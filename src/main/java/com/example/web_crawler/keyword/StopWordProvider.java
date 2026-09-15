package com.example.web_crawler.keyword;

import java.util.Set;

public interface StopWordProvider {
    Set<String> getStopWords(String language);

    Set<String> getUniversalStopWords();
}
