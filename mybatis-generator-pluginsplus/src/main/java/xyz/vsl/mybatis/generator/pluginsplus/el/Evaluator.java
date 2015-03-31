package xyz.vsl.mybatis.generator.pluginsplus.el;

/**
 * @author Vladimir Lokhov
 */
public interface Evaluator {

    public Evaluator addFunction(Function fn);

    public Evaluator addOperator(Operator op);

    Tokenizer getTokenizer();

    public Evaluator compile(String expression);

    public String evaluate(Context context);

}
