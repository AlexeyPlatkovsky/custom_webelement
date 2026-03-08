package unit_tests.ai.generator;

import ai.crawler.PageSnapshot;
import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectWriter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// singleThreaded = true because @BeforeMethod/@AfterMethod manage shared instance fields
@Test(groups = "unit", singleThreaded = true)
public class PageObjectWriterTest {

    private Path tempDir;

    @BeforeMethod
    public void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("page-object-writer-test-");
    }

    @AfterMethod
    public void deleteTempDir() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // best-effort cleanup
                        }
                    });
            }
        }
    }

    private GeneratedPageObject pageObject(String className, String source) {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Test", "<body/>", "{}", null
        );
        return new GeneratedPageObject(className, source, snapshot);
    }

    @Test
    public void writeCreatesFileNamedAfterClassName() throws IOException {
        PageObjectWriter writer = new PageObjectWriter(tempDir);
        GeneratedPageObject po = pageObject("LoginPage", "public class LoginPage {}");

        Path written = writer.write(po);

        assertEquals(written.getFileName().toString(), "LoginPage.java",
            "Written file must be named <ClassName>.java");
        assertTrue(Files.exists(written), "Written file must exist on disk");
    }

    @Test
    public void writtenFileContainsFullSourceCode() throws IOException {
        String source = "package generated;\npublic class SearchPage extends AbstractPage {}";
        PageObjectWriter writer = new PageObjectWriter(tempDir);
        GeneratedPageObject po = pageObject("SearchPage", source);

        Path written = writer.write(po);

        String content = Files.readString(written, StandardCharsets.UTF_8);
        assertEquals(content, source, "File content must exactly match the generated source code");
    }

    @Test
    public void writeCreatesOutputDirectoryWhenAbsent() throws IOException {
        Path nested = tempDir.resolve("a/b/c/generated");
        PageObjectWriter writer = new PageObjectWriter(nested);
        GeneratedPageObject po = pageObject("FooPage", "public class FooPage {}");

        Path written = writer.write(po);

        assertTrue(Files.exists(nested), "Nested output directory must be created automatically");
        assertTrue(Files.exists(written), "File must exist inside the nested directory");
    }

    @Test
    public void writeReturnsAbsolutePath() throws IOException {
        PageObjectWriter writer = new PageObjectWriter(tempDir);
        GeneratedPageObject po = pageObject("CheckoutPage", "public class CheckoutPage {}");

        Path written = writer.write(po);

        assertTrue(written.isAbsolute(), "Returned path must be absolute");
    }

    @Test
    public void secondWriteOverwritesFirstWithNewContent() throws IOException {
        PageObjectWriter writer = new PageObjectWriter(tempDir);
        GeneratedPageObject first = pageObject("MyPage", "// version 1");
        GeneratedPageObject second = pageObject("MyPage", "// version 2");

        writer.write(first);
        Path overwritten = writer.write(second);

        String content = Files.readString(overwritten, StandardCharsets.UTF_8);
        assertEquals(content, "// version 2", "Second write must overwrite the first");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void writeThrowsRuntimeExceptionWhenOutputDirectoryIsAFile() throws IOException {
        // Create a file where the output directory would be — forces IOException
        Path fileInsteadOfDir = tempDir.resolve("blocked");
        Files.writeString(fileInsteadOfDir, "blocker");

        PageObjectWriter writer = new PageObjectWriter(fileInsteadOfDir);
        writer.write(pageObject("AnyPage", "public class AnyPage {}"));
    }
}
