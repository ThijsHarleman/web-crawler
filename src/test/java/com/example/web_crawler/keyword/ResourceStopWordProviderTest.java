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
    void loadsGermanStopWords() {
        Set<String> stopWords =
            provider.getStopWords("de");

        assertTrue(stopWords.contains("der"));
        assertTrue(stopWords.contains("die"));
        assertTrue(stopWords.contains("das"));
    }

    @Test
    void loadsFrenchStopWords() {
        Set<String> stopWords =
            provider.getStopWords("fr");

        assertTrue(stopWords.contains("le"));
        assertTrue(stopWords.contains("la"));
        assertTrue(stopWords.contains("les"));
    }

    @Test
    void loadsDutchStopWords() {
        Set<String> stopWords =
            provider.getStopWords("nl");

        assertTrue(stopWords.contains("de"));
        assertTrue(stopWords.contains("het"));
        assertTrue(stopWords.contains("een"));
    }

    @Test
    void loadsSpanishStopWords() {
        Set<String> stopWords =
            provider.getStopWords("es");

        assertTrue(stopWords.contains("el"));
        assertTrue(stopWords.contains("la"));
        assertTrue(stopWords.contains("los"));
    }

    @Test
    void returnsEmptySetForUnknownLanguage() {
        Set<String> stopWords = provider.getStopWords("xx");

        assertTrue(stopWords.isEmpty());
    }
}
