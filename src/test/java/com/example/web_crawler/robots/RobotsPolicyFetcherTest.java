package com.example.web_crawler.robots;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.fetch.PageFetchException;
import com.example.web_crawler.fetch.PageFetcher;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RobotsPolicyFetcherTest {
    @Test
    void fetchesAndParsesRobotsTxt() {
        PageFetcher pageFetcher = uri -> {
            assertEquals(
                URI.create("https://example.com/robots.txt"),
                uri
            );

            return new FetchResult(
                uri,
                200,
                "text/plain",
                """
                User-agent: *
                Disallow: /private/
                Allow: /private/public/
                """
            );
        };

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/secret"
                )
            )
        );

        assertTrue(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/public/page"
                )
            )
        );
    }

    @Test
    void allowsEverythingWhenRobotsTxtDoesNotExist() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                404,
                "text/plain",
                ""
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertTrue(
            policy.isAllowed(
                URI.create("https://example.com/private")
            )
        );
    }

    @Test
    void ignoresRulesForOtherUserAgents() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                200,
                "text/plain",
                """
                User-agent: SomeOtherBot
                Disallow: /

                User-agent: *
                Disallow: /private/
                """
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/page"
                )
            )
        );

        assertTrue(
            policy.isAllowed(
                URI.create(
                    "https://example.com/public/page"
                )
            )
        );
    }

    @Test
    void ignoresComments() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                200,
                "text/plain",
                """
                # Site robots rules
                User-agent: *
                Disallow: /private/ # internal pages
                """
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/page"
                )
            )
        );
    }

    @Test
    void ignoresNonSuccessfulResponses() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                500,
                "text/plain",
                "Server error"
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create("https://example.com/private")
            )
        );
    }

    @Test
    void doesNotIncludeUserInfoInRobotsUri() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();

        PageFetcher pageFetcher = uri -> {
            requestedUri.set(uri);

            return new FetchResult(
                uri,
                404,
                "text/plain",
                ""
            );
        };

        RobotsPolicyFetcher fetcher =
            new RobotsPolicyFetcher(pageFetcher);

        fetcher.getPolicy(
            URI.create(
                "https://username:password@example.com/page"
            )
        );

        assertEquals(
            URI.create("https://example.com/robots.txt"),
            requestedUri.get()
        );
    }

    @Test
    void deniesEverythingWhenRobotsTxtCannotBeFetched() {
        PageFetcher pageFetcher = uri -> {
            throw new PageFetchException(
                "Connection failed",
                new RuntimeException("Network error")
            );
        };

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create("https://example.com/")
            )
        );

        assertFalse(
            policy.isAllowed(
                URI.create("https://example.com/public/page")
            )
        );
    }

    @Test
    void prefersSpecificUserAgentOverWildcard() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                200,
                "text/plain",
                """
                User-agent: *
                Disallow: /

                User-agent: EducationalWebCrawler
                Allow: /
                """
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertTrue(
            policy.isAllowed(
                URI.create("https://example.com/public")
            )
        );
    }

    @Test
    void fallsBackToWildcardWhenSpecificUserAgentIsAbsent() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                200,
                "text/plain",
                """
                User-agent: SomeOtherBot
                Disallow: /

                User-agent: *
                Disallow: /private/
                """
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/page"
                )
            )
        );

        assertTrue(
            policy.isAllowed(
                URI.create(
                    "https://example.com/public/page"
                )
            )
        );
    }

    @Test
    void supportsMultipleUserAgentsInOneGroup() {
        PageFetcher pageFetcher = uri ->
            new FetchResult(
                uri,
                200,
                "text/plain",
                """
                User-agent: SomeOtherBot
                User-agent: EducationalWebCrawler
                Disallow: /private/
                """
            );

        RobotsPolicyFetcher fetcher = new RobotsPolicyFetcher(pageFetcher);

        RobotsPolicy policy = fetcher.getPolicy(
            URI.create("https://example.com/page")
        );

        assertFalse(
            policy.isAllowed(
                URI.create(
                    "https://example.com/private/page"
                )
            )
        );
    }
}
