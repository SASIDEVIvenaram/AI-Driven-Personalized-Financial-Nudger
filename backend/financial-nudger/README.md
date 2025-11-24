# Financial Nudger Backend

Spring Boot service that ingests manual expenses, receipt images, and bank statements, classifies them with Google Gemini, and stores feedback so the model can respect user corrections.

## Prerequisites

- JDK 17+
- Maven Wrapper (bundled)
- MySQL running with the schema configured in `application.properties`
- Google Gemini API key (free tier is fine)

## Configuration

Set the Gemini key before running:

```powershell
$env:GEMINI_API_KEY="your-key"
```

Or add it to `application.properties` / environment variables. Relevant settings:

| Property | Description |
|----------|-------------|
| `gemini.api.key` | API key pulled from env |
| `gemini.model` | Defaults to `gemini-1.5-flash-latest` |
| `gemini.timeout-seconds` | HTTP timeout (default 45s) |
| `gemini.max-statement-chars` | Truncation length for PDF text |

## Running

```powershell
cd backend/financial-nudger
.\mvnw.cmd spring-boot:run
```

## API Walkthrough

Base path: `http://localhost:8080/api`

### 1. Manual Transaction + Auto Categorization

`POST /transactions/manual`

```json
{
  "userId": 1,
  "amount": 250.00,
  "note": "Bought medicines at Apollo pharmacy",
  "date": "2025-11-20"
}
```

The service calls Gemini to classify the note into the closest user/system category and stores the confidence score.

### 2. Upload Receipt Image/PDF

`POST /files/upload-receipt` (multipart form data)

Fields:

| Name | Type | Description |
|------|------|-------------|
| `file` | binary | Receipt image or PDF |
| `userId` | integer | Owning user |

Gemini receives the uploaded file + the user’s category list, extracts (date, merchant, amount, description, category) and creates a transaction.

### 3. Bank Statement PDF

`POST /files/upload-statement`

Same payload fields as receipt upload. Pipeline:

1. Extract raw text with PDFBox.
2. Gemini converts that text into JSON transactions.
3. Each transaction is re-classified via Gemini with the user’s feedback context.

### 4. Feedback Loop

`POST /transactions/{transactionId}/feedback`

```json
{
  "transactionId": 42,
  "userId": 1,
  "correctedCategoryName": "Medical"
}
```

This updates the transaction, stores a feedback record, and the Gemini prompts automatically include the latest corrections so future classifications drift toward the user’s preferences.

## Error Handling

- Missing/invalid inputs → `400` with details.
- Resources not found → `404`.
- Gemini issues (invalid key, quota) → `502 GEMINI_FAILURE`.

## Next Ideas

- Secure endpoints (Spring Security + JWT).
- Persist raw files for reprocessing.
- Add tests around the Gemini prompt builders.
- Build a simple frontend that hits these endpoints.

