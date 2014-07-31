package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Stack;

/**
 * @author Vladimir Lokhov
 */
class ConTextReplacer extends TextReplacer<Stack<XmlElement>> {
    private Predicate<Stack<XmlElement>> condition;
    ConTextReplacer(String pattern, String to, Predicate<Stack<XmlElement>> condition) {
        super(pattern, to);
        this.condition = condition;
    }

    @Override
    public Pair<String, Boolean> replace(Stack<XmlElement> parents, String value) {
        if (!condition.test(parents))
            return UNCHANGED;
        return super.replace(parents, value);
    }
}
