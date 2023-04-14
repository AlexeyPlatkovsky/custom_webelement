package unit_tests.utils;

import org.junit.Test;
import utils.StringUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.testng.Assert.assertEquals;

public class StringUtilTest {
    @Test
    public void testCutExtraEndSlashes() {
        assertAll("Cut extra end slashes",
                () -> assertEquals(StringUtil.cutExtraEndSlashes("http://www.google.com/"), "http://www.google.com"),
                () -> assertEquals(StringUtil.cutExtraEndSlashes("http://www.google.com"), "http://www.google.com"),
                () -> assertEquals(StringUtil.cutExtraEndSlashes("http://www.google.com//"), "http://www.google.com")
        );
    }

    @Test
    public void testFormatRelativeURL() {
        assertAll("Relative URLs formatting",
                () -> assertEquals(StringUtil.formatRelativeURL("http://www.google.com/"), "http://www.google.com"),
                () -> assertEquals(StringUtil.formatRelativeURL("https://www.google.com/"), "https://www.google.com"),
                () -> assertEquals(StringUtil.formatRelativeURL("www.google.com"), "www.google.com"),
                () -> assertEquals(StringUtil.formatRelativeURL("/search"), "/search"),
                () -> assertEquals(StringUtil.formatRelativeURL("search"), "/search")
        );

    }
}