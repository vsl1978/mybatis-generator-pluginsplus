package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public abstract class Function extends Action {
    private String name;

    public Function(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Function declare(String name) {
        return new Function(name) {
            @Override
            public String apply(Context ctx, List<Token> args) {
                throw new UnsupportedOperationException("Unsupported function: '"+getName()+"'");
            }
        };
    }
}
