package utils.readers;

import com.google.common.reflect.ClassPath;
import utils.logging.iLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassLoader {

  public List<Class<?>> loadClassesInPackage(String packageName) {
    List<Class<?>> classes = new ArrayList();
    try {
      for (ClassPath.ClassInfo classInfo
              : ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(packageName)) {
        Class<?> cls;

        cls = classInfo.load();

        if (!cls.isInterface()) {
          classes.add(cls);
        }
      }
    } catch (IOException e) {
      iLogger.error("Couldn't load class ", e);
    }
    return classes;
  }
}
