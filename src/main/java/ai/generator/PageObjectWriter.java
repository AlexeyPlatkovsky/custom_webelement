package ai.generator;

import utils.logging.iLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes a {@link GeneratedPageObject} as a {@code .java} source file to disk.
 *
 * <p>The default output directory is {@code src/test/java/generated} relative to the
 * working directory (project root when launched via Gradle).
 *
 * <p>Usage:
 * <pre>{@code
 * Path written = new PageObjectWriter().write(generatedPageObject);
 * }</pre>
 */
public class PageObjectWriter {

    private static final String DEFAULT_OUTPUT_DIR = "src/test/java/generated";

    private final Path outputDirectory;

    /** Creates a writer that targets the default {@code src/test/java/generated} directory. */
    public PageObjectWriter() {
        this(Paths.get(DEFAULT_OUTPUT_DIR));
    }

    /** Creates a writer targeting a custom output directory. Primarily for testing. */
    public PageObjectWriter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Writes the generated Page Object source code to
     * {@code <outputDirectory>/<ClassName>.java}, creating the directory if absent.
     *
     * @return the absolute path of the written file
     * @throws RuntimeException if the file cannot be written
     */
    public Path write(GeneratedPageObject pageObject) {
        try {
            Files.createDirectories(outputDirectory);
            Path file = outputDirectory.resolve(pageObject.getClassName() + ".java");
            Files.writeString(file, pageObject.getSourceCode(), StandardCharsets.UTF_8);
            iLogger.info("Page Object written to: " + file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to write Page Object '" + pageObject.getClassName() + "': " + e.getMessage(), e
            );
        }
    }
}
