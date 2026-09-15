package com.example.web_crawler.keyword;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicKeywordExtractorTest {
    private final StopWordProvider stopWordProvider = language -> Set.of(
        "the",
        "and",
        "is"
    );

    private final BasicKeywordExtractor extractor = new BasicKeywordExtractor(
        stopWordProvider
    );

    @Test
    void extractsWordFrequencies() {
        String text = """
            Java is great. Java is powerful.
            Spring is also great.
            """;

        Map<String, Integer> result = extractor.extract(text);

        assertEquals(2, result.get("java"));
        assertEquals(2, result.get("great"));
        assertEquals(1, result.get("powerful"));
        assertEquals(1, result.get("spring"));
    }

    @Test
    void ignoresStopWords() {
        String text = "the is and";

        Map<String, Integer> result = extractor.extract(text);

        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresShortWords() {
        String text = "a an is to be it java web";

        Map<String, Integer> result = extractor.extract(text);

        assertEquals(
            Map.of("java", 1, "web", 1),
            result
        );
    }

    @Test
    void handlesPunctuationAndCase() {
        String text = "Java, JAVA! java? Spring.";

        Map<String, Integer> result = extractor.extract(text);

        assertEquals(3, result.get("java"));
        assertEquals(1, result.get("spring"));
    }

    @Test
    void handlesBlankText() {
        assertTrue(
            extractor.extract("").isEmpty()
        );

        assertTrue(
            extractor.extract("   ").isEmpty()
        );
    }

    @Test
    void handlesNullText() {
        assertTrue(
            extractor.extract(null).isEmpty()
        );
    }

    @Test
    void limitsResultsToMostFrequentKeywords() {
        StringBuilder text = new StringBuilder();

        for (int i = 1; i <= 150; i++) {
            text.append("word")
                .append(i)
                .append(" ");
        }

        text.append("popular ".repeat(200));

        Map<String, Integer> result = extractor.extract(text.toString());

        assertEquals(100, result.size());

        assertEquals(
            200,
            result.get("popular")
        );
    }

    @Test
    void ignoresNumbers() {
        Map<String, Integer> result = extractor.extract(
            "2025 2025 2024 123 artificial intelligence"
        );

        assertFalse(result.containsKey("2025"));
        assertFalse(result.containsKey("2024"));
        assertFalse(result.containsKey("123"));

        assertEquals(
            1,
            result.get("artificial")
        );

        assertEquals(
            1,
            result.get("intelligence")
        );
    }

    @Test
    void keepsWordsThatContainNumbers() {
        Map<String, Integer> result = extractor.extract(
            "gpt4 gpt4 html5 ipv6"
        );

        assertEquals(2, result.get("gpt4"));
        assertEquals(1, result.get("html5"));
        assertEquals(1, result.get("ipv6"));
    }
}
