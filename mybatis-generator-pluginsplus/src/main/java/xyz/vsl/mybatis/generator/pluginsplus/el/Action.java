package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public abstract class Action {


    public Token first(List<Token> args) {
        return args.get(0);
    }

    public Token second(List<Token> args) {
        return args.get(1);
    }

    public abstract String apply(Context ctx, List<Token> args);

}
