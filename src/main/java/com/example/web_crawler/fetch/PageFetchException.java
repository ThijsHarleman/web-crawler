package com.example.web_crawler.fetch;

public class PageFetchException extends RuntimeException {
    public PageFetchException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
