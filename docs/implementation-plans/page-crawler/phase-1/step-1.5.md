# Step 1.5 — `OpenAiProvider`

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Step 1.1
**Blocks:** Step 1.6

---

## Goal

Implement the OpenAI provider supporting both standard API keys (GPT-4o) and Codex OAuth
tokens (experimental). The two auth types use different base URLs.

---

## File

`src/main/java/ai/provider/OpenAiProvider.java`

---

## Implementation Spec

### Auth-driven base URL selection

| Auth type | Base URL | Notes |
|-----------|----------|-------|
| `API_KEY` | `https://api.openai.com/v1/chat/completions` | Standard OpenAI |
| `AUTH_TOKEN` | `https://chatgpt.com/backend-api/codex/completions` | Codex OAuth |

```java
public OpenAiProvider(AuthConfig auth, String model) {
    this.auth     = auth;
    this.model    = model;
    this.endpoint = auth.getType() == AuthConfig.AuthType.AUTH_TOKEN
        ? "https://chatgpt.com/backend-api/codex/completions"
        : "https://api.openai.com/v1/chat/completions";
    this.http = HttpClient.newHttpClient();
}
```

### Request Body

```json
{
  "model": "{model}",
  "max_tokens": 4096,
  "messages": [
    { "role": "system", "content": "{systemPrompt}" },
    { "role": "user",   "content": "{userMessage}" }
  ]
}
```

When `base64Image` is non-null, the user message `content` becomes an array:
```json
"content": [
  { "type": "image_url", "image_url": { "url": "data:image/png;base64,{base64}" } },
  { "type": "text", "text": "{userMessage}" }
]
```

### Authentication Header

Both auth types use:
```
Authorization: Bearer {value}
```

### Response Parsing

```json
{
  "choices": [{ "message": { "content": "..." } }],
  "model": "gpt-4o-...",
  "usage": { "prompt_tokens": 1234, "completion_tokens": 567 }
}
```

Map to `AiResponse`:
- `choices[0].message.content` → `content`
- `model` → `model`
- `usage.prompt_tokens` → `inputTokens`
- `usage.completion_tokens` → `outputTokens`

### 401 Error Surface for Codex Auth

When `AUTH_TOKEN` is used and the response is 401:

```java
throw new RuntimeException(
    "Codex OAuth token expired or invalid. "
    + "Refresh it by running: codex login"
);
```

---

## Unit Test

**File:** (no separate test file required in MVP; cover via `AiProviderFactoryTest` switching.
Add `OpenAiProviderTest` if time permits.)

When added, test cases:
- `API_KEY` → endpoint is `api.openai.com`, `Authorization: Bearer` header present
- `AUTH_TOKEN` → endpoint is `chatgpt.com/backend-api/codex/...`
- 401 with `AUTH_TOKEN` → "Codex OAuth token expired" message in exception
- Vision input → `image_url` content block present
- Successful response → fields correctly mapped

---

## Definition of Done

- [ ] `OpenAiProvider` implements `AiProvider`
- [ ] Auth type determines endpoint URL correctly
- [ ] Both auth types use `Authorization: Bearer` header
- [ ] 401 with `AUTH_TOKEN` surfaces Codex-specific error message
- [ ] Vision input uses `image_url` content block
- [ ] `./gradlew compileJava` passes
