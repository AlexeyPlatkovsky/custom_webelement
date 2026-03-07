package unit_tests.utils;

import org.testng.annotations.Test;
import utils.StringUtil;
import utils.assertions.iAssert;

import static utils.assertions.iAssert.assertAll;
import static utils.assertions.iAssert.equalsTo;

@Test(groups = {"unit"})
public class StringUtilTest {
    @Test
    public void testCutExtraEndSlashes() {
        assertAll("Cut extra end slashes",
                () -> equalsTo(StringUtil.cutExtraEndSlashes("http://www.google.com/"), "http://www.google.com", "single ending slash is removed"),
                () -> equalsTo(StringUtil.cutExtraEndSlashes("http://www.google.com"), "http://www.google.com", "url without ending slash stays unchanged"),
                () -> equalsTo(StringUtil.cutExtraEndSlashes("http://www.google.com//"), "http://www.google.com", "multiple ending slashes are removed")
        );
    }

    @Test
    public void testFormatRelativeURL() {
        assertAll("Relative URLs formatting",
                () -> equalsTo(StringUtil.formatRelativeURL("http://www.google.com/"), "http://www.google.com", "http absolute url trims trailing slash"),
                () -> equalsTo(StringUtil.formatRelativeURL("https://www.google.com/"), "https://www.google.com", "https absolute url trims trailing slash"),
                () -> equalsTo(StringUtil.formatRelativeURL("www.google.com"), "www.google.com", "plain domain stays unchanged"),
                () -> equalsTo(StringUtil.formatRelativeURL("/search"), "/search", "already relative path stays unchanged"),
                () -> equalsTo(StringUtil.formatRelativeURL("search"), "/search", "relative path gets leading slash")
        );
    }

    @Test
    public void cssToXPath() {
        assertAll("Css to Xpath Convert",
                () -> equalsTo(StringUtil.cssToXPath("div"), "//div", "tag selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath(".test"), "//*[contains(@class, 'test')]", "class selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath(".test"), "//*[contains(@class, 'test')]", "duplicate class selector conversion remains deterministic"),
                () -> equalsTo(StringUtil.cssToXPath("span.test"), "//span[contains(@class, 'test')]", "tag and class selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("div.test span"), "//div[contains(@class, 'test')]//span", "descendant selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("div.test1 span.test2"), "//div[contains(@class, 'test1')]//span[contains(@class, 'test2')]", "descendant selector with classes converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("div.test span#testId"), "//div[contains(@class, 'test')]//span[@id='testId']", "id selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath(".test1.test2"), "//*[contains(@class, 'test1')][contains(@class, 'test2')]", "multiple classes convert to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("div.test1.test2"), "//div[contains(@class, 'test1')][contains(@class, 'test2')]", "tag with multiple classes converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("[target]"), "//*[@target]", "attribute presence selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("[target=\"_blank\"]"), "//*[@target='_blank']", "attribute value selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath("[title~=\"flower\"]"), "//*[contains(@title, 'flower')]", "attribute contains selector converts to xpath"),
                () -> equalsTo(StringUtil.cssToXPath(".gLFyf[type='search']"), "//*[contains(@class, 'gLFyf')][@type='search']", "mixed class and attribute selector converts to xpath")
        );
    }
}
