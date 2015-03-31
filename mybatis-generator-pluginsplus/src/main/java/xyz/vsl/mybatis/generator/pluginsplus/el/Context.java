package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.Map;

/**
 * @author Vladimir Lokhov
 */
public interface Context {
    public final static String VAR_PREFIX   = "$.";

    public Context set(String name, Object value);

    public Context set(Map<String, Object> values);

    public Context override(String name, Function fn);

    public Object get(String name);

    public Function fn(String name);

    public String resolve(Token token);

    public Object resolveObject(Token token);

    public Object resolve(String name);

    public boolean asBoolean(Token t);

    public Long asNumber(Token t);

    public Long asNumber(String t);

    public int compare(Token a, Token b);
}
