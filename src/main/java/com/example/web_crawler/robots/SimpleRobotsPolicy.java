package com.example.web_crawler.robots;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

public class SimpleRobotsPolicy implements RobotsPolicy {
    private final List<Rule> rules;

    public SimpleRobotsPolicy(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean isAllowed(URI uri) {
        String rawPath = uri.getRawPath();

        final String path =
            rawPath == null || rawPath.isEmpty()
                ? "/"
                : rawPath;

        Rule matchingRule = rules.stream()
            .filter(rule -> path.startsWith(rule.path()))
            .max(Comparator.comparingInt(rule ->
                rule.path().length()
            ))
            .orElse(null);

        if (matchingRule == null) {
            return true;
        }

        return matchingRule.allowed();
    }

    public record Rule(
        String path,
        boolean allowed
    ) {
    }
}
