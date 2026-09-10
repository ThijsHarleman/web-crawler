package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Keyword;
import com.example.web_crawler.repository.KeywordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcKeywordRepository implements KeywordRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcKeywordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Keyword save(Keyword keyword) {
        if (keyword.id() != 0) {
            update(keyword);
            return keyword;
        }

        Optional<Keyword> existing = findByWord(keyword.word());

        if (existing.isPresent()) {
            return existing.get();
        }

        return insert(keyword);
    }

    @Override
    public Optional<Keyword> findByWord(String word) {
        String sql = """
            SELECT
                id,
                word
            FROM keyword
            WHERE word = ?
            """;

        return jdbcTemplate.query(
            sql,
            this::mapRow,
            word
        ).stream().findFirst();
    }

    @Override
    public Optional<Keyword> findById(long id) {
        String sql = """
            SELECT
                id,
                word
            FROM keyword
            WHERE id = ?
            """;

        return jdbcTemplate.query(
            sql,
            this::mapRow,
            id
        ).stream().findFirst();
    }

    private Keyword insert(Keyword keyword) {
        String sql = """
            INSERT INTO keyword (word)
            VALUES (?)
            """;

        jdbcTemplate.update(
            sql,
            keyword.word()
        );

        long generatedId = jdbcTemplate.queryForObject(
            "SELECT last_insert_rowid()",
            Long.class
        );

        return new Keyword(
            generatedId,
            keyword.word()
        );
    }

    private void update(Keyword keyword) {
        String sql = """
            UPDATE keyword
            SET word = ?
            WHERE id = ?
            """;

        jdbcTemplate.update(
            sql,
            keyword.word(),
            keyword.id()
        );
    }

    private Keyword mapRow(ResultSet resultSet, int rowNum)
        throws SQLException {

        return new Keyword(
            resultSet.getLong("id"),
            resultSet.getString("word")
        );
    }
}
