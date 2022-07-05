package core.tools;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.collect.Iterables.any;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public final class ReflectionUtils {

  public static boolean isClass(Field field, Class<?> expected) {
    return isClass(field.getType(), expected);
  }

  public static boolean isClass(Class<?> t, Class<?> expected) {
    if (expected == Object.class) {
      return true;
    }
    Class<?> type = t;
    while (type != null && type != Object.class) {
      if (type == expected) {
        return true;
      } else {
        type = type.getSuperclass();
      }
    }
    return false;
  }

  public static boolean isClass(Class<?> type, Class<?>... expected) {
    for (Class<?> expectedType : expected) {
      Class<?> actualType = type;
      if (expectedType == Object.class) {
        return true;
      }
      while (actualType != null && actualType != Object.class) {
        if (actualType == expectedType) {
          return true;
        } else {
          actualType = actualType.getSuperclass();
        }
      }
    }
    return false;
  }

  public static boolean isInterface(Field field, Class<?> expected) {
    return isInterface(field.getType(), expected);
  }

  public static boolean isInterface(Class<?> type, Class<?> expected) {
    if (type == null || expected == null || type == Object.class) {
      return false;
    }
    if (type == expected) {
      return true;
    }
    List<Class> interfaces = asList(type.getInterfaces());
    return any(interfaces, i -> isInterface(i, expected)) || isInterface(type.getSuperclass(), expected);
  }

  public static Object getValueField(Field field, Object obj) {
    field.setAccessible(true);
    try {
      return field.get(obj);
    } catch (Exception ex) {
      throw new RuntimeException(format("Can't get field '%s' value", field.getName()));
    }
  }
}
