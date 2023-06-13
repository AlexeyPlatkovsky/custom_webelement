package utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static String readFileAsString(String fileName) throws IOException, URISyntaxException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Path path = Paths.get(classLoader.getResource(fileName).toURI());
        return Files.readString(path);
    }
}
