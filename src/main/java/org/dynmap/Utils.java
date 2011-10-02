package org.dynmap;

public final class Utils {
    public static <T> T as(Class<T> c, Object o) {
        if (c.isInstance(o)) {
            return c.cast(o);
        }
        return null;
    }
}
