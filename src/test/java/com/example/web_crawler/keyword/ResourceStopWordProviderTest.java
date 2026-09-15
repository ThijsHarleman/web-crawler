package com.example.web_crawler.keyword;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceStopWordProviderTest {
    private final ResourceStopWordProvider provider = new ResourceStopWordProvider();

    @Test
    void loadsEnglishStopWords() {
        Set<String> stopWords = provider.getStopWords("en");

        assertTrue(stopWords.contains("the"));
        assertTrue(stopWords.contains("and"));
        assertTrue(stopWords.contains("with"));
    }

    @Test
    void loadsUniversalStopWords() {
        Set<String> stopWords = provider.getUniversalStopWords();

        assertTrue(stopWords.contains("the"));
        assertTrue(stopWords.contains("and"));
        assertTrue(stopWords.contains("with"));
    }

    @Test
    void returnsEmptySetForUnknownLanguage() {
        Set<String> stopWords = provider.getStopWords("xx");

        assertTrue(stopWords.isEmpty());
    }
}
