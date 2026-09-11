package com.example.web_crawler.fetch;

import java.net.URI;

public record FetchResult(
    URI uri,
    int statusCode,
    String contentType,
    String body
) {
}
