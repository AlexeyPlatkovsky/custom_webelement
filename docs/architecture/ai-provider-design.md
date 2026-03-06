# AI Provider Design

## Table of Contents

- [Supported Providers](#supported-providers)
- [AuthConfig](#authconfig)
- [AiProvider Interface](#aiprovider-interface)
- [Configuration: ai-provider.properties](#configuration-ai-providerproperties)
- [Per-Provider Auth Handling](#per-provider-auth-handling)
- [AiProviderFactory](#aiproviderfactory)
- [Obtaining Auth Tokens](#obtaining-auth-tokens)
- [Known Limitations and Open Items](#known-limitations-and-open-items)

---

## Supported Providers

| Provider | `api_key` | `auth_token` | Notes |
|----------|-----------|--------------|-------|
| Anthropic (Claude) | ✅ | ❌ | API key only. OAuth removed from third-party integrations per Anthropic ToS. |
| Gemini (Google AI) | ✅ | ✅ | `auth_token` = Application Default Credentials (ADC) Bearer token via `gcloud auth`. |
| OpenAI / Codex | ✅ | ✅ | `auth_token` = Codex OAuth token. Requires different base URL (ChatGPT backend). |
| Ollama (local) | — | — | No auth required. Local endpoint only. |

---

## AuthConfig

A unified auth object passed to every provider at initialization.

```java
public class AuthConfig {
    private AuthType type;   // API_KEY or AUTH_TOKEN
    private String value;    // the key or token value

    public enum AuthType {
        API_KEY,
        AUTH_TOKEN
    }
}
```

If a provider receives an unsupported `AuthType`, it throws `UnsupportedAuthException` at startup — failing fast before any test runs.

> **Open item:** consider `Supplier<String>` instead of `String value` to support token rotation (Gemini ADC tokens expire ~1h, Codex tokens are short-lived) without restarting the JVM. See [Known Limitations](#known-limitations-and-open-items).

---

## AiProvider Interface

```java
public interface AiProvider {
    AiResponse complete(AiRequest request);
}

public class AiRequest {
    private String systemPrompt;
    private String userMessage;
    private String base64Image;  // null if vision not needed
}

public class AiResponse {
    private String content;
    private String model;
    private int inputTokens;
    private int outputTokens;
}
```

`TestGenerator` and `FailureAnalyzer` interact **only** with `AiProvider` — they are completely decoupled from provider implementation details.

> **Note:** `AiRequest.base64Image` is nullable by convention. Not all providers / models support vision. A provider receiving a non-null image when unsupported should throw clearly rather than silently dropping it.

---

## Configuration: ai-provider.properties

Switch providers by changing a single line.

```properties
# Active provider: anthropic | gemini | openai | ollama
ai.provider=anthropic

# ── Anthropic ──────────────────────────────────────────
ai.anthropic.auth.type=api_key
ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
ai.anthropic.model=claude-sonnet-4-20250514

# ── Gemini ─────────────────────────────────────────────
# ai.provider=gemini
ai.gemini.auth.type=api_key
ai.gemini.auth.value=${GEMINI_API_KEY}
# Alternative: Google OAuth via ADC
# ai.gemini.auth.type=auth_token
# ai.gemini.auth.value=${GEMINI_ADC_TOKEN}
ai.gemini.model=gemini-2.0-flash

# ── OpenAI / Codex ─────────────────────────────────────
# ai.provider=openai
ai.openai.auth.type=api_key
ai.openai.auth.value=${OPENAI_API_KEY}
# Alternative: ChatGPT subscription via Codex OAuth
# ai.openai.auth.type=auth_token
# ai.openai.auth.value=${CODEX_OAUTH_TOKEN}
ai.openai.model=gpt-4o

# ── Ollama (local) ─────────────────────────────────────
# ai.provider=ollama
# No auth required
ai.ollama.base-url=http://localhost:11434
ai.ollama.model=qwen2.5-coder
```

> **Important:** `${ENV_VAR}` placeholders must be expanded at load time by `PropertyReader`. The current `PropertyReader` implementation does **not** expand environment variables — this must be added before any provider can authenticate.

---

## Per-Provider Auth Handling

### AnthropicProvider

```
API_KEY    → Header: "x-api-key: {value}"
AUTH_TOKEN → throws UnsupportedAuthException
             "Anthropic does not support auth_token. Use api_key."
```

### GeminiProvider

```
API_KEY    → Query param: ?key={value}
AUTH_TOKEN → Header: "Authorization: Bearer {value}"
             (ADC Bearer token from gcloud auth application-default login)
```

### OpenAiProvider

```
API_KEY    → Header: "Authorization: Bearer {value}"
             Base URL: api.openai.com

AUTH_TOKEN → Header: "Authorization: Bearer {value}"
             Base URL: chatgpt.com/backend-api/codex  ← Codex OAuth endpoint
```

When `auth_token` is used and the provider returns HTTP 401, `OpenAiProvider` surfaces:
> *"Codex OAuth token expired — run: `codex login`"*

### OllamaProvider

```
AuthConfig → ignored entirely
             Connects to ai.ollama.base-url with no authentication
```

---

## AiProviderFactory

Reads `ai-provider.properties`, constructs the correct `AuthConfig`, and returns the matching `AiProvider` implementation.

```java
public class AiProviderFactory {
    public static AiProvider create() {
        String provider = config.get("ai.provider");
        return switch (provider) {
            case "anthropic" -> new AnthropicProvider(loadAuth("anthropic"), loadModel("anthropic"));
            case "gemini"    -> new GeminiProvider(loadAuth("gemini"), loadModel("gemini"));
            case "openai"    -> new OpenAiProvider(loadAuth("openai"), loadModel("openai"));
            case "ollama"    -> new OllamaProvider(config.get("ai.ollama.base-url"), loadModel("ollama"));
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
```

---

## Obtaining Auth Tokens

### Gemini — Application Default Credentials (ADC)

```bash
# Install Google Cloud CLI, then:
gcloud auth application-default login

# Get the token:
gcloud auth application-default print-access-token

# Set environment variable:
export GEMINI_ADC_TOKEN=$(gcloud auth application-default print-access-token)
```

ADC tokens expire after ~1 hour. For longer sessions, `GeminiProvider` can use `google-auth-library-java` to handle token refresh automatically.

### OpenAI — Codex OAuth Token

```bash
# 1. Install Codex CLI
npm install -g @openai/codex

# 2. Log in (opens browser, completes OAuth flow)
codex login

# 3. Read the cached token
cat ~/.codex/auth.json
# → { "accessToken": "eyJ...", "refreshToken": "..." }

# 4. Export for use in properties
export CODEX_OAUTH_TOKEN="eyJ..."
```

Codex OAuth tokens are short-lived. See the 401 error message above for refresh instructions.

---

## Known Limitations and Open Items

| # | Issue | Impact | Suggested Fix |
|---|-------|--------|---------------|
| 1 | `PropertyReader` does not expand `${ENV_VAR}` | Auth values passed literally; all providers fail | Add env-var expansion step to `PropertyReader.load()` |
| 2 | `AuthConfig.value` is a plain `String` | Token rotation requires JVM restart | Replace with `Supplier<String>` |
| 3 | No retry / backoff in `AiProvider` | Transient 429 / 503 surface as test failures | Add `withRetry(int maxAttempts)` decorator or default method |
| 4 | `AiRequest.base64Image` is nullable | Callers must know which providers support vision | Use `Optional<String>` |
| 5 | `OpenAiProvider` branches on `AuthType` for base URL | Couples auth and endpoint concerns | Extract `CodexProvider` extends / wraps `OpenAiProvider` |
| 6 | AI-generated tests land directly in `src/test/java/generated/` | Bad generation breaks the build | Use a staging directory with explicit promotion |
