package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Keyword;
import com.example.web_crawler.model.PageKeyword;
import com.example.web_crawler.repository.PageKeywordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcPageKeywordRepository implements PageKeywordRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPageKeywordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PageKeyword pageKeyword) {
        String sql = """
            INSERT INTO page_keyword (
                page_id,
                keyword_id,
                frequency,
                score
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT(page_id, keyword_id)
            DO UPDATE SET
                frequency = excluded.frequency,
                score = excluded.score
            """;

        jdbcTemplate.update(
            sql,
            pageKeyword.pageId(),
            pageKeyword.keyword().id(),
            pageKeyword.frequency(),
            pageKeyword.score()
        );
    }

    @Override
    public List<PageKeyword> findByPageId(long pageId) {
        String sql = """
            SELECT
                pk.page_id,
                k.id AS keyword_id,
                k.word,
                pk.frequency,
                pk.score
            FROM page_keyword pk
            JOIN keyword k
                ON k.id = pk.keyword_id
            WHERE pk.page_id = ?
            ORDER BY pk.score DESC, k.word ASC
            """;

        return jdbcTemplate.query(
            sql,
            this::mapRow,
            pageId
        );
    }

    private PageKeyword mapRow(ResultSet resultSet, int rowNum)
        throws SQLException {

        Keyword keyword = new Keyword(
            resultSet.getLong("keyword_id"),
            resultSet.getString("word")
        );

        return new PageKeyword(
            resultSet.getLong("page_id"),
            keyword,
            resultSet.getInt("frequency"),
            resultSet.getDouble("score")
        );
    }
}
