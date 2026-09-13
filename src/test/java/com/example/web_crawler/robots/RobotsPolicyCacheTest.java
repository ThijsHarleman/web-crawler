package com.example.web_crawler.robots;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RobotsPolicyCacheTest {
    @Test
    void fetchesRobotsPolicyOnlyOncePerOrigin() {
        AtomicInteger fetchCount = new AtomicInteger();

        RobotsPolicy expectedPolicy = new SimpleRobotsPolicy(List.of());

        RobotsPolicyProvider policyProvider = uri -> {
            fetchCount.incrementAndGet();

            return expectedPolicy;
        };

        RobotsPolicyCache cache = new RobotsPolicyCache(policyProvider);

        URI firstPage = URI.create("https://example.com/page1");

        URI secondPage = URI.create("https://example.com/page2");

        RobotsPolicy firstPolicy = cache.getPolicy(firstPage);

        RobotsPolicy secondPolicy = cache.getPolicy(secondPage);

        assertSame(
            firstPolicy,
            secondPolicy
        );

        assertEquals(
            1,
            fetchCount.get()
        );
    }

    @Test
    void usesDifferentPolicyForDifferentOrigin() {
        AtomicInteger fetchCount = new AtomicInteger();

        RobotsPolicyProvider policyProvider = uri -> {
            fetchCount.incrementAndGet();

            return new SimpleRobotsPolicy(List.of());
        };

        RobotsPolicyCache cache = new RobotsPolicyCache(policyProvider);

        cache.getPolicy(URI.create("https://example.com/page"));

        cache.getPolicy(URI.create("https://other.example.com/page"));

        assertEquals(
            2,
            fetchCount.get()
        );
    }

    @Test
    void treatsHttpAndHttpsAsDifferentOrigins() {
        AtomicInteger fetchCount = new AtomicInteger();

        RobotsPolicyProvider policyProvider = uri -> {
            fetchCount.incrementAndGet();

            return new SimpleRobotsPolicy(List.of());
        };

        RobotsPolicyCache cache = new RobotsPolicyCache(policyProvider);

        cache.getPolicy(URI.create("http://example.com/page"));

        cache.getPolicy(URI.create("https://example.com/page"));

        assertEquals(
            2,
            fetchCount.get()
        );
    }
}
