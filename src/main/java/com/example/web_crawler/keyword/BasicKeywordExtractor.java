package com.example.web_crawler.keyword;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BasicKeywordExtractor implements KeywordExtractor {
    private static final int MIN_WORD_LENGTH = 3;
    private static final int MAX_KEYWORDS = 100;

    private final StopWordProvider stopWordProvider;

    public BasicKeywordExtractor(
        StopWordProvider stopWordProvider
    ) {
        this.stopWordProvider = stopWordProvider;
    }

@Override
    public Map<String, Integer> extract(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

        Set<String> stopWords = stopWordProvider.getStopWords("en");

        Map<String, Integer> frequencies = new HashMap<>();

        String[] words = text
            .toLowerCase(Locale.ROOT)
            .split("[^\\p{L}\\p{N}]+");

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (word.length() < MIN_WORD_LENGTH) {
                continue;
            }

            if (isNumber(word)) {
                continue;
            }

            if (stopWords.contains(word)) {
                continue;
            }

            frequencies.merge(
                word,
                1,
                Integer::sum
            );
        }

        return frequencies.entrySet()
            .stream()
            .sorted(
                Map.Entry.<String, Integer>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry::getKey)
            )
            .limit(MAX_KEYWORDS)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    LinkedHashMap::new
                )
            );
    }

    private boolean isNumber(String word) {
        return word.codePoints()
            .allMatch(Character::isDigit);
    }
}
