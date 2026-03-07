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

        String groupDescription = normalizeDescription(heading, "assertAll");
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
        String description = normalizeDescription(checkDescription, "condition is true");
        iLogger.info("Check that " + description);
        Assert.assertTrue(condition, description);
    }

    public static void isFalse(boolean condition, String checkDescription) {
        String description = normalizeDescription(checkDescription, "condition is false");
        iLogger.info("Check that " + description);
        Assert.assertFalse(condition, description);
    }

    public static void equalsTo(Object actual, Object expected, String checkDescription) {
        String description = normalizeDescription(checkDescription, "actual value equals expected value");
        logCheckWithExpectedActual(description, expected, actual);
        Assert.assertEquals(actual, expected, description);
    }

    public static void notEqualsTo(Object actual, Object expected, String checkDescription) {
        String description = normalizeDescription(checkDescription, "actual value does not equal expected value");
        logCheckWithExpectedActual(description, expected, actual);
        Assert.assertNotEquals(actual, expected, description);
    }

    public static void isNull(Object actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "value is null");
        logCheckWithExpectedActual(description, null, actual);
        Assert.assertNull(actual, description);
    }

    public static void isNotNull(Object actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "value is not null");
        logCheckWithExpectedActual(description, "not null", actual);
        Assert.assertNotNull(actual, description);
    }

    public static void same(Object actual, Object expected, String checkDescription) {
        String description = normalizeDescription(checkDescription, "both references point to the same object");
        logCheckWithExpectedActual(description, expected, actual);
        Assert.assertSame(actual, expected, description);
    }

    public static void notSame(Object actual, Object expected, String checkDescription) {
        String description = normalizeDescription(checkDescription, "references point to different objects");
        logCheckWithExpectedActual(description, "different reference than " + expected, actual);
        Assert.assertNotSame(actual, expected, description);
    }

    public static void contains(String actual, String expectedSubstring, String checkDescription) {
        String description = normalizeDescription(checkDescription, "text contains expected substring");
        logCheckWithExpectedActual(description, expectedSubstring, actual);
        Assert.assertTrue(actual != null && expectedSubstring != null && actual.contains(expectedSubstring), description);
    }

    public static void notContains(String actual, String unexpectedSubstring, String checkDescription) {
        String description = normalizeDescription(checkDescription, "text does not contain unexpected substring");
        logCheckWithExpectedActual(description, unexpectedSubstring, actual);
        Assert.assertTrue(actual == null || unexpectedSubstring == null || !actual.contains(unexpectedSubstring), description);
    }

    public static void isEmpty(String actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "string is empty");
        logCheckWithExpectedActual(description, "", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), description);
    }

    public static void isNotEmpty(String actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "string is not empty");
        logCheckWithExpectedActual(description, "not empty", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), description);
    }

    public static void isEmpty(Collection<?> actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "collection is empty");
        logCheckWithExpectedActual(description, "empty collection", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), description);
    }

    public static void isNotEmpty(Collection<?> actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "collection is not empty");
        logCheckWithExpectedActual(description, "non-empty collection", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), description);
    }

    public static void isEmpty(Map<?, ?> actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "map is empty");
        logCheckWithExpectedActual(description, "empty map", actual);
        Assert.assertTrue(actual != null && actual.isEmpty(), description);
    }

    public static void isNotEmpty(Map<?, ?> actual, String checkDescription) {
        String description = normalizeDescription(checkDescription, "map is not empty");
        logCheckWithExpectedActual(description, "non-empty map", actual);
        Assert.assertTrue(actual != null && !actual.isEmpty(), description);
    }

    public static void fail(String reason) {
        String description = normalizeDescription(reason, "forced failure");
        iLogger.error("Check failed: " + description);
        Assert.fail(description);
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

    private static String normalizeDescription(String checkDescription, String fallback) {
        if (checkDescription == null || checkDescription.isBlank()) {
            return fallback;
        }
        return checkDescription;
    }

    private static void logCheckWithExpectedActual(String description, Object expected, Object actual) {
        iLogger.info(
                "Check that " + description
                        + System.lineSeparator()
                        + " expected: " + String.valueOf(expected)
                        + System.lineSeparator()
                        + " actual: " + String.valueOf(actual)
        );
    }
}
