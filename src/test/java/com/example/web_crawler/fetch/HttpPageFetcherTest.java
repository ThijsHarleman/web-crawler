package com.example.web_crawler.fetch;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpPageFetcherTest {
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(
            new InetSocketAddress(0),
            0
        );

        server.createContext(
            "/page",
            exchange -> {
                String body = """
                    <html>
                        <head>
                            <title>Test Page</title>
                        </head>
                        <body>
                            Hello world
                        </body>
                    </html>
                    """;

                exchange.getResponseHeaders()
                    .set(
                        "Content-Type",
                        "text/html; charset=UTF-8"
                    );

                exchange.sendResponseHeaders(
                    200,
                    body.getBytes().length
                );

                try (OutputStream output =
                         exchange.getResponseBody()) {
                    output.write(body.getBytes());
                }
            }
        );

        server.createContext(
            "/not-found",
            exchange -> {
                String body = "Not found";

                exchange.sendResponseHeaders(
                    404,
                    body.getBytes().length
                );

                try (OutputStream output =
                        exchange.getResponseBody()) {
                    output.write(body.getBytes());
                }
            }
        );

        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void fetchesHtmlPage() {
        HttpPageFetcher fetcher = new HttpPageFetcher();

        URI uri = URI.create(
            "http://localhost:"
                + server.getAddress().getPort()
                + "/page"
        );

        FetchResult result = fetcher.fetch(uri);

        assertEquals(uri, result.uri());
        assertEquals(200, result.statusCode());
        assertEquals(
            "text/html; charset=UTF-8",
            result.contentType()
        );
        assertEquals(
            true,
            result.body().contains("Test Page")
        );
    }

    @Test
    void returnsHttpErrorStatus() {
        HttpPageFetcher fetcher = new HttpPageFetcher();

        URI uri = URI.create(
            "http://localhost:"
                + server.getAddress().getPort()
                + "/not-found"
        );

        FetchResult result = fetcher.fetch(uri);

        assertEquals(404, result.statusCode());
        assertEquals("Not found", result.body());
    }
}
