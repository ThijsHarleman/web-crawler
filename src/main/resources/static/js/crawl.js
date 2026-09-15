const form = document.getElementById("crawl-form");
const statusElement = document.getElementById("crawl-status");
const resultsElement = document.getElementById("crawl-results");
const keywordAnalysisElement = document.getElementById("keyword-analysis");
const wordCloudContainer = document.getElementById("word-cloud-container");
const keywordTableElement = document.getElementById("keyword-table");
const keywordTableContainer = document.getElementById("keyword-table-container");

let lastKeywordData = null;

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(form);

    const params = new URLSearchParams(formData);

    try {
        const response = await fetch(
            "/api/crawls",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params
            }
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const crawl = await response.json();

        renderCrawlStatus(crawl);

        startPolling(crawl.id);

    } catch (error) {
        renderCrawlStatusError(
            "Failed to start crawl",
            error
        );
    }
});

function renderCrawlStatus(crawl) {
    statusElement.innerHTML = "";

    const heading = document.createElement("h2");
    heading.textContent = "Crawl Status";

    statusElement.appendChild(heading);

    appendStatusValue(
        statusElement,
        "ID",
        crawl.id
    );

    appendStatusValue(
        statusElement,
        "Status",
        crawl.status
    );

    appendStatusValue(
        statusElement,
        "Start URL",
        crawl.startUrl
    );
}

function renderCrawlStatusError(message, error) {
    statusElement.innerHTML = "";

    const heading = document.createElement("h2");
    heading.textContent = "Crawl Status";

    statusElement.appendChild(heading);

    const paragraph = document.createElement("p");

    paragraph.textContent = `${message}: ${error.message}`;

    statusElement.appendChild(paragraph);
}

function appendStatusValue(container, label, value) {
    const paragraph = document.createElement("p");

    const strong = document.createElement("strong");
    strong.textContent = `${label}:`;

    paragraph.appendChild(strong);
    paragraph.append(` ${value}`);

    container.appendChild(paragraph);
}

function startPolling(crawlId) {
    const intervalId = setInterval(
        async () => {
            try {
                const response = await fetch(`/api/crawls/${crawlId}`);

                if (!response.ok) {
                    clearInterval(intervalId);
                    return;
                }

                const crawl = await response.json();

                renderCrawlStatus(crawl);

                await loadCrawlPages(crawlId);
                await loadCrawlKeywords(crawlId);

                if (
                    crawl.status === "COMPLETED" ||
                    crawl.status === "STOPPED" ||
                    crawl.status === "FAILED"
                ) {
                    clearInterval(intervalId);
                }

            } catch (error) {
                clearInterval(intervalId);

                renderCrawlStatusError(
                    "Failed to retrieve crawl status",
                    error
                );
            }
        },
        1000
    );
}

async function loadCrawlPages(crawlId) {
    const response = await fetch(`/api/crawls/${crawlId}/pages`);

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const pages = await response.json();

    renderCrawlPages(pages);
}

function renderCrawlPages(pages) {
    resultsElement.innerHTML = "";

    const heading = document.createElement("h2");
    heading.textContent = "Pages Discovered";

    resultsElement.appendChild(heading);

    if (pages.length === 0) {
        const message = document.createElement("p");
        message.textContent = "No pages discovered yet.";
        resultsElement.appendChild(message);
        return;
    }

    const table = document.createElement("table");

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const headers = [
        "Depth",
        "Status",
        "HTTP",
        "Title",
        "URL",
        "Error"
    ];

    for (const header of headers) {
        const cell = document.createElement("th");
        cell.textContent = header;
        headerRow.appendChild(cell);
    }

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    for (const page of pages) {
        const row = document.createElement("tr");

        appendCell(row, page.depth);
        appendCell(row, page.status);
        appendCell(row, page.httpStatusCode ?? "");
        appendCell(row, page.title ?? "");
        appendCell(row, page.uri);
        appendCell(row, page.errorMessage ?? "");

        tbody.appendChild(row);
    }

    table.appendChild(tbody);
    resultsElement.appendChild(table);
}

