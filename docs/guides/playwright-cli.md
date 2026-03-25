# Playwright CLI Setup

The `ai-write-test` skill uses Playwright CLI to walk scenario paths and capture live DOM state. The CLI is purpose-built for AI coding agents — token-efficient and designed for step-by-step browser automation.

## 1. Install Node.js 18+

**macOS**
```bash
brew install node
```

**Windows**
```powershell
winget install OpenJS.NodeJS
```

Or download the installer for either platform from https://nodejs.org.

Verify:
```bash
node --version   # should print v18 or higher
```

## 2. Install Playwright CLI

Same command on both platforms:
```bash
npm install -g @playwright/cli@latest
```

Verify:
```bash
playwright-cli --help
```

If the global binary is unavailable, prefix commands with `npx`:
```bash
npx playwright-cli open https://example.com
```

## 3. Register Skills with Claude Code

```bash
playwright-cli install --skills
```

This registers the CLI's browser automation tools so Claude Code can invoke them directly during a skill run.

## 4. Pre-authorize Commands in `settings.json`

If your Claude Code setup uses command allow-lists, pre-authorize Playwright before running `ai-write-test`.

Add both the global binary and the documented `npx` fallback to `permissions.allow`:

```json
{
  "permissions": {
    "allow": [
      "Bash(playwright-cli:*)",
      "Bash(npx playwright-cli:*)"
    ]
  }
}
```

Both entries are required: some machines use the global `playwright-cli` binary, while others rely on `npx playwright-cli`.

## 5. Populate `.env`

Copy `.env.example` to `.env` and fill in credentials (only needed for authenticated scenarios):

```
TEST_USERNAME=your_username
TEST_PASSWORD=your_password
```

`.env` is listed in `.gitignore` and will not be committed.

When a scenario requires login, the `ai-write-test` skill reads the credentials from `.env`, fills the login form via the CLI, then saves the session state:

```bash
playwright-cli state-save auth.json   # saved after login
playwright-cli state-load auth.json   # restored on subsequent runs
```

Also ignore the saved storage-state file so session tokens are not committed:

```gitignore
auth.json
```

## 6. Verify `.gitignore`

```bash
git check-ignore -v .env
git check-ignore -v auth.json
```

Expected output should show `.gitignore` entries for both `.env` and `auth.json`.

## Fallback: HTML Paste

If the CLI cannot reach a page (auth wall, VPN-only environment, local dev server), paste the page HTML directly into the chat when prompted. The skill will extract locators from the pasted markup instead.

## Known Limits

| Scenario | Behaviour |
|---|---|
| Shadow DOM | Locator capture may be incomplete; review generated locators manually |
| Nested iframes | CLI may not cross frame boundaries; use HTML-paste fallback |
| Complex auth flows (SSO, MFA) | Log in manually first, save state with `playwright-cli state-save auth.json`, then resume the skill |
