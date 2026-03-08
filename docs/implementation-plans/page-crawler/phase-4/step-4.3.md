# Step 4.3 — Batch Summary Logging

**Phase:** 4 — Validation and polish
**Status:** Todo
**Depends on:** Step 3.5 (`PageCrawlerFacade` full wiring)
**Blocks:** —

---

## Goal

After all URLs in a batch are processed, emit a structured summary log that shows which
Page Objects were generated successfully and which failed. This provides immediate
feedback without requiring the developer to scroll through individual log lines.

---

## Location

`PageCrawlerFacade.logBatchSummary()` — already specified in Step 3.5.

---

## Output Format

```
PageCrawlerFacade: batch complete — 2/3 page objects generated
  OK  LoginPage <- https://example.com/login
  OK  DashboardPage <- https://example.com/dashboard
  ERR https://example.com/checkout [FAILED: timeout after 30000ms]
```

- `OK` lines logged at INFO
- `ERR` lines logged at ERROR
- Summary line (`N/total page objects generated`) logged at INFO
- Logged whether the batch fully succeeded or partially failed

---

## Implementation

Already defined in Step 3.5:

```java
private static void logBatchSummary(List<String> succeeded, List<String> failed) {
    int total = succeeded.size() + failed.size();
    iLogger.info(String.format(
        "PageCrawlerFacade: batch complete — %d/%d page objects generated",
        succeeded.size(), total
    ));
    succeeded.forEach(s -> iLogger.info("  OK  " + s));
    failed.forEach(f   -> iLogger.error("  ERR " + f));
}
```

---

## Behaviour for Single-URL Calls

`generatePageObject(String url)` delegates to `generatePageObjects(List.of(url))`, so
the summary is also emitted for single-URL calls:

```
PageCrawlerFacade: batch complete — 1/1 page objects generated
  OK  LoginPage <- https://example.com/login
```

This is acceptable — the single-line summary adds negligible noise and keeps the code path
consistent.

---

## Definition of Done

- [ ] Batch summary logged after every `generatePageObjects()` call
- [ ] Summary correctly reflects counts of succeeded and failed URLs
- [ ] OK lines at INFO, ERR lines at ERROR
- [ ] Summary emitted even when all URLs fail (0/N)
- [ ] Summary emitted for single-URL calls via `generatePageObject()`
- [ ] Integration test in `PageCrawlerFacadeTest` verifies summary is logged
