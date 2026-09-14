package com.example.web_crawler.controller;

import com.example.web_crawler.crawler.CrawlService;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.repository.CrawlRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;

@Controller
public class PageController {
    private final CrawlService crawlService;
    private final CrawlRepository crawlRepository;

    public PageController(
        CrawlService crawlService,
        CrawlRepository crawlRepository
    ) {
        this.crawlService = crawlService;
        this.crawlRepository = crawlRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/crawls")
    @ResponseBody
    public Crawl startCrawl(
        @RequestParam String startUrl,
        @RequestParam int maxDepth,
        @RequestParam long maxDurationSeconds
    ) {
        return crawlService.start(
            URI.create(startUrl),
            maxDepth,
            Duration.ofSeconds(maxDurationSeconds)
        );
    }

    @GetMapping("/api/crawls/{id}")
    @ResponseBody
    public ResponseEntity<Crawl> getCrawl(
        @PathVariable long id
    ) {
        return crawlRepository
            .findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(
                () -> ResponseEntity.notFound().build()
            );
    }
}
