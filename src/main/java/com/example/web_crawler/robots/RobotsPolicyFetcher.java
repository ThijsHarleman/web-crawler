package com.example.web_crawler.robots;

import com.example.web_crawler.fetch.FetchResult;
import com.example.web_crawler.fetch.PageFetcher;
import com.example.web_crawler.fetch.PageFetchException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class RobotsPolicyFetcher {
    private final PageFetcher pageFetcher;

    public RobotsPolicyFetcher(PageFetcher pageFetcher) {
        this.pageFetcher = pageFetcher;
    }

    public RobotsPolicy fetch(URI pageUri) {
        URI robotsUri = buildRobotsUri(pageUri);

        FetchResult result;

        try {
            result = pageFetcher.fetch(robotsUri);
        } catch (PageFetchException exception) {
            return denyAllPolicy();
        }

        if (result.statusCode() == 404) {
            return new SimpleRobotsPolicy(List.of());
        }

        if (result.statusCode() < 200 ||
            result.statusCode() >= 300) {
            return denyAllPolicy();
        }

        return parse(result.body());
    }

    private URI buildRobotsUri(URI pageUri) {
        try {
            return new URI(
                pageUri.getScheme(),
                null,
                pageUri.getHost(),
                pageUri.getPort(),
                "/robots.txt",
                null,
                null
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "Unable to construct robots.txt URI for " + pageUri,
                exception
            );
        }
    }

    private RobotsPolicy denyAllPolicy() {
        return new SimpleRobotsPolicy(
            List.of(
                new SimpleRobotsPolicy.Rule(
                    "/",
                    false
                )
            )
        );
    }

    private RobotsPolicy parse(String content) {
        List<RobotsGroup> groups = parseGroups(content);

        RobotsGroup selectedGroup = selectGroup(groups);

        if (selectedGroup == null) {
            return new SimpleRobotsPolicy(List.of());
        }

        return new SimpleRobotsPolicy(
            selectedGroup.rules()
        );
    }

    private List<RobotsGroup> parseGroups(String content) {
        List<RobotsGroup> groups = new ArrayList<>();

        List<String> userAgents = new ArrayList<>();
        List<SimpleRobotsPolicy.Rule> rules = new ArrayList<>();

        boolean hasRules = false;

        for (String line : content.split("\\R")) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                if (!userAgents.isEmpty() && hasRules) {
                    groups.add(
                        new RobotsGroup(
                            List.copyOf(userAgents),
                            List.copyOf(rules)
                        )
                    );

                    userAgents.clear();
                    rules.clear();
                    hasRules = false;
                }

                continue;
            }

            int commentIndex = trimmed.indexOf('#');

            if (commentIndex >= 0) {
                trimmed = trimmed
                    .substring(0, commentIndex)
                    .trim();
            }

            if (trimmed.isEmpty()) {
                continue;
            }

            int separatorIndex = trimmed.indexOf(':');

            if (separatorIndex < 0) {
                continue;
            }

            String directive = trimmed
                .substring(0, separatorIndex)
                .trim()
                .toLowerCase();

            String value = trimmed
                .substring(separatorIndex + 1)
                .trim();

            if (directive.equals("user-agent")) {
                if (!userAgents.isEmpty() && hasRules) {
                    groups.add(
                        new RobotsGroup(
                            List.copyOf(userAgents),
                            List.copyOf(rules)
                        )
                    );

                    userAgents.clear();
                    rules.clear();
                    hasRules = false;
                }

                if (!value.isEmpty()) {
                    userAgents.add(value.toLowerCase());
                }

                continue;
            }

            if (!directive.equals("allow") &&
                !directive.equals("disallow")) {
                continue;
            }

            if (userAgents.isEmpty()) {
                continue;
            }

            if (value.isEmpty()) {
                if (directive.equals("disallow")) {
                    hasRules = true;
                }

                continue;
            }

            boolean allowed = directive.equals("allow");

            rules.add(
                new SimpleRobotsPolicy.Rule(
                    value,
                    allowed
                )
            );

            hasRules = true;
        }

        if (!userAgents.isEmpty() && hasRules) {
            groups.add(
                new RobotsGroup(
                    List.copyOf(userAgents),
                    List.copyOf(rules)
                )
            );
        }

        return groups;
    }

    private RobotsGroup selectGroup(List<RobotsGroup> groups) {
        String crawlerUserAgent = RobotsConstants.USER_AGENT.toLowerCase();

        for (RobotsGroup group : groups) {
            if (group.userAgents().contains(crawlerUserAgent)) {
                return group;
            }
        }

        for (RobotsGroup group : groups) {
            if (group.userAgents().contains("*")) {
                return group;
            }
        }

        return null;
    }
    
    private record RobotsGroup(
        List<String> userAgents,
        List<SimpleRobotsPolicy.Rule> rules
    ) {
    }
}
