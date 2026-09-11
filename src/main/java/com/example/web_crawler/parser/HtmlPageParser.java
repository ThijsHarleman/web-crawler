package com.example.web_crawler.parser;

import com.example.web_crawler.fetch.FetchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class HtmlPageParser {
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

        return new ParsedPage(
            fetchResult.uri(),
            title,
            text,
            links
        );
    }
}
