package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vladimir Lokhov
 */
class StringMacro extends LinkedHashMap<String, String> {

    public String replaceAll(String s) {
        for (Map.Entry<String, String> e : entrySet())
            if (e.getKey() != null && e.getValue() != null)
                s = s.replaceAll(e.getKey(), e.getValue());
        return s;
    }

    public StringMacro text(String substring, String value) {
        put(Pattern.quote(substring), value);
        return this;
    }

    public StringMacro pattern(String pattern, String value) {
        put(pattern, value);
        return this;
    }

}
