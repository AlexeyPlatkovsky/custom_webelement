# PageCrawler for Page Objects

`PageCrawler` should be implemented as a supporting subsystem for AI-assisted Page Object generation and update. The real feature is not "crawl a page", but "turn a live page into a maintainable Java Page Object" for this framework.

## Goal

Given a URL, the feature should:

- generate a new Page Object, or
- update an existing Page Object against the current UI

Output must follow framework conventions: `iWebElement`, `iWebElementsList`, `iPageFactory`, `@FindBy`, optional `@CacheElement`, optional `@Waiter`, and readable locator naming.

## Core Design

Keep the feature as a small pipeline with hard boundaries:

```text
URL
 -> PageCrawler
 -> PageSnapshot
 -> PromptBuilder
 -> AiProvider
 -> Java source validation
 -> writer / update flow
```

Each stage should stay focused:

- `PageCrawler`: open page, wait, extract evidence
- `PageSnapshot`: hold normalized crawl data
- `PromptBuilder`: combine page evidence with framework rules
- `AiProvider`: provider-agnostic model call
- validator: check generated Java before writing
- writer/updater: save new source or prepare safe updates

This is better than one large "AI generator" class because crawl, prompt, and source-update failures are different problems.

## Recommended Technologies

- **Playwright Java** for crawling
  - Best fit for isolated headless browsing and accessibility snapshot support
  - Selenium should remain the automation runtime, not the crawler
- **DOM cleaner** via current cleaner or `jsoup`
  - Strip scripts, styles, comments, and noisy wrappers before prompt construction
- **Jackson or Gson**
  - Serialize accessibility tree into compact JSON
- **JavaParser**
  - Validate generated Java
  - Parse existing Page Objects in update mode
  - Enable safer AST-based updates later
- **Resource-based prompt templates**
  - Keep prompts in `src/main/resources`, versioned like code
- **Existing `AiProvider` abstraction**
  - Support OpenAI, Anthropic, Gemini, Ollama without feature-level branching

## Crawl Strategy

For MVP:

- launch isolated headless Chromium with Playwright
- navigate to URL
- wait for `networkidle`
- apply a short settle delay
- capture:
  - page title
  - final URL
  - cleaned HTML
  - accessibility tree
  - optional screenshot

`PageCrawler` should stay dumb and reliable: it returns evidence, not Java code.

## Prompt Strategy

The prompt should contain three inputs:

- framework rules
  - use `iWebElement` / `iWebElementsList`
  - use framework initialization rules
  - prefer semantic locators
  - return Java only
- page evidence
  - URL, title, cleaned DOM, accessibility tree, optional screenshot
- task mode
  - `create`: generate a new Page Object
  - `update`: update an existing Page Object conservatively

Accessibility data matters because it exposes roles, labels, and control names more clearly than raw DOM alone.

## Create vs Update

### Create mode

Use fresh crawl evidence to generate a compile-ready Page Object skeleton with meaningful fields and a small number of useful interaction methods.

### Update mode

This is the higher-value workflow. UI drift is constant in Selenium projects, so the system should update stale locators and add missing elements without destroying handwritten logic.

Rules for update mode:

1. Keep class name and public API stable by default.
2. Reuse valid fields where locator intent still matches.
3. Replace stale locators only when page evidence clearly supports it.
4. Preserve custom methods unless the page change invalidates them.
5. Avoid full-class rewrites.

Blind overwrite is the wrong model for update mode.

## Locator Heuristics

Bias toward stable, semantic selectors:

1. `id`, `name`, `data-*`, ARIA labels, accessible names
2. readable CSS selectors
3. text-based templated locators where appropriate
4. XPath only when better signals do not exist

Avoid deep absolute XPath, positional selectors, generated CSS classes, and layout-wrapper locators.

## Validation and Safety

Do not write raw AI output directly into production code.

Recommended flow:

1. extract Java source from model response
2. validate class/package/import shape
3. parse with `JavaParser`
4. write to staging/generated location or produce reviewable update output
5. compile-validate before adoption

For update mode, the long-term safer design is:

- AI proposes a structured update
- framework applies changes with AST awareness

That is safer than asking the model to regenerate an entire class.

## Constraints

- authenticated pages will need cookies, storage state, or pre-navigation hooks later
- SPAs may need custom waits beyond `networkidle`
- bot protection can make the crawler see the wrong page
- prompt size must stay compact enough for reliable generation

## Success Criteria

The feature is successful when it can:

1. generate a compile-ready Page Object from a public URL
2. follow framework-specific Page Object conventions
3. update an existing Page Object without discarding useful custom behavior
4. reduce routine maintenance on changing UIs
5. work with any configured `AiProvider`

## Summary

The right implementation is a layered pipeline:

- Playwright Java for evidence collection
- cleaned DOM + accessibility tree as AI context
- provider-agnostic generation through `AiProvider`
- `JavaParser` for validation and safer updates

`PageCrawler` should remain a crawl/evidence tool. The actual product is AI-assisted Page Object generation and conservative Page Object update.
