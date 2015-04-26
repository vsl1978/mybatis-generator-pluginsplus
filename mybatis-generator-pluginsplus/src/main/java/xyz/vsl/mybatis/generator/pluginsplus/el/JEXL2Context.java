package xyz.vsl.mybatis.generator.pluginsplus.el;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.MapContext;

import java.util.Map;

/**
 * @author Vladimir Lokhov.
 */
class JEXL2Context implements Context {
    private JexlContext ctx = new MapContext();

    @Override
    public Object get(String name) {
        return ctx.get(name);
    }

    @Override
    public Context set(String name, Object value) {
        ctx.set(name, value);
        return this;
    }

    @Override
    public Context set(Map<String, Object> values) {
        if (values != null)
            for (Map.Entry<String, Object> e : values.entrySet())
                ctx.set(e.getKey(), e.getValue());
        return this;
    }

    JexlContext getDelegatedContext() {
        return ctx;
    }
}
