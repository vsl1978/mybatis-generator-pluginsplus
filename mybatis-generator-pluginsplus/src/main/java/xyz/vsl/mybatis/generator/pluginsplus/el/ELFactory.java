package xyz.vsl.mybatis.generator.pluginsplus.el;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.*;
import org.apache.commons.jexl2.*;

/**
 * @author Vladimir Lokhov
 */
public class ELFactory {
    private static JexlEngine engine;

    public static Context context() {
        return new JEXL2Context();
    }

    public static Evaluator evaluator() {
        return new JEXL2Evaluator(engine);
    }

    public static class Functor {
        public String lower(String s) {
            return s == null ? null : s.toLowerCase();
        }

        public String upper(String s) {
            return s == null ? null : s.toLowerCase();
        }

        public Collection<Object> conj(Object ... objects) {
            Collection<Object> target = null;
            if (objects != null)
                for (Object o : objects) {
                    if (o == null)
                        continue;
                    if (o instanceof Collection<?>) {
                        if (target == null) {
                            if (o instanceof Set)
                                target = new LinkedHashSet<Object>();
                            else
                                target = new ArrayList<Object>();
                        }
                        target.addAll((Collection<?>)o);
                    }
                    else {
                        if (target == null)
                            target = new ArrayList<Object>();
                        target.add(o);
                    }
                }
            return target;
        }

        public List<Object> attr(List<?> list, String path) {
            List<Object> result = new ArrayList<Object>();
            if (list != null && !list.isEmpty()) {
                Expression ex = engine.createExpression(path);
                for (Object o : list) {
                    if (o == null) continue;
                    Object r = ex.evaluate(new ObjectContext(engine, o));
                    if (r != null)
                        result.add(r);
                }
            }
            return result;
        }

        public List<Object> attrs(List<?> list, String path) {
            return attr(list, path);
        }

        public String quote(List<?> list) {
            StringBuilder sb = new StringBuilder();
            if (list != null) for (Object o : list) if (o != null) sb.append('"').append(o).append("\", ");
            if (sb.length() > 0) sb.setLength(sb.length() - 2);
            return sb.toString();
        }

        public boolean in(Object ... objects) {
            if (objects == null || objects.length < 2 || objects[0] == null)
                return false;
            Object lookupFor = objects[0];
            for (int i = 1; i < objects.length; i++) {
                Object o = objects[i];
                if (o == null) continue;
                if (o instanceof Collection<?>) {
                    if ( ((Collection<?>)o).contains(lookupFor) ) return true;
                }
                else {
                    if (lookupFor.equals(o)) return true;
                }
            }
            return false;
        }

        public int count(Object o) {
            if (o == null) return 0;
            if (o instanceof Collection<?>) return ((Collection<?>)o).size();
            if (o instanceof Object[]) return ((Object[])o).length;
            return 1;
        }

        public boolean is(Object a, String className) {
            if (a == null)
                return false;
            if (className == null)
                return false;
            if (a instanceof IntrospectedColumn) {
                a = ((IntrospectedColumn)a).getFullyQualifiedJavaType();
            }
            if (a instanceof org.mybatis.generator.api.dom.java.Field) {
                a = ((org.mybatis.generator.api.dom.java.Field)a).getType();
            }
            if (a instanceof FullyQualifiedJavaType) {
                FullyQualifiedJavaType type = (FullyQualifiedJavaType)a;
                return type.getFullyQualifiedName().equals(className) || type.getShortName().equals(className);
            }
            if (a instanceof String) {
                String type = (String)a;
                return type.equals(className);
            }
/*
        new Operator(30, "instanceof", "instanceof?") {
            public Token apply(Context ctx, List<Token> args) {
                Object o = ctx.resolveObject(first(args));
                String className = ctx.resolve(second(args));
                try {
                    Class<?> klazz = Class.forName(className);
                    return value(klazz.isAssignableFrom(o.getClass()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return value(false);
            }
        },
        new Operator(30, "is") {
            public Token apply(Context ctx, List<Token> args) {
                Object o = ctx.resolveObject(first(args));
                String className = ctx.resolve(second(args));
                else if (o instanceof String) {
                    String type = (String)o;
                    return value(type.equals(className));
                }
                return value(false);
            }
        },

 */
            return false;
        }

    }

    private static class U extends org.apache.commons.jexl2.introspection.UberspectImpl {
        public U(org.apache.commons.logging.Log logger) {
            super(logger);
        }
        public java.lang.reflect.Field getField(Object obj, String name, JexlInfo info) {
            Class<?> klazz = obj instanceof Class<?> ? (Class<?>) obj : obj.getClass();
            while (klazz != null && klazz != Object.class && !klazz.isPrimitive()) {
                try {
                    java.lang.reflect.Field f = klazz.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (Exception e) {
                }
                klazz = klazz.getSuperclass();
            }
            return null;//super.getField(obj, name, info);
        }
    }

    static {
        engine = new JexlEngine(new U(org.apache.commons.logging.LogFactory.getLog(JexlEngine.class)), null, null, null);
        Map<String, Object> funcs = new HashMap<String, Object>();
        funcs.put(null, new Functor());
        engine.setFunctions(funcs);
    }
}
