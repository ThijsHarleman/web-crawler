package com.example.web_crawler.fetch;

import com.example.web_crawler.ratelimit.RateLimiter;

import java.net.URI;

public class RateLimitedPageFetcher
    implements PageFetcher {

    private final PageFetcher delegate;
    private final RateLimiter rateLimiter;

    public RateLimitedPageFetcher(
        PageFetcher delegate,
        RateLimiter rateLimiter
    ) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public FetchResult fetch(URI uri) {
        rateLimiter.waitIfNecessary(uri);

        return delegate.fetch(uri);
    }
}
