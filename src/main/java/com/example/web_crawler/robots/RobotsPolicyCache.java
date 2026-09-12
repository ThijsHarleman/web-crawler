package com.example.web_crawler.robots;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RobotsPolicyCache implements RobotsPolicyProvider {
    private final RobotsPolicyProvider policyProvider;
    private final Map<RobotsOrigin, RobotsPolicy> policies;

    public RobotsPolicyCache(
        RobotsPolicyProvider policyProvider
    ) {
        this.policyProvider = policyProvider;
        this.policies = new HashMap<>();
    }

    @Override
    public RobotsPolicy getPolicy(URI uri) {
        RobotsOrigin origin = RobotsOrigin.from(uri);

        return policies.computeIfAbsent(
            origin,
            key -> policyProvider.getPolicy(uri)
        );
    }
}
