package com.example.web_crawler.controller;

import com.example.web_crawler.crawler.CrawlService;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlKeyword;
import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageKeyword;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageKeywordRepository;
import com.example.web_crawler.repository.PageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.time.Duration;

@Controller
public class PageController {
    private final CrawlService crawlService;
    private final CrawlRepository crawlRepository;
    private final PageRepository pageRepository;
    private final PageKeywordRepository pageKeywordRepository;

    public PageController(
        CrawlService crawlService,
        CrawlRepository crawlRepository,
        PageRepository pageRepository,
        PageKeywordRepository pageKeywordRepository
    ) {
        this.crawlService = crawlService;
        this.crawlRepository = crawlRepository;
        this.pageRepository = pageRepository;
        this.pageKeywordRepository = pageKeywordRepository;
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

    @GetMapping("/api/crawls/{id}/pages")
    @ResponseBody
    public ResponseEntity<List<Page>> getPages(
        @PathVariable long id
    ) {
        if (crawlRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
            pageRepository.findByCrawlId(id)
        );
    }

    @GetMapping("/api/pages/{id}/keywords")
    @ResponseBody
    public ResponseEntity<List<PageKeyword>> getPageKeywords(
        @PathVariable long id
    ) {
        if (pageRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
            pageKeywordRepository.findByPageId(id)
        );
    }

    @GetMapping("/api/crawls/{id}/keywords")
    @ResponseBody
    public ResponseEntity<List<CrawlKeyword>> getCrawlKeywords(
        @PathVariable long id
    ) {
        if (crawlRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
            pageKeywordRepository.findByCrawlId(id)
        );
    }
}
