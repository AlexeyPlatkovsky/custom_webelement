# Step 0.3 — `ai-provider.properties` Configuration Template

**Phase:** 0 — Infrastructure prerequisites
**Status:** Todo
**Depends on:** —
**Blocks:** Step 1.6 (`AiProviderFactory` reads from this file)

---

## Goal

Create the `ai-provider.properties` configuration file that controls which AI provider is
active and supplies per-provider settings. The file is safe to commit; secrets are kept in
environment variables and expanded at runtime by `PropertyReader` (Step 0.1).

---

## Location

`src/main/resources/ai-provider.properties`

---

## File Content

```properties
# ─────────────────────────────────────────────────────────────────────────────
# AI Provider configuration for custom_webelement AI-QA Copilot
# Active provider: anthropic | gemini | openai | ollama
# ─────────────────────────────────────────────────────────────────────────────
ai.provider=anthropic

# ── Anthropic (Claude) ───────────────────────────────────────────────────────
# auth.type: api_key only (AUTH_TOKEN not supported — ADR-002)
ai.anthropic.auth.type=api_key
ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
ai.anthropic.model=claude-sonnet-4-6

# ── Google Gemini ─────────────────────────────────────────────────────────────
# auth.type: api_key | auth_token (Application Default Credentials)
ai.gemini.auth.type=api_key
ai.gemini.auth.value=${GEMINI_API_KEY}
ai.gemini.model=gemini-2.0-flash

# ── OpenAI ────────────────────────────────────────────────────────────────────
# auth.type: api_key | auth_token (Codex OAuth token)
ai.openai.auth.type=api_key
ai.openai.auth.value=${OPENAI_API_KEY}
ai.openai.model=gpt-4o

# ── Ollama (local, no auth required) ─────────────────────────────────────────
ai.ollama.base-url=http://localhost:11434
ai.ollama.model=qwen2.5-coder
```

---

## Notes

- `${ENV_VAR}` tokens are expanded by `PropertyReader` at load time (Step 0.1).
- Secrets (`ANTHROPIC_API_KEY`, `GEMINI_API_KEY`, `OPENAI_API_KEY`) must be set as
  environment variables; they are **never** written into this file.
- To switch providers without code changes, update `ai.provider` and ensure the
  corresponding `auth.value` environment variable is set.
- Ollama requires no API key — set `ai.provider=ollama` and ensure the Ollama service is
  running at `ai.ollama.base-url`.
- See `docs/guides/auth-setup.md` for instructions on obtaining tokens for each provider.

---

## Definition of Done

- [ ] `src/main/resources/ai-provider.properties` created with content above
- [ ] File contains no real API keys or secrets
- [ ] File is on the compile classpath (verified via `./gradlew processResources`)
