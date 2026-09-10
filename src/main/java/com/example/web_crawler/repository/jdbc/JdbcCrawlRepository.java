package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Crawl;
import com.example.web_crawler.model.CrawlStatus;
import com.example.web_crawler.repository.CrawlRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcCrawlRepository implements CrawlRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCrawlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Crawl save(Crawl crawl) {
        if (crawl.getId() == 0) {
            return insert(crawl);
        }

        update(crawl);
        return crawl;
    }

    @Override
    public Optional<Crawl> findById(long id) {
        String sql = """
            SELECT
                id,
                start_url,
                max_depth,
                max_duration_seconds,
                status,
                started_at,
                finished_at
            FROM crawl
            WHERE id = ?
            """;

        return jdbcTemplate.query(
            sql,
            this::mapRow,
            id
        ).stream().findFirst();
    }

    private Crawl insert(Crawl crawl) {
        String sql = """
            INSERT INTO crawl (
                start_url,
                max_depth,
                max_duration_seconds,
                status,
                started_at,
                finished_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            crawl.getStartUrl().toString(),
            crawl.getMaxDepth(),
            crawl.getMaxDuration().toSeconds(),
            crawl.getStatus().name(),
            toDatabaseTimestamp(crawl.getStartedAt()),
            toDatabaseTimestamp(crawl.getFinishedAt())
        );

        long generatedId = jdbcTemplate.queryForObject(
            "SELECT last_insert_rowid()",
            Long.class
        );

        return new Crawl(
            generatedId,
            crawl.getStartUrl(),
            crawl.getMaxDepth(),
            crawl.getMaxDuration(),
            crawl.getStatus(),
            crawl.getStartedAt(),
            crawl.getFinishedAt()
        );
    }

    private void update(Crawl crawl) {
        String sql = """
            UPDATE crawl
            SET
                start_url = ?,
                max_depth = ?,
                max_duration_seconds = ?,
                status = ?,
                started_at = ?,
                finished_at = ?
            WHERE id = ?
            """;

        jdbcTemplate.update(
            sql,
            crawl.getStartUrl().toString(),
            crawl.getMaxDepth(),
            crawl.getMaxDuration().toSeconds(),
            crawl.getStatus().name(),
            toDatabaseTimestamp(crawl.getStartedAt()),
            toDatabaseTimestamp(crawl.getFinishedAt()),
            crawl.getId()
        );
    }

    private Crawl mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Crawl(
            resultSet.getLong("id"),
            URI.create(resultSet.getString("start_url")),
            resultSet.getInt("max_depth"),
            Duration.ofSeconds(resultSet.getLong("max_duration_seconds")),
            CrawlStatus.valueOf(resultSet.getString("status")),
            fromDatabaseTimestamp(resultSet.getString("started_at")),
            fromDatabaseTimestamp(resultSet.getString("finished_at"))
        );
    }

    private String toDatabaseTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.toString();
    }

    private Instant fromDatabaseTimestamp(String value) {
        if (value == null) {
            return null;
        }

        return Instant.parse(value);
    }
}
