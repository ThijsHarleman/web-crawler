package com.example.web_crawler.repository.jdbc;

import com.example.web_crawler.model.Page;
import com.example.web_crawler.model.PageStatus;
import com.example.web_crawler.repository.PageRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcPageRepository implements PageRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page save(Page page) {
        if (page.getId() == 0) {
            return insert(page);
        }

        update(page);
        return page;
    }

    @Override
    public Optional<Page> findById(long id) {
        String sql = """
            SELECT
                id,
                crawl_id,
                uri,
                depth,
                status,
                http_status_code,
                title,
                discovered_at,
                crawled_at,
                error_message
            FROM page
            WHERE id = ?
            """;

        return jdbcTemplate.query(
            sql,
            this::mapRow,
            id
        ).stream().findFirst();
    }

    @Override
    public boolean existsByCrawlIdAndUri(long crawlId, String uri) {
        String sql = """
            SELECT EXISTS(
                SELECT 1
                FROM page
                WHERE crawl_id = ?
                  AND uri = ?
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(
            sql,
            Boolean.class,
            crawlId,
            uri
        );

        return Boolean.TRUE.equals(exists);
    }

    private Page insert(Page page) {
        String sql = """
            INSERT INTO page (
                crawl_id,
                uri,
                depth,
                status,
                http_status_code,
                title,
                discovered_at,
                crawled_at,
                error_message
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            page.getCrawlId(),
            page.getUri().toString(),
            page.getDepth(),
            page.getStatus().name(),
            page.getHttpStatusCode(),
            page.getTitle(),
            toDatabaseTimestamp(page.getDiscoveredAt()),
            toDatabaseTimestamp(page.getCrawledAt()),
            page.getErrorMessage()
        );

        long generatedId = jdbcTemplate.queryForObject(
            "SELECT last_insert_rowid()",
            Long.class
        );

        return new Page(
            generatedId,
            page.getCrawlId(),
            page.getUri(),
            page.getDepth(),
            page.getStatus(),
            page.getHttpStatusCode(),
            page.getTitle(),
            page.getDiscoveredAt(),
            page.getCrawledAt(),
            page.getErrorMessage()
        );
    }

    private void update(Page page) {
        String sql = """
            UPDATE page
            SET
                crawl_id = ?,
                uri = ?,
                depth = ?,
                status = ?,
                http_status_code = ?,
                title = ?,
                discovered_at = ?,
                crawled_at = ?,
                error_message = ?
            WHERE id = ?
            """;

        jdbcTemplate.update(
            sql,
            page.getCrawlId(),
            page.getUri().toString(),
            page.getDepth(),
            page.getStatus().name(),
            page.getHttpStatusCode(),
            page.getTitle(),
            toDatabaseTimestamp(page.getDiscoveredAt()),
            toDatabaseTimestamp(page.getCrawledAt()),
            page.getErrorMessage(),
            page.getId()
        );
    }

    private Page mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Page(
            resultSet.getLong("id"),
            resultSet.getLong("crawl_id"),
            URI.create(resultSet.getString("uri")),
            resultSet.getInt("depth"),
            PageStatus.valueOf(resultSet.getString("status")),
            (Integer) resultSet.getObject("http_status_code"),
            resultSet.getString("title"),
            fromDatabaseTimestamp(resultSet.getString("discovered_at")),
            fromDatabaseTimestamp(resultSet.getString("crawled_at")),
            resultSet.getString("error_message")
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
