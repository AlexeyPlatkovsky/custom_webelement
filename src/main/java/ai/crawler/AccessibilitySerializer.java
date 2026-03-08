package ai.crawler;

import com.microsoft.playwright.Page;
import utils.logging.iLogger;

/**
 * Extracts a simplified accessibility tree from a loaded Playwright page.
 * Uses JavaScript evaluation because {@code page.accessibility().snapshot()} was
 * removed in Playwright 1.25. The JS snippet walks the DOM and collects ARIA
 * attributes and semantic roles from interactive and landmark elements.
 */
class AccessibilitySerializer {

    // Inlined JS: returns a JSON string representing the ARIA tree of document.body.
    // Only semantic/interactive elements are included; purely structural wrappers are skipped.
    private static final String ARIA_EXTRACT_JS =
        "(function() {"
        + "  var TAG_ROLES = {'button':'button','a':'link','input':'textbox','select':'combobox',"
        + "    'textarea':'textbox','form':'form','nav':'navigation','main':'main',"
        + "    'header':'banner','footer':'contentinfo','h1':'heading','h2':'heading',"
        + "    'h3':'heading','h4':'heading','h5':'heading','h6':'heading',"
        + "    'img':'img','label':'label','li':'listitem','ul':'list','ol':'list'};"
        + "  function extract(el, depth) {"
        + "    if (!el || depth > 8) return null;"
        + "    var tag = (el.tagName || '').toLowerCase();"
        + "    var role = el.getAttribute('role') || TAG_ROLES[tag] || '';"
        + "    var name = el.getAttribute('aria-label') || el.getAttribute('placeholder')"
        + "              || el.getAttribute('alt') || el.getAttribute('title') || '';"
        + "    if (!name && ['h1','h2','h3','h4','h5','h6','button','a','label'].indexOf(tag) >= 0)"
        + "      name = (el.textContent || '').trim().substring(0, 100);"
        + "    var node = {};"
        + "    if (role) node.role = role;"
        + "    if (name) node.name = name;"
        + "    if (el.getAttribute('type')) node.type = el.getAttribute('type');"
        + "    if (el.getAttribute('href')) node.href = el.getAttribute('href');"
        + "    if (el.getAttribute('id')) node.id = el.getAttribute('id');"
        + "    if (el.getAttribute('data-testid')) node.testid = el.getAttribute('data-testid');"
        + "    var kids = [];"
        + "    for (var i = 0; i < el.children.length; i++) {"
        + "      var child = extract(el.children[i], depth + 1);"
        + "      if (child) kids.push(child);"
        + "    }"
        + "    if (kids.length > 0) node.children = kids;"
        + "    return (role || name || kids.length > 0) ? node : null;"
        + "  }"
        + "  return JSON.stringify(extract(document.body, 0) || {});"
        + "})()";

    private AccessibilitySerializer() {}

    static String extract(Page page) {
        try {
            Object result = page.evaluate(ARIA_EXTRACT_JS);
            return result != null ? result.toString() : "{}";
        } catch (Exception e) {
            iLogger.error("AccessibilitySerializer: failed to extract ARIA tree — " + e.getMessage());
            return "{}";
        }
    }
}
