package com.example.web_crawler.ratelimit;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DefaultRateLimiter
    implements RateLimiter {

    private final Duration minimumDelay;
    private final Map<RateLimitOrigin, Instant> lastRequestTimes;

    public DefaultRateLimiter(Duration minimumDelay) {
        if (minimumDelay.isNegative()) {
            throw new IllegalArgumentException(
                "Minimum delay cannot be negative"
            );
        }

        this.minimumDelay = minimumDelay;
        this.lastRequestTimes = new HashMap<>();
    }

    @Override
    public void waitIfNecessary(URI uri) {
        RateLimitOrigin origin = RateLimitOrigin.from(uri);

        Instant now = Instant.now();

        Instant lastRequest = lastRequestTimes.get(origin);

        if (lastRequest != null) {
            Duration elapsed = Duration.between(lastRequest, now);

            Duration remaining = minimumDelay.minus(elapsed);

            if (!remaining.isNegative()
                && !remaining.isZero()) {
                sleep(remaining);
            }
        }

        lastRequestTimes.put(
            origin,
            Instant.now()
        );
    }

    private void sleep(Duration duration) {
        try {
            long millis = duration.toMillis();

            int nanos = duration
                .minusMillis(millis)
                .getNano();

            Thread.sleep(millis, nanos);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RateLimitInterruptedException(
                "Rate limiter was interrupted",
                exception
            );
        }
    }
}
