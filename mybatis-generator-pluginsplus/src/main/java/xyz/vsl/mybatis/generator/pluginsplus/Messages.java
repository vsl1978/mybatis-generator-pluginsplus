package xyz.vsl.mybatis.generator.pluginsplus;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Vladimir Lokhov
 */
class Messages {
    private static ResourceBundle rb;
    private String prefix;

    private Messages(Class<?> klazz) {
        prefix = klazz != null ? klazz.getSimpleName() + "." : "";
    }

    public static Messages load(Object o) {
        if (o instanceof Class<?>) return new Messages((Class<?>)o);
        if (o == null) return new Messages(null);
        return new Messages(o.getClass());
    }

    public String get(String code, Object ... args) {
        String s = rb.getString(prefix + code);
        if (s != null && args != null && args.length > 0)
            s = MessageFormat.format(s, args);
        return s;
    }

    static {
        rb = ResourceBundle.getBundle(Messages.class.getPackage().getName()+".messages");
    }

}
