package com.example.web_crawler.config;

import com.example.web_crawler.crawler.BreadthFirstCrawlerFactory;
import com.example.web_crawler.crawler.CrawlExecutor;
import com.example.web_crawler.crawler.CrawlerFactory;
import com.example.web_crawler.crawler.SingleCrawlExecutor;
import com.example.web_crawler.fetch.HttpPageFetcher;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.fetch.RateLimitedPageFetcher;
import com.example.web_crawler.parser.HtmlPageParser;
import com.example.web_crawler.ratelimit.DefaultRateLimiter;
import com.example.web_crawler.ratelimit.RateLimiter;
import com.example.web_crawler.repository.CrawlRepository;
import com.example.web_crawler.repository.PageRepository;
import com.example.web_crawler.robots.RobotsPolicyFetcher;
import com.example.web_crawler.url.UrlNormalizer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class CrawlerConfiguration {
    @Bean
    public RateLimiter rateLimiter(
        @Value("${crawler.rate-limit.minimum-delay}")
        Duration minimumDelay
    ) {
        return new DefaultRateLimiter(minimumDelay);
    }

    @Bean
    @Primary
    public PageFetcher rateLimitedPageFetcher(
        HttpPageFetcher httpPageFetcher,
        RateLimiter rateLimiter
    ) {
        return new RateLimitedPageFetcher(
            httpPageFetcher,
            rateLimiter
        );
    }

    @Bean
    public CrawlerFactory crawlerFactory(
        PageRepository pageRepository,
        RobotsPolicyFetcher robotsPolicyFetcher,
        PageFetcher pageFetcher,
        HtmlPageParser pageParser,
        UrlNormalizer urlNormalizer,
        Clock clock
    ) {
        return new BreadthFirstCrawlerFactory(
            pageRepository,
            robotsPolicyFetcher,
            pageFetcher,
            pageParser,
            urlNormalizer,
            clock
        );
    }

    @Bean(destroyMethod = "close")
    public CrawlExecutor crawlExecutor(
        CrawlerFactory crawlerFactory,
        CrawlRepository crawlRepository
    ) {
        return new SingleCrawlExecutor(
            crawlerFactory,
            crawlRepository
        );
    }
}
