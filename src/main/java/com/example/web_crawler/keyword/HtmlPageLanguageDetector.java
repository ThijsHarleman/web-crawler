package com.example.web_crawler.keyword;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class HtmlPageLanguageDetector implements PageLanguageDetector {
    @Override
    public Optional<String> detect(Document document) {
        if (document == null) {
            return Optional.empty();
        }

        String language = document
                .select("html")
                .attr("lang")
                .trim();

        if (language.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(normalizeLanguage(language));
    }

    private String normalizeLanguage(String language) {
        String normalized = language
                .toLowerCase(Locale.ROOT)
                .replace('_', '-');

        int separatorIndex = normalized.indexOf('-');

        if (separatorIndex >= 0) {
            return normalized.substring(0, separatorIndex);
        }

        return normalized;
    }
}
