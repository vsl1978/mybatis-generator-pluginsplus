package xyz.vsl.mybatis.generator.pluginsplus.el;

/**
 * @author Vladimir Lokhov
 */
public class Token {
    public final Type type;
    public final String text;
    public final int priority;
    public final int argsCount;

    private boolean fnWithParentheses;

    public Token(Type type, String text) {
        this(type, text, 0, 0);
    }

    public Token(Type type, String text, int argsCount, int priority) {
        this.argsCount = argsCount;
        this.type = type;
        this.text = text;
        this.priority = priority;
    }

    public Token(Token t, int argsCount) {
        this(t.type, t.text, argsCount, t.priority);
        setFnWithParentheses(t.isFnWithParentheses());
    }

    boolean isFnWithParentheses() {
        return fnWithParentheses;
    }

    Token setFnWithParentheses(boolean fnWithParentheses) {
        this.fnWithParentheses = fnWithParentheses;
        return this;
    }

    @Override
    public String toString() {
        return "<" + type + ":" + text+(type == Type.op ? ("/"+argsCount+","+priority) : type == Type.fn ? ("/"+argsCount+", "+fnWithParentheses) : "")+">";
    }
}
