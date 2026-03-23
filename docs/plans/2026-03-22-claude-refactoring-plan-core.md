# Core Package Refactoring Plan

**Branch:** `refactor/core-package-improvements`
**Based on:** `main` by default; use another base branch only if the user explicitly requests it
**Scope:** `src/main/java/core/`

---

## Overview

15 issues identified across the core package, grouped into 7 sequential steps.
Steps are ordered by risk and dependency: thread-safety and correctness first,
design cleanups second, style last.

Each step should be independently compilable and testable.

---

## Step 1 - Fix thread-safety in `DriverCaps` (Critical)

**Files:** `core/driver/DriverCaps.java`

**Problem:** `capabilities` is a `static` mutable field written and read in
`getCaps()`. In parallel test runs, thread A can overwrite the value while
thread B is returning it.

**Change:** Remove the static field; use a local variable instead.

```java
// Before
private static MutableCapabilities capabilities;

public static MutableCapabilities getCaps(DriverNames driverName) {
    switch (driverName) {
        case CHROME -> capabilities = getChromeCaps();
        ...
    }
    iLogger.info("Driver options are : {}", capabilities);
    return capabilities;
}

// After
public static MutableCapabilities getCaps(DriverNames driverName) {
    MutableCapabilities capabilities = switch (driverName) {
        case CHROME  -> getChromeCaps();
        case FIREFOX -> getFirefoxCaps();
        case LAMBDA  -> getLambdaCaps();
    };
    iLogger.info("Driver options are : {}", capabilities);
    return capabilities;
}
```

**Also fix in this step:**
- Rename `getFirefoxCas()` -> `getFirefoxCaps()` (typo)
- Make `getChromeCaps()`, `getFirefoxCaps()`, `getLambdaCaps()` package-private
  (only called internally)
- Scrub `user` and `accessKey` from the Lambda capabilities log

**Validation:** `./gradlew compileJava` + `./gradlew checkstyleMain`

---

## Step 2 - Fix thread-safety in `DriverFactory` and remove TestNG coupling (Critical)

**Files:** `core/driver/DriverFactory.java`

**Problem A:** `driverName` is a `static` field but `DRIVER` is `ThreadLocal`.
If thread A calls `initDriver()` and thread B reads `driverName()` concurrently,
it can read the wrong driver type.

**Problem B:** `Assert.fail()` is called from infrastructure code, coupling the
driver layer to TestNG.

**Problem C:** `configureWebDriverManagerCache()` calls `System.setProperty`
(global, not thread-safe) and `mkdirs()` on every `initDriver()` call.

**Changes:**

```java
// A: make driverName ThreadLocal
private static final ThreadLocal<DriverNames> DRIVER_NAME = new ThreadLocal<>();

public static DriverNames driverName() {
    return DRIVER_NAME.get();
}

// B: replace Assert.fail with IllegalStateException
} catch (Exception e) {
    throw new IllegalStateException("Remote WebDriver creation failed: " + e.getMessage(), e);
}

// C: move cache setup to a static initializer, run once
static {
    configureWebDriverManagerCache();
}
```

**Also fix in this step:**
- Add `DRIVER_NAME.remove()` alongside `DRIVER.remove()` in `disposeCurrentDriver()`

**Validation:** `./gradlew compileJava` + `./gradlew test -Dsuite=unit`

---

## Step 3 - Simplify `CacheValue` without changing cache semantics (High)

**Files:** `core/tools/CacheValue.java`

**Problem:** `getRule` is always `() -> null`, so the auto-populate path is
broken. The `get(Supplier<T>)` overload also ignores its argument during
re-population. In actual framework usage, `CacheValue` is populated via
`setForce()` and then reused.

**Change:** Remove the broken auto-populate path, but keep the current
"cache after first lookup and reuse it" behavior. Do not add cache invalidation
in this refactor unless separate call sites and tests are introduced.

```java
public class CacheValue<T> {

    private static final ThreadLocal<Long> globalCache = new ThreadLocal<>();
    private long elementCache = 0;
    private T value;

    private static long getGlobalCache() {
        Long v = globalCache.get();
        if (v == null) {
            globalCache.set(0L);
            return 0L;
        }
        return v;
    }

    public T get() {
        return value;
    }

    public void setForce(T value) {
        this.value = value;
        this.elementCache = getGlobalCache();
    }

    public boolean hasValue() {
        return isUseCache() && value != null && elementCache == getGlobalCache();
    }

    public boolean isUseCache() {
        return elementCache > -1;
    }
}
```

**Validation:** `./gradlew test -Dsuite=unit` + `./gradlew compileTestJava`

---

## Step 4 - Fix `iWebElementsList` List contract and item resolution (High)

**Files:** `core/web/iWebElementsList.java`

**Problem A:** Stub `List` methods (`add`, `remove`, `containsAll`, etc.)
return `false` or `null` silently, violating the `List` contract and hiding bugs.

**Problem B:** `:nth-child(n)` selects elements that are the nth child of their
parent node, not the nth element matching the selector globally. This produces
wrong results for real-world lists.

**Problem C:** `getChildWithText()` can fail on the first non-matching element
because child lookup is eager. Keep `SkipException` as the framework policy, but
fix the search logic so all candidates are checked before skipping.

**Changes:**

A - Replace all stub methods with `UnsupportedOperationException`:

```java
@Override
public boolean add(iWebElement webElement) {
    throw new UnsupportedOperationException("iWebElementsList is read-only");
}
```

