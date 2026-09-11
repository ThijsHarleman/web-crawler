package com.example.web_crawler.fetch;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpPageFetcher implements PageFetcher {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;

    public HttpPageFetcher() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Override
    public FetchResult fetch(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(REQUEST_TIMEOUT)
            .header(
                "User-Agent",
                "EducationalWebCrawler/1.0"
            )
            .header(
                "Accept",
                "text/html,application/xhtml+xml"
            )
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse(null);

            return new FetchResult(
                uri,
                response.statusCode(),
                contentType,
                response.body()
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new PageFetchException(
                "HTTP request was interrupted",
                exception
            );

        } catch (Exception exception) {
            throw new PageFetchException(
                "Failed to fetch " + uri,
                exception
            );
        }
    }
}
