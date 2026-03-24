# Assertions

`iAssert` wraps TestNG's `Assert` with automatic INFO-level log output on every check.

## Why Use iAssert

Plain `Assert.assertEquals(actual, expected)` passes or fails silently in reports. `iAssert` prints what was checked, what was expected, and what was actual — without extra code in the test.

```java
// plain TestNG
Assert.assertEquals(actualText, expectedText);

// with iAssert — logs the check, expected, and actual values automatically
iAssert.equalsTo(actualText, expectedText, "search input contains entered query");
```

## Comparison Checks

| Method | Equivalent |
|---|---|
| `equalsTo(actual, expected, description)` | `assertEquals` |
| `notEqualsTo(actual, expected, description)` | `assertNotEquals` |
| `same(actual, expected, description)` | `assertSame` (reference equality) |
| `notSame(actual, expected, description)` | `assertNotSame` |

## Boolean Checks

| Method | Equivalent |
|---|---|
| `isTrue(condition, description)` | `assertTrue` |
| `isFalse(condition, description)` | `assertFalse` |

## Null Checks

| Method | Equivalent |
|---|---|
| `isNull(actual, description)` | `assertNull` |
| `isNotNull(actual, description)` | `assertNotNull` |

## String Checks

| Method | Notes |
|---|---|
| `contains(actual, substring, description)` | Passes if `actual` contains `substring` |
| `notContains(actual, substring, description)` | Passes if `actual` does not contain `substring` |
| `isEmpty(actual, description)` | String overload |
| `isNotEmpty(actual, description)` | String overload |

## Collection and Map Checks

| Method | Notes |
|---|---|
| `isEmpty(collection, description)` | Works for `Collection<?>` and `Map<?,?>` |
| `isNotEmpty(collection, description)` | Works for `Collection<?>` and `Map<?,?>` |

## Grouped Assertions (Soft Assert Pattern)

`assertAll` runs every check before reporting failures. Use it when you want to see all failures at once instead of stopping at the first one.

```java
iAssert.assertAll("product card checks",
    () -> iAssert.equalsTo(card.getTitle(), expectedTitle, "card title"),
    () -> iAssert.isNotEmpty(card.getPrice(), "card price is present"),
    () -> iAssert.isTrue(card.isDisplayed(), "card is visible")
);
```

The heading (`"product card checks"`) appears in the log and in the failure message.

Pass `Executable` lambdas — any `Throwable` is caught and collected. `VirtualMachineError` and `LinkageError` are rethrown immediately.

## Forced Failure

```java
iAssert.fail("reached unreachable branch");
```

Logs the reason at ERROR level and calls `Assert.fail`.

## Logging Format

Every check logs at INFO level:

```
Assert values are equal
 failure message: search input contains entered query
 expected: hello world
 actual: hello world
```

For boolean checks the format omits expected/actual and shows only the failure message.
