package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * @author Vladimir Lokhov
 */
class Bool {
    public static boolean bool(String s, boolean defaultValue) {
        s = Str.trim(s);
        if (s == null) return defaultValue;
        s = s.toLowerCase();
        if (defaultValue)
            return !("false".equals(s) || "no".equals(s) || "off".equals(s) || "нет".equals(s));
        else
            return ("true".equals(s) || "yes".equals(s) || "on".equals(s) || "да".equals(s));
    }
}
