const form = document.getElementById("crawl-form");
const statusElement = document.getElementById("crawl-status");
const resultsElement = document.getElementById("crawl-results");

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
        statusElement.innerHTML = `
            <h2>Crawl Status</h2>
            <p>
                Failed to start crawl:
                ${error.message}
            </p>
        `;
    }
});

function renderCrawlStatus(crawl) {
    statusElement.innerHTML = "";

    const heading = document.createElement("h2");
    heading.textContent = "Crawl Status";
    statusElement.appendChild(heading);

    appendStatusValue(statusElement, "ID", crawl.id);
    appendStatusValue(statusElement, "Status", crawl.status);
    appendStatusValue(statusElement, "Start URL", crawl.startUrl);
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

                if (
                    crawl.status === "COMPLETED" ||
                    crawl.status === "STOPPED" ||
                    crawl.status === "FAILED"
                ) {
                    clearInterval(intervalId);
                }

            } catch (error) {
                clearInterval(intervalId);

                statusElement.innerHTML = `
                    <h2>Crawl Status</h2>
                    <p>
                        Failed to retrieve crawl status:
                        ${error.message}
                    </p>
                `;
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
    heading.textContent = "Crawl Results";
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
