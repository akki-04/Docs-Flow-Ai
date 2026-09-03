const pdfInput = document.getElementById("pdfInput");
const fileName = document.getElementById("fileName");
const analyzeBtn = document.getElementById("analyzeBtn");

const uploadSection = document.getElementById("uploadSection");
const querySection = document.getElementById("querySection");

const documentName = document.getElementById("documentName");
const newDocumentBtn = document.getElementById("newDocumentBtn");

const questionInput = document.getElementById("questionInput");
const askBtn = document.getElementById("askBtn");

const answerBox = document.getElementById("answerBox");
const answerText = document.getElementById("answerText");

// Spring Boot backend
const BACKEND_URL = "http://localhost:8080";

let selectedFile = null;


// ==========================================
// PDF SELECT
// ==========================================

pdfInput.addEventListener("change", function () {

    selectedFile = this.files[0];

    if (!selectedFile) {
        return;
    }

    if (selectedFile.type !== "application/pdf") {

        alert("Please select a PDF file.");

        selectedFile = null;
        pdfInput.value = "";

        return;
    }

    fileName.textContent =
        "Selected: " + selectedFile.name;

    analyzeBtn.disabled = false;
});


// ==========================================
// ANALYZE DOCUMENT
// ==========================================

analyzeBtn.addEventListener("click", function () {

    if (!selectedFile) {
        alert("Please select a PDF first.");
        return;
    }

    console.log(
        "Selected file:",
        selectedFile.name
    );

    analyzeBtn.textContent = "Analyzing...";
    analyzeBtn.disabled = true;

    // Currently only switching UI.
    // Actual PDF upload to backend will be connected next.

    setTimeout(() => {

        uploadSection.classList.add("hidden");

        querySection.classList.remove("hidden");

        documentName.textContent =
            selectedFile.name;

        analyzeBtn.textContent =
            "Analyze Document";

    }, 1000);
});


// ==========================================
// NEW DOCUMENT
// ==========================================

newDocumentBtn.addEventListener("click", function () {

    selectedFile = null;

    pdfInput.value = "";

    fileName.textContent = "";

    questionInput.value = "";

    answerText.textContent = "";

    answerBox.classList.add("hidden");

    querySection.classList.add("hidden");

    uploadSection.classList.remove("hidden");

    analyzeBtn.textContent =
        "Analyze Document";

    analyzeBtn.disabled = true;
});


// ==========================================
// ASK AI
// ==========================================

askBtn.addEventListener("click", async function () {

    console.log("ASK AI BUTTON CLICKED");

    const question =
        questionInput.value.trim();

    if (!question) {

        alert("Please enter a question.");

        return;
    }

    answerBox.classList.remove("hidden");

    answerText.textContent =
        "Thinking...";

    askBtn.disabled = true;

    try {

        console.log(
            "Sending query:",
            question
        );

        const response = await fetch(
            `${BACKEND_URL}/api/query`,
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({
                    query: question
                })
            }
        );

        console.log(
            "Response status:",
            response.status
        );

        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                `Server Error (${response.status}): ${errorText}`
            );
        }

        const data =
            await response.text();

        console.log(
            "Server response:",
            data
        );

        answerText.textContent = data;

    } catch (error) {

        console.error(
            "Query error:",
            error
        );

        answerText.textContent =
            "Error: " + error.message;

    } finally {

        askBtn.disabled = false;
    }
});