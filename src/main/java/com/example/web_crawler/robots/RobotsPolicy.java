package com.example.web_crawler.robots;

import java.net.URI;

public interface RobotsPolicy {
    boolean isAllowed(URI uri);
}
