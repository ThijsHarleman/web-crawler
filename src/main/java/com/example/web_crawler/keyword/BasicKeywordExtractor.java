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

    private static final Set<String> STOP_WORDS = Set.of(
        "the",
        "and",
        "for",
        "are",
        "but",
        "not",
        "you",
        "all",
        "any",
        "can",
        "had",
        "her",
        "was",
        "one",
        "our",
        "out",
        "has",
        "have",
        "with",
        "this",
        "that",
        "from",
        "they",
        "their",
        "there",
        "which",
        "would",
        "could",
        "should",
        "about",
        "into",
        "more",
        "when",
        "where",
        "what",
        "will",
        "your",
        "than",
        "then",
        "them",
        "these",
        "those",
        "also"
    );

    @Override
    public Map<String, Integer> extract(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

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

            if (STOP_WORDS.contains(word)) {
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
                Map.Entry
                    .<String, Integer>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry.comparingByKey())
            )
            .limit(MAX_KEYWORDS)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (first, second) -> first,
                    LinkedHashMap::new
            )
        );
    }
}
