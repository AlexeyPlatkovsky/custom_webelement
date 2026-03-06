# Configuration Reference

## webelement.properties

Located at `src/main/resources/webelement.properties`. Controls framework-level behavior.

| Key | Default | Description |
|-----|---------|-------------|
| `webelement.border.highlighted` | `false` | Highlight elements with a colored border during test execution (useful for debugging) |
| `webelement.border.width` | `3px` | Border width when highlighting is enabled |
| `webelement.border.color` | `red` | Border color when highlighting is enabled |

---

## ai-provider.properties

Located at project root. Controls the AI-QA Copilot layer. This file is safe to commit — sensitive values use `${ENV_VAR}` placeholders.

### Global

| Key | Values | Description |
|-----|--------|-------------|
| `ai.provider` | `anthropic` \| `gemini` \| `openai` \| `ollama` | The active provider. Only one is active at a time. |

### Anthropic

| Key | Example | Description |
|-----|---------|-------------|
| `ai.anthropic.auth.type` | `api_key` | Must be `api_key`. `auth_token` is not supported. |
| `ai.anthropic.auth.value` | `${ANTHROPIC_API_KEY}` | API key from console.anthropic.com |
| `ai.anthropic.model` | `claude-sonnet-4-20250514` | Model ID to use |

### Gemini

| Key | Example | Description |
|-----|---------|-------------|
| `ai.gemini.auth.type` | `api_key` or `auth_token` | `api_key` = Google AI Studio key; `auth_token` = ADC Bearer token |
| `ai.gemini.auth.value` | `${GEMINI_API_KEY}` | API key or ADC token |
| `ai.gemini.model` | `gemini-2.0-flash` | Model ID to use |

### OpenAI

| Key | Example | Description |
|-----|---------|-------------|
| `ai.openai.auth.type` | `api_key` or `auth_token` | `api_key` = standard API; `auth_token` = Codex OAuth (different base URL) |
| `ai.openai.auth.value` | `${OPENAI_API_KEY}` | API key or Codex OAuth token |
| `ai.openai.model` | `gpt-4o` | Model ID to use |

### Ollama (local)

| Key | Example | Description |
|-----|---------|-------------|
| `ai.ollama.base-url` | `http://localhost:11434` | Ollama server URL |
| `ai.ollama.model` | `qwen2.5-coder` | Model name (must be pulled locally: `ollama pull qwen2.5-coder`) |

No auth keys are needed for Ollama.

---

## Build-time System Properties

Passed via `-P<key>=<value>` to `./gradlew test`. All have defaults.

| Property | Default | Description |
|----------|---------|-------------|
| `base_url` | _(empty)_ | Base URL for tests that navigate to a configurable host |
| `browser_version` | `latest` | Browser version for WebDriverManager |
| `build_number` | _(timestamp)_ | Build identifier, used in Allure reports |
| `driver` | `CHROME` | Browser driver: `CHROME`, `FIREFOX`, `EDGE` |
| `lambda_key` | _(empty)_ | LambdaTest access key for remote execution |
| `lambda_username` | _(empty)_ | LambdaTest username |
| `locale` | `en-US` | Browser locale |
| `os` | `windows` | OS for remote execution config |
| `platform` | `Windows 10` | Platform string for remote execution |
| `screen_maximize` | `true` | Maximize browser window on start |
| `screen_resolution` | `1920,1080` | Screen resolution for remote execution |
| `suite` | `ui, unit` | TestNG groups to include |
| `thread_count` | `4` | Number of parallel test threads |
| `exclude` | `disabled` | TestNG groups to exclude |

---

## Environment Variables

The following environment variables are read via `${ENV_VAR}` expansion in `ai-provider.properties`:

| Variable | Provider | Description |
|----------|----------|-------------|
| `ANTHROPIC_API_KEY` | Anthropic | API key from console.anthropic.com |
| `GEMINI_API_KEY` | Gemini | API key from aistudio.google.com |
| `GEMINI_ADC_TOKEN` | Gemini | ADC Bearer token (`gcloud auth application-default print-access-token`) |
| `OPENAI_API_KEY` | OpenAI | API key from platform.openai.com |
| `CODEX_OAUTH_TOKEN` | OpenAI (Codex) | OAuth token from `~/.codex/auth.json` |

> **Note:** `${ENV_VAR}` expansion in `PropertyReader` is a planned feature — see [Known Limitations](../architecture/ai-provider-design.md#known-limitations-and-open-items).
