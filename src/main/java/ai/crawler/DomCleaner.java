package ai.crawler;

import utils.logging.iLogger;

public class DomCleaner {

    private static final int DOM_LIMIT = 50_000;
    private static final String TRUNCATION_MARKER = " <!-- [DOM TRUNCATED AT 50KB LIMIT] -->";

    private DomCleaner() {}

    public static String clean(String rawHtml) {
        String html = rawHtml;

        // 1. Remove <script> blocks
        html = html.replaceAll("(?si)<script[^>]*>.*?</script>", "");

        // 2. Remove <style> blocks
        html = html.replaceAll("(?si)<style[^>]*>.*?</style>", "");

        // 3. Remove HTML comments
        html = html.replaceAll("(?s)<!--.*?-->", "");

        // 4. Strip inline event handler attributes
        html = html.replaceAll("\\s+on\\w+=\"[^\"]*\"", "");
        html = html.replaceAll("\\s+on\\w+='[^']*'", "");

        // 5. Collapse whitespace
        html = html.replaceAll("\\s{2,}", " ").strip();

        // 6. Truncate at 50,000 characters at a clean tag boundary
        if (html.length() > DOM_LIMIT) {
            int cutPoint = html.lastIndexOf('>', DOM_LIMIT);
            if (cutPoint < 0) {
                cutPoint = DOM_LIMIT;
            }
            html = html.substring(0, cutPoint + 1) + TRUNCATION_MARKER;
            iLogger.info("DomCleaner: DOM truncated to " + DOM_LIMIT + " chars for prompt safety");
        }

        return html;
    }
}
