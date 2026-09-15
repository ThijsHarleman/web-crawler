package com.example.web_crawler.parser;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.keyword.PageLanguageDetector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HtmlPageParser {
    private final PageLanguageDetector pageLanguageDetector;

    public HtmlPageParser(
        PageLanguageDetector pageLanguageDetector
    ) {
        this.pageLanguageDetector = pageLanguageDetector;
    }

    public ParsedPage parse(FetchResult fetchResult) {
        Document document = Jsoup.parse(
            fetchResult.body(),
            fetchResult.uri().toString()
        );

        String title = document.title();

        String text = document.body() != null
            ? document.body().text()
            : "";

        List<String> links = document
            .select("a[href]")
            .stream()
            .map(element -> element.attr("href"))
            .toList();

        String language = pageLanguageDetector
            .detect(document)
            .orElse("en");

        return new ParsedPage(
            fetchResult.uri(),
            title,
            text,
            links,
            language
        );
    }
}
