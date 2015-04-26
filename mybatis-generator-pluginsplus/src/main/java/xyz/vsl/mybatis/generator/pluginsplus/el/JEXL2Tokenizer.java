package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
class JEXL2Tokenizer implements Tokenizer {

    @Override
    public String escape(String text) {
        return text == null ? "''" : ("'" + text + "'");
    }

    @Override
    public List<String> tokenize(String expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String unescape(String text) {
        throw new UnsupportedOperationException();
    }
}
