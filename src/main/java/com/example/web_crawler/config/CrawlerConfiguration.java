package com.example.web_crawler.config;

import com.example.web_crawler.fetch.HttpPageFetcher;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.fetch.RateLimitedPageFetcher;
import com.example.web_crawler.ratelimit.DefaultRateLimiter;
import com.example.web_crawler.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
}
