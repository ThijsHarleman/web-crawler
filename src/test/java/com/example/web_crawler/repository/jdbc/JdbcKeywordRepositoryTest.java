package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Keyword;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcKeywordRepositoryTest {
    @Autowired
    private JdbcKeywordRepository keywordRepository;

    @Test
    void savesAndFindsKeywordById() {
        Keyword keyword = new Keyword(
            0L,
            "java"
        );

        Keyword saved = keywordRepository.save(keyword);

        assertTrue(saved.id() > 0);
        assertEquals("java", saved.word());

        Optional<Keyword> found = keywordRepository.findById(saved.id());

        assertTrue(found.isPresent());

        Keyword loaded = found.get();

        assertEquals(saved.id(), loaded.id());
        assertEquals(saved.word(), loaded.word());
    }

    @Test
    void findsKeywordByWord() {
        Keyword keyword = new Keyword(
            0L,
            "spring"
        );

        Keyword saved = keywordRepository.save(keyword);

        Optional<Keyword> found = keywordRepository.findByWord("spring");

        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.get().id());
        assertEquals("spring", found.get().word());
    }

    @Test
    void returnsExistingKeywordWhenSavingDuplicateWord() {
        Keyword first = keywordRepository.save(
            new Keyword(0L, "crawler")
        );

        Keyword second = keywordRepository.save(
            new Keyword(0L, "crawler")
        );

        assertEquals(first.id(), second.id());
        assertEquals("crawler", second.word());
    }

    @Test
    void updatesExistingKeyword() {
        Keyword saved = keywordRepository.save(
            new Keyword(0L, "old-word")
        );

        Keyword updated = new Keyword(
            saved.id(),
            "new-word"
        );

        keywordRepository.save(updated);

        Keyword loaded = keywordRepository.findById(saved.id()).orElseThrow();

        assertEquals(saved.id(), loaded.id());
        assertEquals("new-word", loaded.word());
    }
}
