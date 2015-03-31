package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public abstract class Operator extends Action {
    private String name;
    private String[] altNames;
    private int priority;
    private int argsCount = 2;

    public Operator(int priority, String name, String... altNames) {
        this.name = name;
        this.altNames = altNames;
        this.priority = priority;
    }

    public Operator(int argsCount, int priority, String name, String... altNames) {
        this.name = name;
        this.altNames = altNames;
        this.priority = priority;
        this.argsCount = argsCount;
    }

    public String[] getAltNames() {
        return altNames;
    }

    public int getArgsCount() {
        return argsCount;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

}
