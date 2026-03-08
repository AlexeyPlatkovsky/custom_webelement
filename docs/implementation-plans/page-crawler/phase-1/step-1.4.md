# Step 1.4 — `GeminiProvider`

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Step 1.1
**Blocks:** Step 1.6

---

## Goal

Implement the Google Gemini provider supporting both API key and Application Default
Credentials (AUTH_TOKEN) authentication modes.

---

## File

`src/main/java/ai/provider/GeminiProvider.java`

---

## Implementation Spec

### Constructor

Both auth types are supported:

```java
public GeminiProvider(AuthConfig auth, String model) {
    this.auth  = auth;
    this.model = model;
    this.http  = HttpClient.newHttpClient();
}
```

### Endpoint

`POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`

### Authentication

| Auth type | Mechanism |
|-----------|-----------|
| `API_KEY` | Append `?key={value}` query parameter to the URL |
| `AUTH_TOKEN` | Add `Authorization: Bearer {value}` header |

### Request Body

```json
{
  "system_instruction": {
    "parts": [{ "text": "{systemPrompt}" }]
  },
  "contents": [
    {
      "role": "user",
      "parts": [{ "text": "{userMessage}" }]
    }
  ],
  "generationConfig": {
    "maxOutputTokens": 4096
  }
}
```

When `base64Image` is non-null, add an inline image part to the user `parts` array:

```json
{ "inline_data": { "mime_type": "image/png", "data": "{base64}" } }
```

### Response Parsing

```json
{
  "candidates": [
    { "content": { "parts": [{ "text": "..." }] } }
  ],
  "usageMetadata": {
    "promptTokenCount": 1234,
    "candidatesTokenCount": 567
  }
}
```

Map to `AiResponse`:
- `candidates[0].content.parts[0].text` → `content`
- `model` property is not returned by Gemini; use the configured model name
- `usageMetadata.promptTokenCount` → `inputTokens`
- `usageMetadata.candidatesTokenCount` → `outputTokens`

### Error handling

- HTTP 400 (invalid request) → throw with status + body
- HTTP 401/403 (auth failure) → throw with message: `"Gemini auth failed. Check GEMINI_API_KEY or refresh your ADC token."`
- HTTP 429 (quota exceeded) → throw with status + body

---

## Unit Test

**File:** (no separate test file required for Gemini in MVP; covered by `AiProviderFactoryTest`
property-switching test if Gemini is mocked. Add `GeminiProviderTest` if time permits.)

When added, test cases:
- `API_KEY` auth → `?key=` appended to URL, no `Authorization` header
- `AUTH_TOKEN` auth → `Authorization: Bearer` header set, no `?key=` param
- Successful response → fields correctly mapped
- Vision input → inline_data part present in request

---

## Definition of Done

- [ ] `GeminiProvider` implements `AiProvider`
- [ ] `API_KEY` uses query param; `AUTH_TOKEN` uses `Authorization` header
- [ ] Request body matches Gemini `generateContent` schema
- [ ] Vision input adds `inline_data` part when `base64Image` is non-null
- [ ] Non-200 responses throw with meaningful messages
- [ ] `./gradlew compileJava` passes
