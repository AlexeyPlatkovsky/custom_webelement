# Step 1.3 — `OllamaProvider`

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Step 1.1
**Blocks:** Step 1.6

> **Recommended starting point.** Ollama requires no API key, no ENV variable expansion,
> and no external account. Implementing it first lets you validate the full data flow
> (crawler → prompt → AI → file write) locally before touching any auth logic.

---

## Goal

Implement the Ollama provider for local, keyless AI inference. Uses the OpenAI-compatible
chat endpoint that Ollama exposes at `http://localhost:11434` (configurable).

---

## File

`src/main/java/ai/provider/OllamaProvider.java`

---

## Implementation Spec

### Constructor

Auth is ignored entirely — Ollama has no authentication.

```java
public OllamaProvider(String baseUrl, String model) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.model   = model;
    this.http    = HttpClient.newHttpClient();
}
```

### HTTP Request

- **Endpoint:** `POST {baseUrl}api/chat`
- **Headers:** `content-type: application/json`
- **Body (OpenAI-compatible format):**
```json
{
  "model": "{model}",
  "stream": false,
  "messages": [
    { "role": "system",  "content": "{systemPrompt}" },
    { "role": "user",    "content": "{userMessage}" }
  ]
}
```

> `stream: false` is required so the response is a single JSON object rather than a
> server-sent event stream.

### Response Parsing

Ollama's `/api/chat` response structure:
```json
{
  "model": "qwen2.5-coder",
  "message": { "role": "assistant", "content": "..." },
  "prompt_eval_count": 1234,
  "eval_count": 567
}
```

Map to `AiResponse`:
- `message.content` → `content`
- `model` → `model`
- `prompt_eval_count` → `inputTokens`
- `eval_count` → `outputTokens`

> Fields `prompt_eval_count` / `eval_count` may be absent for some Ollama builds.
> Default to `0` if missing — do not throw.

### Error handling

- Connection refused (Ollama not running): `ConnectException` → wrap in `RuntimeException`
  with message: `"Ollama is not running at {baseUrl}. Start it with: ollama serve"`
- Non-200 response: throw `RuntimeException` with status + body

---

## Unit Test

**File:** `src/test/java/unit_tests/ai/provider/OllamaProviderTest.java`

Test cases:
- Successful response → `AiResponse` fields match parsed values
- Missing `prompt_eval_count` in response → `inputTokens` defaults to `0`, no exception
- Non-200 response → `RuntimeException` with status code
- Base URL with trailing slash → endpoint still correct (no double slash)
- Base URL without trailing slash → endpoint still correct

---

## Definition of Done

- [ ] `OllamaProvider` implements `AiProvider`
- [ ] Auth entirely ignored (no `AuthConfig` parameter needed)
- [ ] `stream: false` always set in request body
- [ ] Missing token count fields default to `0` without throwing
- [ ] Connection refused error surfaces a clear "Ollama not running" message
- [ ] All unit tests pass
- [ ] Manual smoke test: Ollama running locally → `OllamaProvider.complete()` returns a response
