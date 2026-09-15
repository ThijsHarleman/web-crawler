package com.example.web_crawler.keyword;

import com.example.web_crawler.model.Keyword;
import com.example.web_crawler.model.PageKeyword;
import com.example.web_crawler.repository.KeywordRepository;
import com.example.web_crawler.repository.PageKeywordRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultKeywordAnalysisService implements KeywordAnalysisService {
    private final KeywordExtractor keywordExtractor;
    private final KeywordRepository keywordRepository;
    private final PageKeywordRepository pageKeywordRepository;
    private static final Logger logger = LoggerFactory.getLogger(
        DefaultKeywordAnalysisService.class
    );

    public DefaultKeywordAnalysisService(
        KeywordExtractor keywordExtractor,
        KeywordRepository keywordRepository,
        PageKeywordRepository pageKeywordRepository
    ) {
        this.keywordExtractor = keywordExtractor;
        this.keywordRepository = keywordRepository;
        this.pageKeywordRepository = pageKeywordRepository;
    }

    @Override
    public void analyze(long pageId, String text) {
        logger.info(
            "Analyzing keywords for page {}",
            pageId
        );
        Map<String, Integer> frequencies = keywordExtractor.extract(text);

        logger.info(
            "Extracted {} unique keywords for page {}",
            frequencies.size(),
            pageId
        );

        int totalWords = frequencies.values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();

        if (totalWords == 0) {
            return;
        }

        for (Map.Entry<String, Integer> entry :
            frequencies.entrySet()) {

            String word = entry.getKey();
            int frequency = entry.getValue();

            Keyword keyword = keywordRepository.save(new Keyword(0, word));

            double score = (double) frequency / totalWords;

            PageKeyword pageKeyword = new PageKeyword(
                pageId,
                keyword,
                frequency,
                score
            );

            pageKeywordRepository.save(pageKeyword);
        }

        logger.info(
            "Finished storing keywords for page {}",
            pageId
        );
    }
}
