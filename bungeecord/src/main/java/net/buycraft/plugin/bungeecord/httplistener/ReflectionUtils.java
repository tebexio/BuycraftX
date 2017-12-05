package net.buycraft.plugin.bungeecord.httplistener;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String name) throws IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = target.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return (T) setAccessible(field).get(target);
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return null;
    }

    public static void setFieldValue(Object target, String name, Object value) throws IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = target.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    setAccessible(field).set(target, value);
                    return;
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
    }

    public static <T extends AccessibleObject> T setAccessible(T object) {
        object.setAccessible(true);
        return object;
    }

    public static void setStaticFinalField(Field field, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setAccessible(Field.class.getDeclaredField("modifiers")).setInt(field, field.getModifiers() & ~Modifier.FINAL);
        setAccessible(Field.class.getDeclaredField("root")).set(field, null);
        setAccessible(Field.class.getDeclaredField("overrideFieldAccessor")).set(field, null);
        setAccessible(field).set(null, newValue);
    }

}
