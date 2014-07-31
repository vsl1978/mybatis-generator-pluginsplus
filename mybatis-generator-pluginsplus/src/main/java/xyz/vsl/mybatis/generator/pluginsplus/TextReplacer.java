package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir Lokhov
 */
class TextReplacer<T> {

    static final Pair<String, Boolean> UNCHANGED = new Pair<String, Boolean>(null, false);

    private Pattern pattern;
    private String to;

    TextReplacer(String pattern, String to) {
        this.pattern = Pattern.compile(pattern);
        this.to = to;
    }

    public Pair<String, Boolean> replace(T context, String value) {
        if (value == null) return UNCHANGED;
        Matcher m = pattern.matcher(value);
        if (!m.find()) return UNCHANGED;
        StringBuffer sb = new StringBuffer();
        do {
            m.appendReplacement(sb, to);
        } while (m.find());
        m.appendTail(sb);
        return new Pair<String, Boolean>(sb.toString(), true);
    }
}
