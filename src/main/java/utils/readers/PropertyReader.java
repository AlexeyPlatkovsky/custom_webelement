package utils.readers;

import org.apache.commons.lang3.reflect.FieldUtils;
import utils.logging.iLogger;
import utils.properties.FilePath;
import utils.properties.Property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class PropertyReader {

  private static final Properties PROPERTIES = new Properties();

  public static void readProperties() {
    PROPERTIES.putAll(System.getProperties());

    List<Class<?>> classesList = new ClassLoader().loadClassesInPackage("utils.properties");
    String filePath;
    for (Class<?> clazz : classesList) {
      Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, Property.class);
      if (clazz.isAnnotationPresent(FilePath.class)) {
        filePath = clazz.getAnnotation(FilePath.class).value();
        try {
          PROPERTIES.load(Files.newInputStream(Paths.get(filePath)));
        } catch (IOException e) {
          iLogger.error("Couldn't read file " + filePath, e);
        }
      }
      for (Field field : fields) {
        String key = field.getAnnotation(Property.class).value();
        try {
          String value = PROPERTIES.getProperty(key);
          field.set(String.class, value);
          iLogger.info("Set property " + clazz.getName() + "::" + field.getName() + " with value " + value);
        } catch (IllegalAccessException e) {
          iLogger.error("Couldn't set field " + field.getName() + " for property class " + clazz.getName(), e);
        }
      }
    }
  }
}
