package ai.generator;

import ai.crawler.PageSnapshot;
import lombok.Value;

/**
 * Immutable result of a Page Object generation.
 * Carries the extracted class name, the generated Java source, and the original snapshot.
 */
@Value
public class GeneratedPageObject {

    /** Simple class name extracted from the generated source (e.g. {@code LoginPage}). */
    String className;

    /** Complete Java source code ready to be written to disk. */
    String sourceCode;

    /** The page snapshot that was used as input for generation. */
    PageSnapshot snapshot;
}
