package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.Map;

/**
 * @author Vladimir Lokhov
 */
public interface Context {
    public Context set(String name, Object value);

    public Context set(Map<String, Object> values);

    public Object get(String name);
}
