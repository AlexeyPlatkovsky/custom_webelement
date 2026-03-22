package unit_tests.utils.assertions;

import org.testng.annotations.Test;
import utils.assertions.iAssert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Test(groups = {"unit"})
public class IAssertTest {

    @Test
    public void equalsAndNotEqualsShouldPass() {
        iAssert.equalsTo("value", "value", "string value equals expected");
        iAssert.notEqualsTo("value", "other", "string value differs from other");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void equalsShouldFailWhenValuesDiffer() {
        iAssert.equalsTo("actual", "expected", "different values should fail");
    }

    @Test
    public void nullAndNotNullShouldPass() {
        iAssert.isNull(null, "value is null");
        iAssert.isNotNull("value", "value is not null");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void isNullShouldFailForNonNullValue() {
        iAssert.isNull("not-null", "value should be null");
    }

    @Test
    public void sameAndNotSameShouldPass() {
        Object shared = new Object();
        Object another = new Object();
        iAssert.same(shared, shared, "references are the same");
        iAssert.notSame(shared, another, "references are different");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void sameShouldFailForDifferentReferences() {
        iAssert.same(new Object(), new Object(), "different references should fail");
    }

    @Test
    public void containsAndNotContainsShouldPass() {
        iAssert.contains("selenium webdriver docs", "webdriver", "text contains substring");
        iAssert.notContains("selenium webdriver docs", "playwright", "text does not contain substring");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void containsShouldFailWhenSubstringMissing() {
        iAssert.contains("selenium webdriver docs", "appium", "missing substring should fail");
    }

    @Test
    public void checkDescriptionIsUsedAsFailureMessage() {
        try {
            iAssert.isTrue(false, "boolean assertion failure message");
            iAssert.fail("assertion should have failed");
        } catch (AssertionError error) {
            iAssert.contains(error.getMessage(), "boolean assertion failure message",
                    "assertion preserves provided failure message");
        }
    }

    @Test
    public void emptyChecksShouldPass() {
        iAssert.isEmpty("", "string is empty");
        iAssert.isNotEmpty("x", "string is not empty");
        iAssert.isEmpty(List.of(), "collection is empty");
        iAssert.isNotEmpty(List.of("x"), "collection is not empty");
        iAssert.isEmpty(Map.of(), "map is empty");
        iAssert.isNotEmpty(Map.of("k", "v"), "map is not empty");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void failShouldAlwaysThrowAssertionError() {
        iAssert.fail("explicit failure");
    }

    @Test
    public void assertAllShouldPassWhenAllChecksPass() {
        AtomicInteger executed = new AtomicInteger(0);

        iAssert.assertAll("all checks should pass",
                () -> {
                    executed.incrementAndGet();
                    iAssert.isTrue(true, "first check passes");
                },
                () -> {
                    executed.incrementAndGet();
                    iAssert.equalsTo("v", "v", "second check passes");
                },
                () -> {
                    executed.incrementAndGet();
                    iAssert.isNotNull(new Object(), "third check passes");
                }
        );

        iAssert.equalsTo(executed.get(), 3, "all checks are executed when no failure occurs");
    }

    @Test
    public void assertAllShouldAggregateFailuresAndContinueExecution() {
        AtomicInteger executed = new AtomicInteger(0);

        try {
            iAssert.assertAll("grouped assertion",
                    () -> {
                        executed.incrementAndGet();
                        iAssert.equalsTo("actual", "expected", "first failure");
                    },
                    () -> {
                        executed.incrementAndGet();
                        throw new IllegalStateException("second failure");
                    },
                    () -> executed.incrementAndGet()
            );

            iAssert.fail("assertAll should fail when at least one executable fails");
        } catch (AssertionError error) {
            iAssert.equalsTo(executed.get(), 3, "assertAll executes all executables");
            iAssert.contains(error.getMessage(), "grouped assertion", "aggregated message contains heading");
            iAssert.contains(error.getMessage(), "2 failures", "aggregated message contains failures count");
            iAssert.equalsTo(error.getSuppressed().length, 2, "all failures are attached as suppressed exceptions");
        }
    }
}
