package xyz.vsl.mybatis.generator.pluginsplus.el;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public class ELFactory {

    public static Context context() {
        return new InternalContext();
    }

    public static Evaluator evaluator() {
        InternalEvaluator e = new InternalEvaluator(new DefaultTokenizer());
        for (Operator o : DEFAULT_OPERATORS)
            e.addOperator(o);
        for (Function f : DEFAULT_FUNCTIONS)
            e.addFunction(f);
        return e;
    }

    private static Function[] DEFAULT_FUNCTIONS = {
        new Function("lower") { public String apply(Context ctx, List<Token> args) { String s = ctx.resolve(first(args)); return s == null ? null : s.toLowerCase(); } },
        new Function("upper") { public String apply(Context ctx, List<Token> args) { String s = ctx.resolve(first(args)); return s == null ? null : s.toLowerCase(); } },
        new Function("in") { public String apply(Context ctx, List<Token> args) {
            String value = null;
            int idx = 0;
            for (Token t : args) {
                String s = ctx.resolve(t);
                if (idx == 0)
                    value = s;
                else {
                    if (value == null && s == null) return "true";
                    if (value != null && value.equals(s)) return "true";
                }
                idx++;
            }
            return "false";
        }},
    };
    
    private static Operator[] DEFAULT_OPERATORS = {
        new Operator(20, "&&", "and") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.asBoolean(first(args)) && ctx.asBoolean(second(args))); } },
        new Operator(10, "||", "or") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.asBoolean(first(args)) || ctx.asBoolean(second(args))); } },
        new Operator(1, 40, "!", "not") { public String apply(Context ctx, List<Token> args) { return String.valueOf(!ctx.asBoolean(first(args))); } },
        new Operator(10, "^", "xor") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.asBoolean(first(args)) ^ ctx.asBoolean(second(args))); } },
        new Operator(30, "<", "lt") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) < 0); } },
        new Operator(30, ">", "gt") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) > 0); } },
        new Operator(30, "<=", "le") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) <= 0); } },
        new Operator(30, ">=", "ge") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) >= 0); } },
        new Operator(30, "!=", "ne", "<>") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) != 0); } },
        new Operator(30, "=", "eq", "==") { public String apply(Context ctx, List<Token> args) { return String.valueOf(ctx.compare(first(args), second(args)) == 0); } },
        new Operator(30, "~", "like") { public String apply(Context ctx, List<Token> args) {
            String t = ctx.resolve(first(args));
            if (t == null) return "false";
            String s = ctx.resolve(second(args));
            if (s == null) return "false";

            StringBuilder sb = new StringBuilder();
            sb.append('^');
            for (int i = 0; i < s.length(); i++) {
                char c = Character.toLowerCase(s.charAt(i));
                if (c == '_') sb.append('.');
                else if (c == '%') sb.append(".*");
                else sb.append(c);
            }
            sb.append('$');
            return String.valueOf(t.toLowerCase().matches(s.toLowerCase()));
        }},
        new Operator(10, "~~", "similar") { public String apply(Context ctx, List<Token> args) {
            String t = ctx.resolve(first(args));
            if (t == null) return "false";
            String s = ctx.resolve(second(args));
            if (s == null) return "false";
            return String.valueOf(t.matches(s));
        }},
        new Operator(30, "instanceof", "instanceof?") {
            public String apply(Context ctx, List<Token> args) {
                Object o = ctx.resolveObject(first(args));
                String className = ctx.resolve(second(args));
                if (o == null) {
                    return "false";
                }
                if (className == null) {
                    return "false";
                }
                try {
                    Class<?> klazz = Class.forName(className);
                    return String.valueOf(klazz.isAssignableFrom(o.getClass()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return "false";
            }
        },
        new Operator(30, "is") {
            public String apply(Context ctx, List<Token> args) {
                Object o = ctx.resolveObject(first(args));
                String className = ctx.resolve(second(args));
                if (o == null) {
                    return "false";
                }
                if (className == null) {
                    return "false";
                }
                if (o instanceof FullyQualifiedJavaType) {
                    FullyQualifiedJavaType type = (FullyQualifiedJavaType)o;
                    return String.valueOf(type.getFullyQualifiedName().equals(className) || type.getShortName().equals(className));
                }
                else if (o instanceof String) {
                    String type = (String)o;
                    return String.valueOf(type.equals(className));
                }
                return "false";
            }
        },
        new Operator(30, "+") {
            public String apply(Context ctx, List<Token> args) {
                String s1 = ctx.resolve(first(args));
                String s2 = ctx.resolve(second(args));
                if (s1 != null && s2 != null) {
                    Long n1 = ctx.asNumber(s1);
                    Long n2 = ctx.asNumber(s2);
                    if (n1 != null && n2 != null)
                        return String.valueOf(n1.longValue() + n2.longValue());
                }
                StringBuilder sb = new StringBuilder();
                if (s1 != null) sb.append(s1);
                if (s2 != null) sb.append(s2);
                return sb.length() > 0 ? sb.toString() : null;
            }
        },
    };
}
