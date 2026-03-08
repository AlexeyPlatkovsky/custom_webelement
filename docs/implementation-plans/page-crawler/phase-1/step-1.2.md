# Step 1.2 — `AnthropicProvider`

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Step 1.1
**Blocks:** Step 1.6 (`AiProviderFactory` must have at least one provider to wire)

---

## Goal

Implement the Anthropic Claude provider using the Anthropic Messages API. This is the
primary provider used in most team environments.

---

## File

`src/main/java/ai/provider/AnthropicProvider.java`

---

## Implementation Spec

### Constructor validation

Throw `UnsupportedAuthException` immediately if `AuthType.AUTH_TOKEN` is passed — Anthropic
only supports API keys (see ADR-002).

```java
public AnthropicProvider(AuthConfig auth, String model) {
    if (auth.getType() == AuthConfig.AuthType.AUTH_TOKEN) {
        throw new UnsupportedAuthException("anthropic", auth.getType());
    }
    this.apiKey = auth.getValue();
    this.model  = model;
    this.http   = HttpClient.newHttpClient();
}
```

### HTTP Request

- **Endpoint:** `POST https://api.anthropic.com/v1/messages`
- **Headers:**
  - `x-api-key: {apiKey}`
  - `anthropic-version: 2023-06-01`
  - `content-type: application/json`
- **Body:**
```json
{
  "model": "{model}",
  "max_tokens": 4096,
  "system": "{systemPrompt}",
  "messages": [
    { "role": "user", "content": "{userMessage}" }
  ]
}
```

When `request.getBase64Image()` is non-null, the `content` field becomes an array:
```json
"content": [
  { "type": "image", "source": { "type": "base64", "media_type": "image/png", "data": "{base64}" } },
  { "type": "text",  "text": "{userMessage}" }
]
```

### Response Parsing

Parse the JSON response with Jackson. Extract:
- `content[0].text` → `AiResponse.content`
- `model` → `AiResponse.model`
- `usage.input_tokens` → `AiResponse.inputTokens`
- `usage.output_tokens` → `AiResponse.outputTokens`

Throw `RuntimeException` wrapping the raw response body if:
- HTTP status is not 200
- `content` array is empty
- JSON parsing fails

### Error message on non-200

```java
throw new RuntimeException(String.format(
    "Anthropic API returned %d: %s", response.statusCode(), response.body()
));
```

---

## Unit Test

**File:** `src/test/java/unit_tests/ai/provider/AnthropicProviderTest.java`

Use Mockito to mock `HttpClient` and `HttpResponse`. Test cases:

- Successful response → `AiResponse` fields match parsed values
- `AUTH_TOKEN` at construction → `UnsupportedAuthException` thrown immediately
- HTTP 401 response → `RuntimeException` with status code in message
- HTTP 429 (rate limit) → `RuntimeException` with status code in message
- Response with `base64Image` non-null → request body contains image content block
- Verify `x-api-key` header is present in the outgoing request

---

## Definition of Done

- [ ] `AnthropicProvider` implements `AiProvider`
- [ ] `AUTH_TOKEN` rejected at construction
- [ ] Request headers and body match spec
- [ ] Vision input included when `base64Image` is non-null
- [ ] Non-200 responses throw with status code
- [ ] All unit tests pass
