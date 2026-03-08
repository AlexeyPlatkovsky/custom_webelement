# Step 0.2 — `build.gradle` Dependencies and Playwright Task

**Phase:** 0 — Infrastructure prerequisites
**Status:** Todo
**Depends on:** —
**Blocks:** Step 2.3 (`PageCrawler` requires Playwright on the classpath)

---

## Goal

Add the Playwright Java library and Jackson databind to the build, and register a Gradle
task that downloads the Playwright Chromium browser binaries.

---

## Location

`build.gradle`

---

## Changes

### New dependencies

Add to the `dependencies` block:

```groovy
// Playwright Java — DOM and accessibility tree extraction for PageCrawler
implementation 'com.microsoft.playwright:playwright:1.44.0'

// Jackson — JSON serialisation for AI provider request/response bodies
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
```

> **Note:** The `java.net.http.HttpClient` used for AI provider REST calls is part of the
> JDK (since Java 11) — no extra dependency is needed for HTTP.

### Playwright browser download task

Add after the `dependencies` block:

```groovy
tasks.register('installPlaywrightBrowsers', JavaExec) {
    group = 'playwright'
    description = 'Downloads Playwright Chromium browser binaries'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.microsoft.playwright.CLI'
    args = ['install', 'chromium']
}
```

---

## CI Integration

In any CI pipeline that runs the Page Crawler integration tests, add this step **before**
the test step:

```sh
./gradlew installPlaywrightBrowsers
```

Cache the Playwright browser directory between runs using the `PLAYWRIGHT_BROWSERS_PATH`
environment variable (defaults to `~/.cache/ms-playwright`):

```yaml
# Example GitHub Actions cache entry
- uses: actions/cache@v4
  with:
    path: ~/.cache/ms-playwright
    key: playwright-chromium-${{ hashFiles('build.gradle') }}
```

---

## Verification

After applying the changes:

```sh
./gradlew compileJava
./gradlew installPlaywrightBrowsers
```

Both commands must complete without errors before proceeding to Phase 2.

---

## Definition of Done

- [ ] `playwright:1.44.0` added to `build.gradle` `dependencies`
- [ ] `jackson-databind:2.17.0` added to `build.gradle` `dependencies`
- [ ] `installPlaywrightBrowsers` task registered and functional
- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew installPlaywrightBrowsers` downloads Chromium without errors
