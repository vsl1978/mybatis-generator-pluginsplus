package xyz.vsl.mybatis.generator.pluginsplus.el;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;

/**
 * @author Vladimir Lokhov
 */
class JEXL2Evaluator implements Evaluator {
    private JexlEngine engine;
    private Expression expression;

    JEXL2Evaluator(JexlEngine engine) {
        this.engine = engine;
    }

    @Override
    public Evaluator compile(String expression) {
        this.expression = engine.createExpression(expression);
        return this;
    }

    @Override
    public Tokenizer getTokenizer() {
        return new JEXL2Tokenizer();
    }

    @Override
    public String evaluate(Context context) {
        Object o = evaluateObject(context);
        return o == null ? null : o.toString();
    }

    @Override
    public boolean evaluateBoolean(Context context) {
        Object o = evaluateObject(context);
        if (o == null)
            return false;
        if (o instanceof Boolean)
            return ((Boolean)o).booleanValue();
        if (o instanceof String)
            return ((String)o).trim().toLowerCase().equals("true");
        return false;
    }

    @Override
    public Object evaluateObject(Context context) {
        return expression.evaluate(((JEXL2Context)context).getDelegatedContext());
    }
}