B - Stop synthesizing per-index locators from CSS/XPath strings. Resolve the
list once with `driver.findElements(getLocator())` and wrap the returned
`WebElement` instances:

```java
private List<iWebElement> getWebElements() {
    List<WebElement> found = getDriver().findElements(getLocator());
    List<iWebElement> wrapped = new ArrayList<>(found.size());
    for (WebElement element : found) {
        wrapped.add(new iWebElement(driver, name, getLocator(), element));
    }
    return wrapped;
}
```

C - Rework `getChildWithText()` to search all elements first, then keep
`SkipException` if none match:

```java
public iWebElement getChildWithText(String expectedText) {
    for (iWebElement element : getWebElements()) {
        if (!element.findElements(By.xpath(".//*[text()='" + expectedText + "']")).isEmpty()) {
            return element;
        }
    }
    throw new SkipException("No child with expected text: " + expectedText);
}
```

**Validation:** `./gradlew compileJava` + targeted UI smoke test with a list-based page object

---

## Step 5 - Fix `iWebElement` correctness issues (High)

**Files:** `core/web/iWebElement.java`

**Problem A:** `getText()` has a redundant final branch that repeats
`el.getText()` after it has already been checked.

**Problem B:** `setFocus()` is called at the start of `getText()`, causing two
separate `getWebElement()` calls before the actual element read.

**Problem C:** `selectTextInElement()` injects the raw locator string into
`document.evaluate()` (XPath API). CSS selectors will silently select nothing.

**Changes:**

A - Remove the dead final branch in `getText()`.

B - Eliminate redundant lookups in `getText()` by inlining focus logic around a
single `WebElement` lookup:

```java
public String getText() {
    WebElement el = getWebElement();
    executeScript("arguments[0].scrollIntoView(true);", el);
    executeScript("arguments[0].focus();", el);
    String text = el.getText();
    String value = el.getAttribute("value");
    if (!isBlank(text)) {
        return text;
    }
    if (!isBlank(value)) {
        return value;
    }
    stopHighlight(el);
    return "";
}
```

C - Guard `selectTextInElement()` against non-XPath locators:

```java
public void selectTextInElement() {
    if (!(byLocator instanceof By.ByXPath)) {
        throw new UnsupportedOperationException(
            "selectTextInElement() only supports XPath locators; got: "
                + byLocator.getClass().getSimpleName()
        );
    }
    ...
}
```

**Docs impact:** update `docs/guides/writing-pages.md` to document the XPath-only
constraint if Step 5C is applied.

**Validation:** `./gradlew compileJava` + `./gradlew test -Dsuite=unit`

---

## Step 6 - Fix `HiddenElementCondition` misuse of `ExpectedCondition` (High)

**Files:** `core/web/conditions/HiddenElementCondition.java`

**Problem:** `ExpectedCondition` is a predicate. Performing a click inside
`apply()` means the click is repeated on every polling interval until the
element responds. The class also uses the raw `ExpectedCondition` type and
pulls the driver from `DriverFactory` instead of the wait callback.

**Change:** Keep the current retry-until-timeout behavior, but make the class
typed and use the `WebDriver` instance passed to `apply()`.

```java
public class HiddenElementCondition implements ExpectedCondition<Boolean> {

    private final WebElement element;

    @Override
    public Boolean apply(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            js.executeScript("arguments[0].focus();", element);
            try {
                element.click();
            } catch (WebDriverException ex) {
                element.sendKeys(Keys.RETURN);
            }
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }
}
```

**Validation:** `./gradlew compileJava` + targeted UI smoke test verifying click on an obscured element

---

## Step 7 - Style and minor fixes (Low)

**Files:** multiple

Apply these small, safe fixes in a single commit.

| File | Fix |
|---|---|
| `ReflectionUtils.java:41` | `List<Class>` -> `List<Class<?>>` (raw type warning) |
| `ReflectionUtils.java:50` | Pass original exception as cause: `new RuntimeException(msg, ex)` |
| `Environment.java:18` | String concat in logger -> `iLogger.info("Get root URL: {}", rootUrl)` |
| `Environment.java:14` | Document or fix the regex; verify it matches the actual `ROOT_URL` format |
| `iPage.java:16` | Rename constant to `MISSING_PAGE_URL_ANNOTATION` (message still says `@RelativeURL`) |
| `iPageFactory.java` | Consider delegating `findByToBy()` to Selenium's `Annotations.buildBy()` to avoid missing `how` / `using` handling |

**Validation:** `./gradlew checkstyleMain` + `./gradlew compileJava`

---

## Execution Order Summary

| # | Step | Files | Risk | Effort |
|---|---|---|---|---|
| 1 | DriverCaps thread-safety + typo | `DriverCaps.java` | Low | Small |
| 2 | DriverFactory thread-safety + TestNG decoupling | `DriverFactory.java` | Low | Small |
| 3 | CacheValue cleanup without semantic change | `CacheValue.java` | Medium | Small |
| 4 | iWebElementsList contract + item resolution fix | `iWebElementsList.java` | Medium | Medium |
| 5 | iWebElement correctness | `iWebElement.java` | Medium | Small |
| 6 | HiddenElementCondition typing + driver decoupling | `HiddenElementCondition.java` | Low | Small |
| 7 | Style fixes | multiple | Low | Small |

Run `./gradlew checkstyleMain` and the smallest relevant test set after each step
before moving to the next.
