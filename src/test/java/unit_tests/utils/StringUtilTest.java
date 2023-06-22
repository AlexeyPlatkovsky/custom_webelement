package unit_tests.utils;

import org.testng.annotations.Test;
import utils.StringUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.testng.Assert.assertEquals;

@Test(groups = {"unit"})
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

    @Test
    public void cssToXPath() {
        assertAll("Css to Xpath Convert",
                () -> assertEquals(StringUtil.cssToXPath("div"), "//div"),
                () -> assertEquals(StringUtil.cssToXPath(".test"), "//*[contains(@class, 'test')]"),
                () -> assertEquals(StringUtil.cssToXPath(".test"), "//*[contains(@class, 'test')]"),
                () -> assertEquals(StringUtil.cssToXPath("span.test"), "//span[contains(@class, 'test')]"),
                () -> assertEquals(StringUtil.cssToXPath("div.test span"), "//div[contains(@class, 'test')]//span"),
                () -> assertEquals(StringUtil.cssToXPath("div.test1 span.test2"), "//div[contains(@class, 'test1')]//span[contains(@class, 'test2')]"),
                () -> assertEquals(StringUtil.cssToXPath("div.test span#testId"), "//div[contains(@class, 'test')]//span[@id='testId']"),
                () -> assertEquals(StringUtil.cssToXPath(".test1.test2"), "//*[contains(@class, 'test1')][contains(@class, 'test2')]"),
                () -> assertEquals(StringUtil.cssToXPath("div.test1.test2"), "//div[contains(@class, 'test1')][contains(@class, 'test2')]"),
                () -> assertEquals(StringUtil.cssToXPath("[target]"), "//*[@target]"),
                () -> assertEquals(StringUtil.cssToXPath("[target=\"_blank\"]"), "//*[@target='_blank']"),
                () -> assertEquals(StringUtil.cssToXPath("[title~=\"flower\"]"), "//*[contains(@title, 'flower')]"),
                () -> assertEquals(StringUtil.cssToXPath(".gLFyf[type='search']"), "//*[contains(@class, 'gLFyf')][@type='search']")
        );
    }
}