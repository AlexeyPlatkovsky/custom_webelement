package unit_tests.ai.crawler;

import ai.crawler.DomCleaner;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class DomCleanerTest {

    @Test
    public void removesScriptBlocks() {
        String html = "<html><body><script>alert('xss')</script><p>Hello</p></body></html>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("<script"), "script tag should be removed");
        assertTrue(result.contains("<p>Hello</p>"), "non-script content should remain");
    }

    @Test
    public void removesStyleBlocks() {
        String html = "<html><head><style>.foo { color: red; }</style></head><body><p>Hello</p></body></html>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("<style"), "style tag should be removed");
        assertTrue(result.contains("<p>Hello</p>"), "non-style content should remain");
    }

    @Test
    public void removesHtmlComments() {
        String html = "<div><!-- this is a comment --><p>Content</p></div>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("<!--"), "HTML comments should be removed");
        assertTrue(result.contains("<p>Content</p>"), "non-comment content should remain");
    }

    @Test
    public void removesInlineEventHandlerDoubleQuote() {
        String html = "<button onclick=\"doSomething()\">Click</button>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("onclick"), "onclick attribute should be removed");
        assertTrue(result.contains("<button"), "button tag should remain");
    }

    @Test
    public void removesInlineEventHandlerSingleQuote() {
        String html = "<a onmouseover='showTooltip()' href='/home'>Home</a>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("onmouseover"), "onmouseover attribute should be removed");
        assertTrue(result.contains("href='/home'"), "other attributes should remain");
    }

    @Test
    public void collapsesExcessiveWhitespace() {
        String html = "<div>   <p>   Hello   World   </p>   </div>";
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("   "), "multiple spaces should be collapsed");
    }

    @Test
    public void doesNotTruncateExactlyAtLimit() {
        // Build an HTML string of exactly DOM_LIMIT characters
        String tag = "<p>";
        int limit = 50_000;
        StringBuilder sb = new StringBuilder(limit);
        while (sb.length() < limit - tag.length()) {
            sb.append("x");
        }
        // Make it exactly limit chars with a tag boundary
        String html = sb + "<p>";
        assertEquals(html.length(), limit);
        String result = DomCleaner.clean(html);
        assertFalse(result.contains("DOM TRUNCATED"), "should not append truncation marker at exactly 50000 chars");
    }

    @Test
    public void truncatesOver50KCharsAtTagBoundary() {
        StringBuilder sb = new StringBuilder(60_000);
        // Create valid HTML with a clear tag boundary before 50000
        sb.append("<div>");
        while (sb.length() < 55_000) {
            sb.append("<p>some content</p>");
        }
        String html = sb.toString();
        String result = DomCleaner.clean(html);
        assertTrue(result.contains("DOM TRUNCATED AT 50KB LIMIT"), "truncation marker should be appended");
        assertTrue(result.length() < html.length(), "result should be shorter than input");
        // Find position of truncation marker; text before it should end with '>'
        int markerIdx = result.indexOf(" <!-- [DOM TRUNCATED AT 50KB LIMIT] -->");
        char lastChar = result.charAt(markerIdx - 1);
        assertEquals(lastChar, '>', "text before truncation marker should end with '>'");
    }

    @Test
    public void hardCutsWhenNoTagBoundaryBeforeLimit() {
        // String of 60000 chars with no '>' before position 50000
        StringBuilder sb = new StringBuilder(60_000);
        while (sb.length() < 60_000) {
            sb.append("x");
        }
        String html = sb.toString();
        // Should not throw; applies hard cut
        String result = DomCleaner.clean(html);
        assertTrue(result.contains("DOM TRUNCATED AT 50KB LIMIT"), "truncation marker should be appended");
    }
}
