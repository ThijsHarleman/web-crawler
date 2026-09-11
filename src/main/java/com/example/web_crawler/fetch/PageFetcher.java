package com.example.web_crawler.fetch;

import java.net.URI;

public interface PageFetcher {
    FetchResult fetch(URI uri);
}
