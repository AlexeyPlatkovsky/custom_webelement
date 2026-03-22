package utils.assertions;

import org.testng.Assert;
import utils.logging.iLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class iAssert {
    @FunctionalInterface
    public interface Executable {
        void execute() throws Throwable;
    }

    private iAssert() {
    }

    public static void assertAll(Executable... executables) {
        assertAll("assertAll", executables);
    }

    public static void assertAll(String heading, Executable... executables) {
        if (executables == null) {
            throw new NullPointerException("executables array must not be null");
        }
        assertAll(heading, Arrays.asList(executables));
    }

    public static void assertAll(String heading, Collection<Executable> executables) {
        if (executables == null) {
            throw new NullPointerException("executables collection must not be null");
        }

        String groupDescription = normalizeFailureMessage(heading, "assertAll");
        iLogger.info("Check group: " + groupDescription);

        List<Throwable> failures = new ArrayList<>();
        int index = 0;
        for (Executable executable : executables) {
            index++;
            try {
                if (executable == null) {
                    failures.add(new NullPointerException("Executable at index " + index + " is null"));
                    continue;
                }
                executable.execute();
            } catch (Throwable throwable) {
                if (isUnrecoverable(throwable)) {
                    rethrow(throwable);
                }
                failures.add(throwable);
            }
        }

        if (failures.isEmpty()) {
            return;
        }

        AssertionError aggregatedError = new AssertionError(buildAssertAllMessage(groupDescription, failures));
        for (Throwable failure : failures) {
            aggregatedError.addSuppressed(failure);
        }
        throw aggregatedError;
    }

    public static void isTrue(boolean condition, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "condition is true");
        logAssertion("Assert condition is true", failureMessage);
        Assert.assertTrue(condition, failureMessage);
    }

    public static void isFalse(boolean condition, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "condition is false");
        logAssertion("Assert condition is false", failureMessage);
        Assert.assertFalse(condition, failureMessage);
    }

    public static void equalsTo(Object actual, Object expected, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "actual value equals expected value");
        logCheckWithExpectedActual("Assert values are equal", failureMessage, expected, actual);
        Assert.assertEquals(actual, expected, failureMessage);
    }

    public static void notEqualsTo(Object actual, Object expected, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "actual value does not equal expected value");
        logCheckWithExpectedActual("Assert values are not equal", failureMessage, expected, actual);
        Assert.assertNotEquals(actual, expected, failureMessage);
    }

    public static void isNull(Object actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "value is null");
        logCheckWithExpectedActual("Assert value is null", failureMessage, null, actual);
        Assert.assertNull(actual, failureMessage);
    }

    public static void isNotNull(Object actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "value is not null");
        logCheckWithExpectedActual("Assert value is not null", failureMessage, "not null", actual);
        Assert.assertNotNull(actual, failureMessage);
    }

    public static void same(Object actual, Object expected, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "both references point to the same object");
        logCheckWithExpectedActual("Assert references are the same", failureMessage, expected, actual);
        Assert.assertSame(actual, expected, failureMessage);
    }

    public static void notSame(Object actual, Object expected, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "references point to different objects");
        logCheckWithExpectedActual("Assert references are different", failureMessage, "different reference than " + expected, actual);
        Assert.assertNotSame(actual, expected, failureMessage);
    }

    public static void contains(String actual, String expectedSubstring, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "text contains expected substring");
        logCheckWithExpectedActual("Assert text contains expected substring", failureMessage, expectedSubstring, actual);
        Assert.assertTrue(actual != null && expectedSubstring != null && actual.contains(expectedSubstring), failureMessage);
    }

    public static void notContains(String actual, String unexpectedSubstring, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "text does not contain unexpected substring");
        logCheckWithExpectedActual("Assert text does not contain unexpected substring", failureMessage, unexpectedSubstring, actual);
        Assert.assertTrue(actual == null || unexpectedSubstring == null || !actual.contains(unexpectedSubstring), failureMessage);
    }

    public static void isEmpty(String actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "string is empty");
        logCheckWithExpectedActual("Assert string is empty", failureMessage, "", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), failureMessage);
    }

    public static void isNotEmpty(String actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "string is not empty");
        logCheckWithExpectedActual("Assert string is not empty", failureMessage, "not empty", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), failureMessage);
    }

    public static void isEmpty(Collection<?> actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "collection is empty");
        logCheckWithExpectedActual("Assert collection is empty", failureMessage, "empty collection", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), failureMessage);
    }

    public static void isNotEmpty(Collection<?> actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "collection is not empty");
        logCheckWithExpectedActual("Assert collection is not empty", failureMessage, "non-empty collection", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), failureMessage);
    }

    public static void isEmpty(Map<?, ?> actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "map is empty");
        logCheckWithExpectedActual("Assert map is empty", failureMessage, "empty map", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), failureMessage);
    }

    public static void isNotEmpty(Map<?, ?> actual, String checkDescription) {
        String failureMessage = normalizeFailureMessage(checkDescription, "map is not empty");
        logCheckWithExpectedActual("Assert map is not empty", failureMessage, "non-empty map", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), failureMessage);
    }

    public static void fail(String reason) {
        String failureMessage = normalizeFailureMessage(reason, "forced failure");
        iLogger.error("Assertion failed: " + failureMessage);
        Assert.fail(failureMessage);
    }

    private static String buildAssertAllMessage(String heading, List<Throwable> failures) {
        StringBuilder message = new StringBuilder();
        message.append(heading)
                .append(" (")
                .append(failures.size())
                .append(failures.size() == 1 ? " failure" : " failures")
                .append(")");

        int index = 0;
        for (Throwable failure : failures) {
            index++;
            message.append(System.lineSeparator())
                    .append("  ")
                    .append(index)
                    .append(") ")
                    .append(failure.getClass().getSimpleName())
                    .append(": ")
                    .append(String.valueOf(failure.getMessage()));
        }

        return message.toString();
    }

    private static boolean isUnrecoverable(Throwable throwable) {
        return throwable instanceof VirtualMachineError
                || throwable instanceof LinkageError;
    }

    private static void rethrow(Throwable throwable) {
        if (throwable instanceof Error error) {
            throw error;
        }
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new RuntimeException(throwable);
    }

    private static String normalizeFailureMessage(String checkDescription, String fallback) {
        if (checkDescription == null || checkDescription.isBlank()) {
            return fallback;
        }
        return checkDescription;
    }

    private static void logAssertion(String assertionLabel, String failureMessage) {
        iLogger.info(
                assertionLabel
                        + System.lineSeparator()
                        + " failure message: " + failureMessage
        );
    }

    private static void logCheckWithExpectedActual(String assertionLabel, String failureMessage, Object expected, Object actual) {
        iLogger.info(
                assertionLabel
                        + System.lineSeparator()
                        + " failure message: " + failureMessage
                        + System.lineSeparator()
                        + " expected: " + String.valueOf(expected)
                        + System.lineSeparator()
                        + " actual: " + String.valueOf(actual)
        );
    }
}
