package unit_tests.utils.properties;

import org.testng.annotations.Test;
import utils.readers.PropertyReader;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = "unit")
public class PropertyReaderEnvVarTest {

    private String expandEnvVars(String value) throws Exception {
        Method method = PropertyReader.class.getDeclaredMethod("expandEnvVars", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, value);
    }

    @Test
    public void expandsSetEnvironmentVariable() throws Exception {
        // PATH is always set in any POSIX environment
        String path = System.getenv("PATH");
        String result = expandEnvVars("${PATH}");
        assertEquals(result, path, "Should expand ${PATH} to the actual PATH value");
    }

    @Test
    public void leavesUnsetVariableUnchanged() throws Exception {
        String result = expandEnvVars("${CUSTOM_WEBELEMENT_DEFINITELY_UNSET_12345}");
        assertEquals(result, "${CUSTOM_WEBELEMENT_DEFINITELY_UNSET_12345}",
            "Unexpanded placeholder should be left as-is");
    }

    @Test
    public void returnsPlainValueUnchanged() throws Exception {
        String result = expandEnvVars("plain-value");
        assertEquals(result, "plain-value", "Value without placeholder should be returned unchanged");
    }

    @Test
    public void expandsTwoPlaceholdersInOneValue() throws Exception {
        String home = System.getenv("HOME");
        String path = System.getenv("PATH");
        if (home == null || path == null) {
            return; // skip if env vars not available (e.g. Windows)
        }
        String result = expandEnvVars("${HOME}:${PATH}");
        assertEquals(result, home + ":" + path, "Both placeholders should be expanded");
    }

    @Test
    public void handlesNullValueWithoutNpe() throws Exception {
        String result = expandEnvVars(null);
        assertNull(result, "null value should return null without NPE");
    }
}
