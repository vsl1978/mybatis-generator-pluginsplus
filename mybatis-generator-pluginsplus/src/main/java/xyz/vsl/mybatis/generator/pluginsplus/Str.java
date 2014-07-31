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
}
