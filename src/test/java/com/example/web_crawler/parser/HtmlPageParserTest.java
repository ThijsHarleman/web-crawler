package com.example.web_crawler.parser;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.keyword.HtmlPageLanguageDetector;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlPageParserTest {
    private final HtmlPageParser parser = new HtmlPageParser(
        new HtmlPageLanguageDetector()
    );

    @Test
    void parsesTitleTextLinksAndLanguage() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <!doctype html>
            <html lang="en">
            <head>
                <title>Example page</title>
            </head>
            <body>
                <h1>Hello world</h1>
                <p>This is an educational crawler.</p>

                <a href="/about">About</a>
                <a href="https://example.com/contact">Contact</a>
                <a href="/about">About again</a>
            </body>
            </html>
            """;

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            html
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals(uri, result.uri());
        assertEquals("Example page", result.title());

        assertEquals(
            "Hello world This is an educational crawler. About Contact About again",
            result.text()
        );

        assertEquals(
            List.of(
                "/about",
                "https://example.com/contact",
                "/about"
            ),
            result.links()
        );

        assertEquals("en", result.language());
    }

    @Test
    void detectsRegionalLanguageCode() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <!doctype html>
            <html lang="de-DE">
            <head>
                <title>Deutsche Seite</title>
            </head>
            <body>
                <p>Das ist eine deutsche Seite.</p>
            </body>
            </html>
            """;

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            html
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals("de", result.language());
    }

    @Test
    void detectsLanguageCodeWithUnderscore() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <html lang="pt_BR">
            <body>
                <p>Esta é uma página em português.</p>
            </body>
            </html>
            """;

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            html
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals("pt", result.language());
    }

    @Test
    void fallsBackToEnglishWhenLanguageIsMissing() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <html>
            <head>
                <title>No language</title>
            </head>
            <body>
                <p>No language was specified.</p>
            </body>
            </html>
            """;

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            html
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals("en", result.language());
    }

    @Test
    void returnsEmptyTitleAndTextWhenDocumentHasNoBody() {
        URI uri = URI.create("https://example.com/page");

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            "<title>Only title</title>"
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals("Only title", result.title());
        assertEquals("", result.text());
        assertEquals(List.of(), result.links());

        assertEquals("en", result.language());
    }

    @Test
    void ignoresElementsWithoutHref() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <html lang="en">
            <body>
                <a>No href</a>
                <a href="/valid">Valid</a>
                <div href="/not-a-link">Not a link</div>
            </body>
            </html>
            """;

        FetchResult fetchResult = new FetchResult(
            uri,
            200,
            "text/html",
            html
        );

        ParsedPage result = parser.parse(fetchResult);

        assertEquals(
            List.of("/valid"),
            result.links()
        );

        assertEquals("en", result.language());
    }
}
