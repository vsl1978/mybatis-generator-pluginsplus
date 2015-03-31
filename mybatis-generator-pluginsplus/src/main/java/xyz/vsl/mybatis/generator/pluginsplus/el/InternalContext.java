package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladimir Lokhov
 */
public class InternalContext implements Context {
    private Map<String, Object> variables = new HashMap<String, Object>();
    private Map<String, Function> functions = new HashMap<String, Function>();


    @Override
    public Context set(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    @Override
    public Context set(Map<String, Object> values) {
        if (values != null)
            values.putAll(values);
        return this;
    }

    @Override
    public Object get(String name) {
        return variables.get(name);
    }

    @Override
    public Context override(String name, Function fn) {
        functions.put(name, fn);
        return this;
    }

    @Override
    public Function fn(String name) {
        return functions.get(name);
    }

    @Override
    public String resolve(Token token) {
        Object o = resolveObject(token);
        return o != null ? String.valueOf(o) : null;
    }

    @Override
    public Object resolveObject(Token token) {
        String s = token.text;
        if (s == null || s.length() == 0) return null;
        if (token.type == Type.var) return resolve(s);
        if (token.type != Type.val) return null;
        /*
        if ("true".equals(s) || "false".equals(s)) return s;
        if (Character.isDigit(s.charAt(0))) return s;
        */
        return s;
    }

    @Override
    public Object resolve(String name) {
        String[] path = name.split("\\.");
        return resolve(variables, path[0], path.length == 1 ? null : Arrays.copyOfRange(path, 1, path.length));
    }

    private Object resolve(Object o, String name, String[] tail) {
        if (o == null) return null;
        Object next = null;
        if (o instanceof Map<?,?>) next = ((Map<?,?>)o).get(name);
        else {
            Class<?> klazz = o.getClass();
            while (next == null && klazz != null && klazz != Object.class) {
                try {
                    java.lang.reflect.Method m = klazz.getDeclaredMethod(name);
                    if (m != null) {
                        m.setAccessible(true);
                        next = m.invoke(o);
                        break;
                    }
                } catch (Exception e) {
                }
                try {
                    java.lang.reflect.Field f = klazz.getDeclaredField(name);
                    if (f != null) {
                        f.setAccessible(true);
                        next = f.get(o);
                    }
                } catch (Exception e) {
                }
                klazz = klazz.getSuperclass();
            }
        }
        if (tail != null)
            return resolve(next, tail[0], tail.length == 1 ? null : Arrays.copyOfRange(tail, 1, tail.length));
        return next;
    }

    @Override
    public boolean asBoolean(Token t) {
        String v = resolve(t);
        return "true".equals(v);
    }

    @Override
    public Long asNumber(Token t) {
        String v = resolve(t);
        try { return Long.parseLong(v); } catch (Exception e) { return null; }
    }

    @Override
    public Long asNumber(String v) {
        try { return Long.parseLong(v); } catch (Exception e) { return null; }
    }

    @Override
    public int compare(Token a, Token b) {
        String s1 = resolve(a);
        String s2 = resolve(b);
        if (s1 != null && s2 != null) {
            Long n1 = asNumber(s1);
            Long n2 = asNumber(s2);
            if (n1 != null && n2 != null)
                return n1.compareTo(n2);
        }
        if ("true".equals(s1)) return "true".equals(s2) ? 0 : -1;
        if ("false".equals(s1)) return "false".equals(s2) ? 0 : 1;
        if (s1 == null) return s2 == null ? 0 : -1;
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }
}
