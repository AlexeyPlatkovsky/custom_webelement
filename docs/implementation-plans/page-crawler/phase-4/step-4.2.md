# Step 4.2 — Package Declaration Injection

**Phase:** 4 — Validation and polish
**Status:** Todo
**Depends on:** Step 3.2 (`PageObjectGenerator` calls this before returning)
**Blocks:** —

---

## Goal

Ensure every generated `.java` file begins with `package generated;`, even if the AI omits
it despite explicit instructions. Files without a package declaration will not compile
correctly in the project structure.

---

## Location

`PageObjectGenerator.injectPackageIfMissing()` — already specified in Step 3.2.

```java
private String injectPackageIfMissing(String javaSource) {
    if (!javaSource.startsWith("package ")) {
        iLogger.warn("PageObjectGenerator: AI omitted package declaration — injecting 'package generated;'");
        return "package generated;\n\n" + javaSource;
    }
    return javaSource;
}
```

Called after `validateSource()` and before `extractClassName()`:

```
extractJavaSource(response)
    → validateSource()
    → injectPackageIfMissing()   ← this step
    → extractClassName()
    → new GeneratedPageObject(...)
```

---

## Why This Is Needed

Models frequently omit the `package` line when instructed to "output only the Java source
code", since in many training examples package declarations are absent in short snippets.
The system prompt explicitly states `The first line of your output must be: package generated;`
but compliance is inconsistent across models and temperatures.

---

## Edge Cases

| Scenario | Behaviour |
|----------|-----------|
| AI outputs `package generated;` correctly | Source unchanged, no warning |
| AI outputs `package generated;` with wrong package name (e.g. `package pages;`) | Not corrected in MVP — warn logged, source written as-is, developer must fix |
| AI outputs a blank line before the package declaration | `startsWith("package ")` fails → injection adds a second declaration — **gap** |

### Gap: leading blank lines

To handle leading blank lines, strip the source before checking:

```java
private String injectPackageIfMissing(String javaSource) {
    if (!javaSource.stripLeading().startsWith("package ")) {
        iLogger.warn("PageObjectGenerator: AI omitted package declaration — injecting 'package generated;'");
        return "package generated;\n\n" + javaSource.stripLeading();
    }
    return javaSource;
}
```

---

## Unit Tests

(Covered by `PageObjectGeneratorTest` in Step 3.2)

Additional cases for this step:
- Source starts with blank lines then `package generated;` → blank lines stripped, package not duplicated
- Source has `package pages;` → not corrected, source written as-is (document this)
- Source has no package → `package generated;\n\n` prepended, WARN logged

---

## Definition of Done

- [ ] `injectPackageIfMissing()` implemented using `stripLeading()` to handle leading blanks
- [ ] WARN logged when injection occurs
- [ ] No double package declaration possible
- [ ] Tests for leading-blank-line edge case pass
