CREATE TABLE IF NOT EXISTS crawl (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    start_url TEXT NOT NULL,
    max_depth INTEGER NOT NULL,
    max_duration_seconds INTEGER NOT NULL,
    status TEXT NOT NULL,
    started_at TEXT,
    finished_at TEXT
);

CREATE TABLE IF NOT EXISTS page (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    crawl_id INTEGER NOT NULL,
    uri TEXT NOT NULL,
    depth INTEGER NOT NULL,
    status TEXT NOT NULL,
    http_status_code INTEGER,
    title TEXT,
    discovered_at TEXT NOT NULL,
    crawled_at TEXT,
    error_message TEXT,

    FOREIGN KEY (crawl_id) REFERENCES crawl(id),

    UNIQUE (crawl_id, uri)
);

CREATE TABLE IF NOT EXISTS keyword (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS page_keyword (
    page_id INTEGER NOT NULL,
    keyword_id INTEGER NOT NULL,
    frequency INTEGER NOT NULL,
    score REAL NOT NULL,

    PRIMARY KEY (page_id, keyword_id),

    FOREIGN KEY (page_id) REFERENCES page(id),
    FOREIGN KEY (keyword_id) REFERENCES keyword(id)
);

CREATE INDEX IF NOT EXISTS idx_page_crawl_id
    ON page(crawl_id);

CREATE INDEX IF NOT EXISTS idx_page_keyword_keyword_id
    ON page_keyword(keyword_id);
