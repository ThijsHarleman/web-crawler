package com.example.web_crawler.model;

import java.net.URI;
import java.time.Instant;

public class Page {
    private final long id;
    private final long crawlId;
    private final URI uri;
    private final int depth;

    private PageStatus status;
    private Integer httpStatusCode;
    private String title;
    private Instant discoveredAt;
    private Instant crawledAt;
    private String errorMessage;

    public Page(
        long id,
        long crawlId,
        URI uri,
        int depth,
        PageStatus status,
        Integer httpStatusCode,
        String title,
        Instant discoveredAt,
        Instant crawledAt,
        String errorMessage
    ) {
        this.id = id;
        this.crawlId = crawlId;
        this.uri = uri;
        this.depth = depth;
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        this.title = title;
        this.discoveredAt = discoveredAt;
        this.crawledAt = crawledAt;
        this.errorMessage = errorMessage;
    }

    public long getId() {
        return id;
    }

    public long getCrawlId() {
        return crawlId;
    }

    public URI getUri() {
        return uri;
    }

    public int getDepth() {
        return depth;
    }

    public PageStatus getStatus() {
        return status;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getTitle() {
        return title;
    }

    public Instant getDiscoveredAt() {
        return discoveredAt;
    }

    public Instant getCrawledAt() {
        return crawledAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markCrawled(
        int httpStatusCode,
        String title,
        Instant crawledAt
    ) {
        this.status = PageStatus.CRAWLED;
        this.httpStatusCode = httpStatusCode;
        this.title = title;
        this.crawledAt = crawledAt;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = PageStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markBlockedByRobots() {
        this.status = PageStatus.BLOCKED_BY_ROBOTS;
    }

    public void markSkipped() {
        this.status = PageStatus.SKIPPED;
    }
}
