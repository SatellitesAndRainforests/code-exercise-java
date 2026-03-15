
const API_BASE_URL = "http://localhost:8080";

const form = document.getElementById("shorten-form");
const fullUrlInput = document.getElementById("fullUrl");
const customAliasInput = document.getElementById("customAlias");
const successMessage = document.getElementById("success-message");
const errorMessage = document.getElementById("error-message");
const refreshButton = document.getElementById("refresh-button");
const urlsTableBody = document.getElementById("urls-table-body");


async function loadUrls() {

    try {

        // awaits a promise, state.pending, until fulfilled/rejected,
         // not blocking the ui while waiting.
        const response = await fetch(`${API_BASE_URL}/urls`);

        // 200-299 http success status codes
        if (!response.ok)  throw new Error("Failed to load URLs.");

        const urls = await response.json();
        renderUrls(urls);

    } catch (error) {
        renderUrls([]);
        showError(error.message || "Something went wrong while loading URLs.");
    }

}

function renderUrls(urls) {

    if (!urls.length) {
        urlsTableBody.innerHTML = `
            <tr>
                <td colspan="4">No shortened URLs found.</td>
            </tr>
        `;
        return;
    }

    urlsTableBody.innerHTML = urls.map((url) => `
        <tr>
            <td>${escapeHtml(url.alias)}</td>
            <td>
                <a href="${url.fullUrl}" target="_blank" rel="noopener noreferrer">
                    ${escapeHtml(url.fullUrl)}
                </a>
            </td>
            <td>
                <a href="${url.shortUrl}" target="_blank" rel="noopener noreferrer">
                    ${escapeHtml(url.shortUrl)}
                </a>
            </td>
            <td>
                <button type="button" class="delete-button" data-alias="${url.alias}">
                    Delete
                </button>
            </td>
        </tr>
    `).join("");

}

async function handleSubmit(event) {

    event.preventDefault();
    clearMessages();

    const fullUrl = fullUrlInput.value.trim();
    const customAlias = customAliasInput.value.trim();

    const payload = { fullUrl };

    if (customAlias) {
        payload.customAlias = customAlias;
    }

    try {

        const response = await fetch(`${API_BASE_URL}/shorten`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const isJson = response.headers.get("content-type")?.includes("application/json");
        const data = isJson ? await response.json() : null;

        if (!response.ok) {
        //check
            const message = data?.error || "Failed to shorten URL.";
            throw new Error(message);
        }

        showSuccess(`Short URL created: ${data.shortUrl}`);
        form.reset();
        await loadUrls();

    } catch (error) {
        showError(error.message || "Something went wrong while creating the short URL.");
    }

}

async function handleDeleteClick(event) {

    const button = event.target.closest(".delete-button");

    if (!button)  return;

    const alias = button.dataset.alias;

    try {
        const response = await fetch(`${API_BASE_URL}/${alias}`, {
            method: "DELETE"
        });

        if (!response.ok)   throw new Error(`Failed to delete alias: ${alias}`);

        showSuccess(`Deleted alias: ${alias}`);
        await loadUrls();

    } catch (error) {
        showError(error.message || "Something went wrong while deleting the short URL.");
    }

}



function showSuccess(message) {

    successMessage.textContent = message;
    successMessage.classList.remove("hidden");

    errorMessage.textContent = "";
    errorMessage.classList.add("hidden");

}

function showError(message) {

    errorMessage.textContent = message;
    errorMessage.classList.remove("hidden");

    successMessage.textContent = "";
    successMessage.classList.add("hidden");

}

function clearMessages() {

    successMessage.textContent = "";
    successMessage.classList.add("hidden");

    errorMessage.textContent = "";
    errorMessage.classList.add("hidden");

}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}


form.addEventListener("submit", handleSubmit);
refreshButton.addEventListener("click", loadUrls);
urlsTableBody.addEventListener("click", handleDeleteClick);

loadUrls();


