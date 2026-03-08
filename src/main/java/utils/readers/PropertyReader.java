package utils.readers;

import org.apache.commons.lang3.reflect.FieldUtils;
import utils.logging.iLogger;
import utils.properties.annotations.FilePath;
import utils.properties.annotations.Property;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class PropertyReader {

    private static final Properties PROPERTIES = new Properties();
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private static String expandEnvVars(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        return ENV_PATTERN.matcher(value).replaceAll(matchResult -> {
            String envValue = System.getenv(matchResult.group(1));
            return envValue != null ? envValue : matchResult.group(0);
        });
    }

    /**
     * Loads a .properties file from the classpath, expands ${ENV_VAR} placeholders,
     * and returns the result as a Properties object.
     */
    public static Properties load(String classpathResource) {
        Properties props = new Properties();
        try (InputStream in = PropertyReader.class.getClassLoader()
                .getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("Classpath resource not found: " + classpathResource);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + classpathResource, e);
        }
        props.replaceAll((k, v) -> expandEnvVars((String) v));
        return props;
    }

    public static void readProperties() {
        PROPERTIES.putAll(System.getProperties());

        List<Class<?>> classesList = new ClassPathScanner().loadClassesInPackage("utils.properties");
        String filePath;
        for (Class<?> clazz : classesList) {
            Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, Property.class);
            if (clazz.isAnnotationPresent(FilePath.class)) {
                filePath = clazz.getAnnotation(FilePath.class).value();
                try {
                    Properties fileProps = new Properties();
                    fileProps.load(Files.newInputStream(Paths.get(filePath)));
                    fileProps.replaceAll((k, v) -> expandEnvVars((String) v));
                    PROPERTIES.putAll(fileProps);
                } catch (IOException e) {
                    iLogger.error("Couldn't read file " + filePath, e);
                }
            }
            for (Field field : fields) {
                String key = field.getAnnotation(Property.class).value();
                String value = PROPERTIES.getProperty(key);

                try {
                    Field classField = clazz.getDeclaredField(field.getName());
                    classField.setAccessible(true);
                    Object convertedValue = convertValue(value, classField.getType());
                    classField.set(null, convertedValue);
                    iLogger.info("Set property " + clazz.getName() + "::" + field.getName() + " with value " + value);
                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    iLogger.error("Couldn't set field " + field.getName() + " for property class " + clazz.getName(), e);
                }
            }
        }
    }

    private static Object convertValue(String value, Class<?> type) {
        return switch (type.getSimpleName()) {
            case "String" -> value;
            case "Integer" -> Integer.parseInt(value);
            case "Boolean" -> Boolean.parseBoolean(value);
            default -> null;
        };
    }
}
