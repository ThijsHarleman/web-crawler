package com.example.web_crawler.keyword;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ResourceStopWordProvider implements StopWordProvider {
    @Override
    public Set<String> getStopWords(String language) {
        String resourcePath = "stopwords/" + language + ".txt";

        ClassPathResource resource = new ClassPathResource(resourcePath);

        if (!resource.exists()) {
            return Set.of();
        }

        try (
            InputStream inputStream = resource.getInputStream();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    inputStream,
                    StandardCharsets.UTF_8
                )
            )
        ) {
            return reader.lines()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("#"))
                .collect(Collectors.toUnmodifiableSet());

        } catch (IOException exception) {
            throw new IllegalStateException(
                "Failed to load stop words for language: "
                    + language,
                exception
            );
        }
    }
}
