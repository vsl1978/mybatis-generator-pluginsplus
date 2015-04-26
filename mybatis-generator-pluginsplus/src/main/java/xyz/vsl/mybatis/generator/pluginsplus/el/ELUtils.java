package xyz.vsl.mybatis.generator.pluginsplus.el;

/**
 * @author Vladimir Lokhov
 */
public class ELUtils {

    public static String convertTemplateToExpression(String template, Tokenizer tokenizer) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int pos;
        while (start < template.length() && (pos = template.indexOf("#{", start)) >= 0) {
            int last = pos;
            int toClose = 0;
            for (; last < template.length(); last++)
                if (template.charAt(last) == '{')
                    toClose++;
                else if (template.charAt(last) == '}') {
                    toClose--;
                    if (toClose == 0) break;
                }
            if (pos > start) {
                if (sb.length() > 0)
                    sb.append(" + ");
                sb.append('(').append(tokenizer.escape(template.substring(start, pos))).append(')');
            }
            pos += "#{".length();
            if (pos < last) {
                if (sb.length() > 0)
                    sb.append(" + ");
                sb.append('(').append(template.substring(pos, last)).append(')');
            }
            start = last + 1;
        }
        if (start == 0)
            return tokenizer.escape(template);
        if (start < template.length()) {
            sb.append(" + ");
            sb.append('(').append(tokenizer.escape(template.substring(start))).append(')');
        }
        return sb.toString();
    }

}
