package com.example.web_crawler.ratelimit;

public class RateLimitInterruptedException
    extends RuntimeException {

    public RateLimitInterruptedException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
