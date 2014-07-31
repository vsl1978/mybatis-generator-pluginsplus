package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * @author Vladimir Lokhov
 */
class Objects {

    public static boolean equals(Object a, Object b) {
        return a == null && b == null || a != null && a.equals(b);
    }

    @SuppressWarnings("unchecked")
    public static <T> T nvl(T ... values) {
        for (T v : values) if (v != null) return v;
        if (values.length == 1 && values instanceof String[])
            return (T)"";
        return null;
    }

}
