# Auth Setup

How to obtain credentials for each supported AI provider.

---

## Anthropic (Claude)

**Auth type:** `api_key` only.

1. Go to [console.anthropic.com](https://console.anthropic.com)
2. Navigate to **API Keys** → **Create Key**
3. Copy the key (shown once)
4. Set the environment variable:

   ```bash
   export ANTHROPIC_API_KEY=sk-ant-api03-...
   ```

5. In `ai-provider.properties`:

   ```properties
   ai.provider=anthropic
   ai.anthropic.auth.type=api_key
   ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
   ```

> Anthropic does not support `auth_token` for third-party integrations. See [ADR-002](../architecture/decisions.md#adr-002-no-oauth--auth_token-support-for-anthropic).

---

## Gemini (Google AI)

### Option A: API Key

1. Go to [aistudio.google.com](https://aistudio.google.com) → **Get API key**
2. Create or select a project, copy the key
3. Set the environment variable:

   ```bash
   export GEMINI_API_KEY=AIza...
   ```

4. In `ai-provider.properties`:

   ```properties
   ai.provider=gemini
   ai.gemini.auth.type=api_key
   ai.gemini.auth.value=${GEMINI_API_KEY}
   ```

### Option B: Application Default Credentials (ADC)

Suitable for Google Workspace users or CI environments with Workload Identity.

```bash
# Install Google Cloud CLI (https://cloud.google.com/sdk/docs/install)

# Authenticate
gcloud auth application-default login

# Print the current token
gcloud auth application-default print-access-token

# Set environment variable
export GEMINI_ADC_TOKEN=$(gcloud auth application-default print-access-token)
```

In `ai-provider.properties`:

```properties
ai.provider=gemini
ai.gemini.auth.type=auth_token
ai.gemini.auth.value=${GEMINI_ADC_TOKEN}
```

> ADC tokens expire after ~1 hour. Re-run `gcloud auth application-default print-access-token` to refresh. Automated token refresh via `google-auth-library-java` is planned.

---

## OpenAI

### Option A: API Key

1. Go to [platform.openai.com](https://platform.openai.com) → **API Keys** → **Create new secret key**
2. Copy the key
3. Set the environment variable:

   ```bash
   export OPENAI_API_KEY=sk-proj-...
   ```

4. In `ai-provider.properties`:

   ```properties
   ai.provider=openai
   ai.openai.auth.type=api_key
   ai.openai.auth.value=${OPENAI_API_KEY}
   ```

### Option B: Codex OAuth Token

Requires an active ChatGPT Plus or Pro subscription.

```bash
# 1. Install Codex CLI
npm install -g @openai/codex

# 2. Log in (opens browser, completes OAuth flow)
codex login

# 3. Read the cached token
cat ~/.codex/auth.json
# → { "accessToken": "eyJ...", "refreshToken": "..." }

# 4. Export the access token
export CODEX_OAUTH_TOKEN=$(cat ~/.codex/auth.json | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
```

In `ai-provider.properties`:

```properties
ai.provider=openai
ai.openai.auth.type=auth_token
ai.openai.auth.value=${CODEX_OAUTH_TOKEN}
```

> Codex OAuth tokens are short-lived. If requests return HTTP 401, run `codex login` again to refresh.

---

## Ollama (local)

No authentication required.

1. Install Ollama from [ollama.com](https://ollama.com)
2. Pull the model:

   ```bash
   ollama pull qwen2.5-coder
   ```

3. Start the server (runs on `localhost:11434` by default):

   ```bash
   ollama serve
   ```

4. In `ai-provider.properties`:

   ```properties
   ai.provider=ollama
   ai.ollama.base-url=http://localhost:11434
   ai.ollama.model=qwen2.5-coder
   ```

Other models that work well for code tasks: `codellama`, `deepseek-coder`, `mistral`.

---

## Security Notes

- Never commit API keys or tokens to version control.
- Add `ai-provider.properties` to `.gitignore` if you store values directly (not recommended); prefer `${ENV_VAR}` placeholders.
- Rotate keys immediately if accidentally exposed.
- For CI/CD pipelines, store secrets in your pipeline's secret manager (GitHub Actions Secrets, GitLab CI Variables, etc.) and inject them as environment variables.
