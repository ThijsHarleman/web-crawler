package com.example.web_crawler.parser;

import java.net.URI;
import java.util.List;

public record ParsedPage(
    URI uri,
    String title,
    String text,
    List<String> links
) {
}
