const form = document.getElementById("crawl-form");
const statusElement = document.getElementById("crawl-status");

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
    statusElement.innerHTML = `
        <h2>Crawl Status</h2>

        <p>
            <strong>ID:</strong>
            ${crawl.id}
        </p>

        <p>
            <strong>Status:</strong>
            ${crawl.status}
        </p>

        <p>
            <strong>Start URL:</strong>
            ${crawl.startUrl}
        </p>
    `;
}

function startPolling(crawlId) {
    const intervalId = setInterval(
        async () => {
            try {
                const response = await fetch(
                    `/api/crawls/${crawlId}`
                );

                if (!response.ok) {
                    clearInterval(intervalId);
                    return;
                }

                const crawl =
                    await response.json();

                renderCrawlStatus(crawl);

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
