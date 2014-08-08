package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir Lokhov
 */
class Str {
    public static String trim(String s) {
        if (s == null)
            return null;
        s = s.trim();
        return s.length() > 0 ? s : null;
    }

    public static String group(String text, String regexp) {
        return group(text, regexp, 1);
    }

    public static String group(String text, String regexp, int group) {
        if (group < 0) return null;
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String g = group <= m.groupCount() ? m.group(group) : null;
            if (g != null) return g;
        }
        return null;
    }

    public static Pair<String, String> groups(String text, String regexp, int group1, int group2) {
        if (group1 < 0 || group2 < 0 || text == null) return Pair.of(null, null);
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String g1 = group1 <= m.groupCount() ? m.group(group1) : null;
            String g2 = group2 <= m.groupCount() ? m.group(group2) : null;
            if (g1 != null || g2 != null) return Pair.of(g1, g2);
        }
        return Pair.of(null, null);
    }

    public static boolean containsSpaces(String ... strings) {
        for (String s : strings) if (s != null && s.indexOf(' ') >= 0) return true;
        return false;
    }

    public static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static String upper(String s) {
        return s == null ? null : s.toUpperCase();
    }
}
