package com.example.web_crawler.robots;

import java.net.URI;

public interface RobotsPolicyProvider {
    RobotsPolicy getPolicy(URI uri);
}
