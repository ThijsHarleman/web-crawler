package com.example.web_crawler.keyword;

import com.example.web_crawler.model.Keyword;
import com.example.web_crawler.model.PageKeyword;
import com.example.web_crawler.repository.KeywordRepository;
import com.example.web_crawler.repository.PageKeywordRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordAnalysisServiceTest {
    @Test
    void analyzesAndPersistsKeywords() {
        KeywordRepository keywordRepository = new InMemoryKeywordRepository();

        InMemoryPageKeywordRepository pageKeywordRepository = 
            new InMemoryPageKeywordRepository();

        KeywordAnalysisService service = new DefaultKeywordAnalysisService(
            new BasicKeywordExtractor(),
            keywordRepository,
            pageKeywordRepository
        );

        service.analyze(
            42L,
            "Java Java Spring Spring Spring crawler"
        );

        assertEquals(3, pageKeywordRepository.saved.size());

        PageKeyword java = findByWord(pageKeywordRepository, "java");

        PageKeyword spring = findByWord(pageKeywordRepository, "spring");

        PageKeyword crawler = findByWord(pageKeywordRepository, "crawler");

        assertEquals(2, java.frequency());
        assertEquals(3, spring.frequency());
        assertEquals(1, crawler.frequency());

        assertEquals(
            2.0 / 6.0,
            java.score(),
            0.000001
        );

        assertEquals(
            3.0 / 6.0,
            spring.score(),
            0.000001
        );

        assertEquals(
            1.0 / 6.0,
            crawler.score(),
            0.000001
        );
    }

    @Test
    void doesNothingWhenThereAreNoKeywords() {
        KeywordRepository keywordRepository = new InMemoryKeywordRepository();

        InMemoryPageKeywordRepository pageKeywordRepository = 
            new InMemoryPageKeywordRepository();

        KeywordAnalysisService service = new DefaultKeywordAnalysisService(
            new BasicKeywordExtractor(),
            keywordRepository,
            pageKeywordRepository
        );

        service.analyze(
            42L,
            "the and for"
        );

        assertTrue(
            pageKeywordRepository.saved.isEmpty()
        );
    }

    private PageKeyword findByWord(
        InMemoryPageKeywordRepository repository,
        String word
    ) {
        return repository.saved.stream()
            .filter(pageKeyword ->
                pageKeyword.keyword().word().equals(word)
            )
            .findFirst()
            .orElseThrow();
    }

    private static class InMemoryKeywordRepository
        implements KeywordRepository {

        private long nextId = 1;

        private final List<Keyword> saved = new ArrayList<>();

        @Override
        public Keyword save(Keyword keyword) {
            Keyword savedKeyword = new Keyword(
                nextId++,
                keyword.word()
            );

            saved.add(savedKeyword);

            return savedKeyword;
        }

        @Override
        public Optional<Keyword> findByWord(
            String word
        ) {
            return saved.stream()
                .filter(keyword ->
                    keyword.word().equals(word)
                )
                .findFirst();
        }

        @Override
        public Optional<Keyword> findById(long id) {
            return saved.stream()
                .filter(keyword ->
                    keyword.id() == id
                )
                .findFirst();
        }
    }

    private static class InMemoryPageKeywordRepository implements PageKeywordRepository {
        private final List<PageKeyword> saved = new ArrayList<>();

        @Override
        public void save(PageKeyword pageKeyword) {
            saved.add(pageKeyword);
        }

        @Override
        public List<PageKeyword> findByPageId(
            long pageId
        ) {
            return saved.stream()
                .filter(pageKeyword ->
                    pageKeyword.pageId() == pageId
                )
                .toList();
        }
    }
}
