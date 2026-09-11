package com.example.web_crawler.robots;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleRobotsPolicyTest {
    @Test
    void allowsUriWhenNoRuleMatches() {
        SimpleRobotsPolicy policy =
            new SimpleRobotsPolicy(
                List.of(
                    new SimpleRobotsPolicy.Rule(
                        "/private/",
                        false
                    )
                )
            );

        assertTrue(
            policy.isAllowed(
                URI.create("https://example.com/public/page")
            )
        );
    }

    @Test
    void blocksUriWhenDisallowRuleMatches() {
        SimpleRobotsPolicy policy =
            new SimpleRobotsPolicy(
                List.of(
                    new SimpleRobotsPolicy.Rule(
                        "/private/",
                        false
                    )
                )
            );

        assertFalse(
            policy.isAllowed(
                URI.create("https://example.com/private/page")
            )
        );
    }

    @Test
    void moreSpecificRuleOverridesLessSpecificRule() {
        SimpleRobotsPolicy policy =
            new SimpleRobotsPolicy(
                List.of(
                    new SimpleRobotsPolicy.Rule(
                        "/private/",
                        false
                    ),
                    new SimpleRobotsPolicy.Rule(
                        "/private/public/",
                        true
                    )
                )
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
    void allowsRootWhenNoPathIsPresent() {
        SimpleRobotsPolicy policy =
            new SimpleRobotsPolicy(
                List.of(
                    new SimpleRobotsPolicy.Rule(
                        "/private/",
                        false
                    )
                )
            );

        assertTrue(
            policy.isAllowed(
                URI.create("https://example.com")
            )
        );
    }

    @Test
    void allowsEverythingWhenPolicyHasNoRules() {
        SimpleRobotsPolicy policy =
            new SimpleRobotsPolicy(List.of());

        assertTrue(
            policy.isAllowed(
                URI.create("https://example.com/anything")
            )
        );
    }
}