function appendCell(row, value) {
    const cell = document.createElement("td");

    cell.textContent = value;

    row.appendChild(cell);
}

async function loadCrawlKeywords(crawlId) {
    try {
        const response = await fetch(`/api/crawls/${crawlId}/keywords`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const keywords = await response.json();

        const keywordData = JSON.stringify(keywords);

        if (keywordData === lastKeywordData) {
            return;
        }

        lastKeywordData = keywordData;

        renderCrawlKeywords(keywords);

    } catch (error) {
        renderKeywordError(error);
    }
}

function renderCrawlKeywords(keywords) {
    if (
        keywordAnalysisElement === null ||
        wordCloudContainer === null ||
        keywordTableElement === null ||
        keywordTableContainer === null
    ) {
        throw new Error(
            "Keyword analysis elements are missing from the page."
        );
    }

    wordCloudContainer.innerHTML = "";
    keywordTableContainer.innerHTML = "";

    if (keywords.length === 0) {
        renderNoKeywords();

        return;
    }

    renderWordCloud(keywords);
    renderKeywordTable(keywords);
}

function renderNoKeywords() {
    wordCloudContainer.innerHTML = "";

    const message = document.createElement("p");

    message.textContent = "No keywords discovered yet.";

    wordCloudContainer.appendChild(message);

    keywordTableContainer.innerHTML = "";

    const tableMessage = document.createElement("p");

    tableMessage.textContent = "No keywords discovered yet.";

    keywordTableContainer.appendChild(tableMessage);
}

function renderKeywordError(error) {
    wordCloudContainer.innerHTML = "";

    const cloudMessage = document.createElement("p");

    cloudMessage.textContent = `Failed to load keyword analysis: ${error.message}`;

    wordCloudContainer.appendChild(cloudMessage);

    keywordTableContainer.innerHTML = "";

    const tableMessage = document.createElement("p");

    tableMessage.textContent = "Keyword results are unavailable.";

    keywordTableContainer.appendChild(tableMessage);
}

function renderWordCloud(keywords) {
    wordCloudContainer.innerHTML = "";

    const canvas = document.createElement("canvas");
    canvas.id = "word-cloud";

    wordCloudContainer.appendChild(canvas);

    const width = wordCloudContainer.offsetWidth;
    const height = wordCloudContainer.offsetHeight;

    canvas.width = width;
    canvas.height = height;

    const wordCloudData = keywords.map(
        (crawlKeyword) => [
            crawlKeyword.keyword.word,
            crawlKeyword.frequency
        ]
    );

    const maxFrequency = Math.max(
        ...keywords.map(
            (crawlKeyword) => crawlKeyword.frequency
        )
    );

    const maxFontSize = 60;
    const minFontSize = 12;

    const weightFactor = (frequency) => {
        if (maxFrequency === 0) {
            return minFontSize;
        }

        return Math.max(
            minFontSize,
            (frequency / maxFrequency) * maxFontSize
        );
    };

    WordCloud(canvas, {
        list: wordCloudData,
        gridSize: 8,
        weightFactor: weightFactor,
        minSize: minFontSize,
        rotateRatio: 0.2,
        rotationSteps: 2,
        backgroundColor: "white",
        drawOutOfBound: false,
        shrinkToFit: true
    });
}

function renderKeywordTable(keywords) {
    keywordTableContainer.innerHTML = "";

    const table = document.createElement("table");

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const headers = [
        "Keyword",
        "Frequency",
        "Score"
    ];

    for (const header of headers) {
        const cell = document.createElement("th");

        cell.textContent = header;

        headerRow.appendChild(cell);
    }

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    for (const crawlKeyword of keywords) {
        const row = document.createElement("tr");

        appendCell(row, crawlKeyword.keyword.word);
        appendCell(row, crawlKeyword.frequency);
        appendCell(row, crawlKeyword.score.toFixed(4));

        tbody.appendChild(row);
    }

    table.appendChild(tbody);

    keywordTableContainer.appendChild(table);
}
