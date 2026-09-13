package com.example.web_crawler.ratelimit;

import java.net.URI;

public interface RateLimiter {
    void waitIfNecessary(URI uri);
}
