package com.example.web_crawler.model;

import java.net.URI;

public record CrawlTarget (
    URI uri,
    int depth
) {
}
