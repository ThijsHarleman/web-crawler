package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlKeyword;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.model.Keyword;
import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageKeyword;
import com.example.web_crawler.model.PageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcPageKeywordRepositoryTest {
    @Autowired
    private JdbcPageKeywordRepository pageKeywordRepository;

    @Autowired
    private JdbcCrawlRepository crawlRepository;

    @Autowired
    private JdbcPageRepository pageRepository;

    @Autowired
    private JdbcKeywordRepository keywordRepository;

    @Test
    void savesAndFindsPageKeywords() {
        Page page = createPage();
        Keyword keyword = keywordRepository.save(
            new Keyword(0L, "java")
        );

        PageKeyword pageKeyword = new PageKeyword(
            page.getId(),
            keyword,
            5,
            2.5
        );

        pageKeywordRepository.save(pageKeyword);

        List<PageKeyword> found = pageKeywordRepository.findByPageId(page.getId());

        assertEquals(1, found.size());

        PageKeyword loaded = found.getFirst();

        assertEquals(page.getId(), loaded.pageId());
        assertEquals(keyword.id(), loaded.keyword().id());
        assertEquals("java", loaded.keyword().word());
        assertEquals(5, loaded.frequency());
        assertEquals(2.5, loaded.score());
    }

    @Test
    void updatesExistingPageKeyword() {
        Page page = createPage();
        Keyword keyword = keywordRepository.save(
            new Keyword(0L, "spring")
        );

        PageKeyword original = new PageKeyword(
            page.getId(),
            keyword,
            2,
            1.0
        );

        pageKeywordRepository.save(original);

        PageKeyword updated = new PageKeyword(
            page.getId(),
            keyword,
            8,
            4.5
        );

        pageKeywordRepository.save(updated);

        List<PageKeyword> found = pageKeywordRepository.findByPageId(page.getId());

        assertEquals(1, found.size());

        PageKeyword loaded = found.getFirst();

        assertEquals(8, loaded.frequency());
        assertEquals(4.5, loaded.score());
    }

    @Test
    void ordersPageKeywordsByScoreDescending() {
        Page page = createPage();

        Keyword java = keywordRepository.save(
            new Keyword(0L, "java")
        );

        Keyword spring = keywordRepository.save(
            new Keyword(0L, "spring")
        );

        pageKeywordRepository.save(
            new PageKeyword(
                page.getId(),
                java,
                3,
                2.0
            )
        );

        pageKeywordRepository.save(
            new PageKeyword(
                page.getId(),
                spring,
                2,
                5.0
            )
        );

        List<PageKeyword> found = pageKeywordRepository.findByPageId(page.getId());

        assertEquals(2, found.size());

        assertEquals("spring", found.get(0).keyword().word());
        assertEquals("java", found.get(1).keyword().word());
    }

    private Page createPage() {
        Crawl crawl = crawlRepository.save(
            new Crawl(
                0L,
                URI.create("https://example.com"),
                2,
                Duration.ofMinutes(5),
                CrawlStatus.NOT_STARTED,
                null,
                null
            )
        );

        return pageRepository.save(
            new Page(
                0L,
                crawl.getId(),
                URI.create("https://example.com/page"),
                0,
                PageStatus.DISCOVERED,
                null,
                null,
                Instant.now(),
                null,
                null
            )
        );
    }

    @Test
    void findsKeywordsAggregatedAcrossPagesInCrawl() {
        Crawl crawl = crawlRepository.save(
            new Crawl(
                0L,
                URI.create("https://example.com"),
                2,
                Duration.ofMinutes(5),
                CrawlStatus.NOT_STARTED,
                null,
                null
            )
        );

        Page firstPage = pageRepository.save(
            new Page(
                0L,
                crawl.getId(),
                URI.create("https://example.com/first"),
                0,
                PageStatus.DISCOVERED,
                null,
                null,
                Instant.now(),
                null,
                null
            )
        );

        Page secondPage = pageRepository.save(
            new Page(
                0L,
                crawl.getId(),
                URI.create("https://example.com/second"),
                1,
                PageStatus.DISCOVERED,
                null,
                null,
                Instant.now(),
                null,
                null
            )
        );

        Keyword java = keywordRepository.save(
            new Keyword(0L, "java")
        );

        Keyword spring = keywordRepository.save(
            new Keyword(0L, "spring")
        );

        pageKeywordRepository.save(
            new PageKeyword(
                firstPage.getId(),
                java,
                5,
                0.5
            )
        );

        pageKeywordRepository.save(
            new PageKeyword(
                firstPage.getId(),
                spring,
                3,
                0.3
            )
        );

        pageKeywordRepository.save(
            new PageKeyword(
                secondPage.getId(),
                java,
                7,
                0.7
            )
        );

        pageKeywordRepository.save(
            new PageKeyword(
                secondPage.getId(),
                spring,
                2,
                0.2
            )
        );

        List<CrawlKeyword> found =
            pageKeywordRepository.findByCrawlId(crawl.getId());

        assertEquals(2, found.size());

        CrawlKeyword first = found.get(0);
        CrawlKeyword second = found.get(1);

        assertEquals("java", first.keyword().word());
        assertEquals(12, first.frequency());
        assertEquals(12.0 / 17.0, first.score(), 0.0001);

        assertEquals("spring", second.keyword().word());
        assertEquals(5, second.frequency());
        assertEquals(5.0 / 17.0, second.score(), 0.0001);
    }
}
