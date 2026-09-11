package com.example.web_crawler.parser;

import com.example.web_crawler.fetch.FetchResult;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlPageParserTest {
    private final HtmlPageParser parser = new HtmlPageParser();

    @Test
    void parsesTitleTextAndLinks() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <!doctype html>
            <html>
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
    }

    @Test
    void ignoresElementsWithoutHref() {
        URI uri = URI.create("https://example.com/page");

        String html = """
            <html>
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
    }
}
