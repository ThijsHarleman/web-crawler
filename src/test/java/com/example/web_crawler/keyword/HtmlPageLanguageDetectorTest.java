package com.example.web_crawler.keyword;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlPageLanguageDetectorTest {
    private final HtmlPageLanguageDetector detector = new HtmlPageLanguageDetector();

    @Test
    void detectsLanguageFromHtmlLangAttribute() {
        Document document = Jsoup.parse("""
            <html lang="de">
                <body>
                    <p>Das ist eine deutsche Seite.</p>
                </body>
            </html>
            """);

        Optional<String> result = detector.detect(document);

        assertEquals(
            Optional.of("de"),
            result
        );
    }

    @Test
    void normalizesRegionalLanguageCode() {
        Document document = Jsoup.parse("""
            <html lang="en-US">
                <body>
                    <p>This is an English page.</p>
                </body>
            </html>
            """);

        Optional<String> result = detector.detect(document);

        assertEquals(
            Optional.of("en"),
            result
        );
    }

    @Test
    void normalizesUnderscoreInLanguageCode() {
        Document document = Jsoup.parse("""
            <html lang="pt_BR">
                <body>
                    <p>Esta é uma página.</p>
                </body>
            </html>
            """);

        Optional<String> result = detector.detect(document);

        assertEquals(
            Optional.of("pt"),
            result
        );
    }

    @Test
    void returnsEmptyWhenLanguageIsMissing() {
        Document document = Jsoup.parse("""
            <html>
                <body>
                    <p>No language specified.</p>
                </body>
            </html>
            """);

        Optional<String> result = detector.detect(document);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullDocument() {
        Optional<String> result = detector.detect(null);

        assertTrue(result.isEmpty());
    }
}
