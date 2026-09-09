package com.example.web_crawler.model;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

public class Crawl {
    private final long id;
    private final URI startUrl;
    private final int maxDepth;
    private final Duration maxDuration;

    private CrawlStatus status;
    private Instant startedAt;
    private Instant finishedAt;

    public Crawl(
        long id,
        URI startUrl,
        int maxDepth,
        Duration maxDuration,
        CrawlStatus status,
        Instant startedAt,
        Instant finishedAt
    ) {
        this.id = id;
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.maxDuration = maxDuration;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public long getId() {
        return id;
    }

    public URI getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public CrawlStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void start(Instant startedAt) {
        this.status = CrawlStatus.RUNNING;
        this.startedAt = startedAt;
    }

    public void complete(Instant finishedAt) {
        this.status = CrawlStatus.COMPLETED;
        this.finishedAt = finishedAt;
    }

    public void stop(Instant finishedAt) {
        this.status = CrawlStatus.STOPPED;
        this.finishedAt = finishedAt;
    }

    public void fail(Instant finishedAt) {
        this.status = CrawlStatus.FAILED;
        this.finishedAt = finishedAt;
    }
}
