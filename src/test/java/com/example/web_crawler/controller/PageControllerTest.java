package com.example.web_crawler.controller;

import com.example.web_crawler.crawler.CrawlExecutor;
import com.example.web_crawler.crawler.CrawlService;
import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.repository.CrawlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@Import(CrawlService.class)
class PageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrawlRepository crawlRepository;

    @MockitoBean
    private CrawlExecutor crawlExecutor;

    @Test
    void servesIndexPage() throws Exception {
        mockMvc.perform(
            get("/")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
    }

    @Test
    void startsCrawl() throws Exception {
        Crawl crawl = new Crawl(
            1,
            URI.create("https://example.com/"),
            2,
            Duration.ofSeconds(60),
            CrawlStatus.NOT_STARTED,
            null,
            null
        );

        when(crawlRepository.save(any(Crawl.class)))
            .thenReturn(crawl);

        mockMvc.perform(
            post("/api/crawls")
                .param("startUrl", "https://example.com/")
                .param("maxDepth", "2")
                .param("maxDurationSeconds", "60")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(
            MediaType.APPLICATION_JSON
        ))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.maxDepth").value(2))
        .andExpect(jsonPath("$.status").value("NOT_STARTED"));
    }

    @Test
    void returnsCrawlById() throws Exception {
        Crawl crawl = new Crawl(
            1,
            URI.create("https://example.com/"),
            2,
            Duration.ofSeconds(60),
            CrawlStatus.RUNNING,
            null,
            null
        );

        when(crawlRepository.findById(1))
            .thenReturn(Optional.of(crawl));

        mockMvc.perform(
            get("/api/crawls/1")
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.id").value(1)
        )
        .andExpect(
            jsonPath("$.status").value("RUNNING")
        );
    }

    @Test
    void returnsNotFoundForUnknownCrawl() throws Exception {
        when(crawlRepository.findById(999))
            .thenReturn(Optional.empty());

        mockMvc.perform(
            get("/api/crawls/999")
        )
        .andExpect(status().isNotFound());
    }
}
