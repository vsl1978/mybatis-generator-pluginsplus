package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.rules.Rules;

import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;

/**
 * MyBatis Generator utilities
 * @author Vladimir Lokhov
 */
final class MBGenerator {

    public static class FQJT {
        public final static FullyQualifiedJavaType LIST_OF_STRING  = new FullyQualifiedJavaType("java.util.List<java.lang.String>");
        public final static FullyQualifiedJavaType LIST_OF_INTEGER  = new FullyQualifiedJavaType("java.util.List<java.lang.Integer>");
        public final static FullyQualifiedJavaType LIST_OF_LONG  = new FullyQualifiedJavaType("java.util.List<java.lang.Long>");
        public final static FullyQualifiedJavaType STRING = FullyQualifiedJavaType.getStringInstance();
        public final static FullyQualifiedJavaType BOOL = FullyQualifiedJavaType.getBooleanPrimitiveInstance();
        public final static FullyQualifiedJavaType BOOLEAN = new FullyQualifiedJavaType("java.lang.Boolean");
        public final static FullyQualifiedJavaType OBJECT = FullyQualifiedJavaType.getObjectInstance();
        public final static FullyQualifiedJavaType CRITERIA  = FullyQualifiedJavaType.getCriteriaInstance();
        public final static FullyQualifiedJavaType INT = FullyQualifiedJavaType.getIntInstance();
        public final static FullyQualifiedJavaType INTEGER = PrimitiveTypeWrapper.getIntegerInstance();
        public final static FullyQualifiedJavaType VOID = null;

        public final static FQJT STATIC = new FQJT();
        public final static FQJT FINAL = new FQJT();
        public final static FQJT SYNCHRONIZED = new FQJT();
        public final static FQJT VOLATILE = new FQJT();
        public final static FQJT TRANSIENT = new FQJT();

        private FQJT() {}
    }

    public static String tableName(IntrospectedTable introspectedTable) {
        String table = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        String schema = introspectedTable.getFullyQualifiedTable().getIntrospectedSchema() + ".";
        if (table.startsWith(schema))
            table = table.substring(schema.length());
        return table;
    }

    public static String camel(String name) {
        return Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
    }

    public static void addStringField(InnerClass criterion, String name) {
        for (Field f : criterion.getFields())
            if (f.getName().equals(name)) return;
        criterion.addField(field(PRIVATE, FQJT.STRING, name, _("\"\"")));
        criterion.addMethod(method(PUBLIC, FQJT.STRING, "get" + camel(name), _("return "+name+";")));
    }

    public static Method getDeclaredMethod(InnerClass klazz, String methodName, FullyQualifiedJavaType ... parameters) {
        if (klazz == null || klazz.getMethods() == null || klazz.getMethods().isEmpty())
            return null;
        if (methodName == null)
            return null;
        search:
        for (Method m : klazz.getMethods()) {
            if (!methodName.equals(m.getName())) continue;
            if (parameters.length != (m.getParameters() == null ? 0 : m.getParameters().size())) continue;
            if (parameters.length == 0) return m;
            int idx = 0;
            for (Parameter p : m.getParameters()) {
                FullyQualifiedJavaType a = parameters[idx++];
                if (!Objects.equals(a, p.getType())) continue search;
            }
            return m;
        }
        return null;
    }

    public static JavaMethod method(Object ... args) {
        JavaMethod m = new JavaMethod();
        for (Object o : args) {
            if (o == null) continue;

            if (o instanceof JavaVisibility) m.setVisibility((JavaVisibility) o);
            else if (o instanceof FullyQualifiedJavaType) m.setReturnType((FullyQualifiedJavaType) o);
            else if (o instanceof Parameter) m.addParameter((Parameter) o);
            else if (o instanceof String) m.setName((String) o);
            else if (o instanceof StringBuilder) m.addBodyLine(((StringBuilder) o).toString().replace('`', '"'));
            else if (o instanceof String[]) {
                for (String s : (String[])o) if (s != null) m.addBodyLine(s.replace('`', '"'));
            }
            else if (o instanceof Parameter[]) {
                for (Parameter p : (Parameter[])o) if (p != null) m.addParameter(p);
            }
            else if (o instanceof Throwable) m.addException(new FullyQualifiedJavaType(o.getClass().getName()));
            else if ((o instanceof Class<?>) && Throwable.class.isAssignableFrom((Class<?>)o)) m.addException(new FullyQualifiedJavaType(((Class<?>)o).getName()));
            else if (o == FQJT.STATIC) m.setStatic(true);
            else if (o == FQJT.FINAL) m.setFinal(true);
            else if (o == FQJT.SYNCHRONIZED) m.setSynchronized(true);
        }
        return m;
    }

    public static Method constructor(Object ... args) {
        Method m = method(args); // (Object[])
        m.setConstructor(true);
        return m;
    }

    public static JavaField field(Object ... args) {
        JavaField f = new JavaField();
        for (Object o : args) {
            if (o instanceof JavaVisibility) f.setVisibility((JavaVisibility) o);
            else if (o instanceof FullyQualifiedJavaType) f.setType((FullyQualifiedJavaType) o);
            else if (o instanceof String) f.setName((String)o);
            else if (o == FQJT.STATIC) f.setStatic(true);
            else if (o == FQJT.FINAL) f.setFinal(true);
            else if (o == FQJT.TRANSIENT) f.setTransient(true);
            else if (o == FQJT.VOLATILE) f.setVolatile(true);
            else if (o instanceof StringBuilder) f.setInitializationString(o.toString().replace('`', '"'));
        }
        return f;
    }

    public static StringBuilder _(String s, String ... args) {
        if (args.length == 0)
            return new StringBuilder(s);
        return new StringBuilder(String.format(s, (Object[])args));
    }

    public static Parameter _(FullyQualifiedJavaType type, String name) {
        return new Parameter(type, name);
    }

    public static Parameter _(FullyQualifiedJavaType type, String name, boolean isVararg) {
        return new Parameter(type, name, isVararg);
    }

    public static String[] __(CharSequence ... args) {
        String[] lines = new String[args.length];
        for (int i = 0; i < args.length; i++)
            if (args[i] != null) lines[i] = args[i].toString();
        return lines;
    }

    public static Parameter[] __(Parameter ... args) {
        return args;
    }

    public static Parameter[] __(Method method) {
        if (method == null || method.getParameters() == null)
            return new Parameter[0];
        return method.getParameters().toArray(new Parameter[method.getParameters().size()]);
    }

    public static void addBoolField(InnerClass criterion, String name) {
        for (Field f : criterion.getFields())
            if (f.getName().equals(name)) return;
        criterion.addField(field(PRIVATE, FQJT.BOOL, name));
        criterion.addMethod(method(PUBLIC, FQJT.BOOL, "is" + camel(name), _("return "+name+";")));
    }
/*
    public static XmlElement find(XmlElement parent, String ... path) {
        find:
        for (String name : path) {
            if (parent == null) return null;
            for (Element e : parent.getElements()) {
                if (e instanceof XmlElement) {
                    XmlElement x = (XmlElement)e;
                    if (name.equals(x.getName())) {
                        parent = x;
                        continue find;
                    }
                }
            }
            return null;
        }
        return parent;
    }
*/
    public static Attribute a(String name, String value) {
        return new Attribute(name, value);
    }

    public static XmlElement e(String name, Object ...children) {
        XmlElement e = new XmlElement(name);
        for (Object o : children) {
            if (o instanceof Attribute) e.addAttribute((Attribute)o);
        }
        for (Object o : children) {
            if (o == null) continue;
            if (o instanceof Attribute) continue;
            if (o instanceof DocumentFragment) e.getElements().addAll(((DocumentFragment)o).getElements());
            else if (o instanceof Element) e.addElement((Element)o);
            else e.addElement(new TextElement(String.valueOf(o)));
        }
        return e;
    }

    public static <T> T traverse(XmlElement root, XmlTraverser<T> walker) {
        traverse0(root, walker);
        walker.commitStructModifications();
        return walker.result();
    }

    private static void traverse0(XmlElement root, XmlTraverser<?> walker) {
        XmlElement parent = root;
        if (parent == null) return;
        walker.start(root);
        try {
            for (Element e : parent.getElements()) {
                if (e instanceof XmlElement) {
                    traverse0((XmlElement) e, walker);
                } else if (e instanceof TextElement) {
                    walker.text((TextElement)e);
                }
            }
        } finally {
            walker.finish(root);
        }
    }

    public static class CopyXml extends XmlTraverser<XmlElement> {
        private XmlElement root;
        private Stack<XmlElement> stack = new Stack<XmlElement>();
        @Override
        public void start(XmlElement element) {
            XmlElement x = new XmlElement(element.getName());
            if (element.getAttributes() != null)
                for (Attribute a : element.getAttributes())
                    x.addAttribute(new Attribute(a.getName(), a.getValue()));
            if (stack.isEmpty()) {
                root = x;
            } else {
                stack.peek().addElement(x);
            }
            stack.push(x);
        }

        @Override
        public void finish(XmlElement element) {
            stack.pop();
        }

        @Override
        public void text(TextElement text) {
            stack.peek().addElement(new TextElement(text.getContent()));
        }

        @Override
        public XmlElement result() {
            return root;
        }
    }

    public static class ReplaceText extends XmlTraverser<Integer> {
        private static java.lang.reflect.Field textContent;
        private static java.lang.reflect.Field attrValue;
        private XmlElement root;
        private TextReplacer<Stack<XmlElement>> replaceTextContent;
        private TextReplacer<Stack<XmlElement>> replaceAttributeValue;
        private Stack<XmlElement> stack = new Stack<XmlElement>();
        private int count;

        public ReplaceText(TextReplacer replaceTextContentAndAttributes) {
            this(replaceTextContentAndAttributes, replaceTextContentAndAttributes);
        }

        public ReplaceText(TextReplacer<Stack<XmlElement>> replaceTextContent, TextReplacer<Stack<XmlElement>> replaceAttributeValue) {
            this.replaceTextContent = replaceTextContent;
            this.replaceAttributeValue = replaceAttributeValue;
        }

        @Override
        public void start(XmlElement element) {
            if (root == null)
                root = element;
            stack.push(element);
            if (replaceAttributeValue != null && element.getAttributes() != null)
                for (Attribute a : element.getAttributes()) {
                    Pair<String, Boolean> r = replaceAttributeValue.replace(stack, a.getValue());
                    if (r.b()) {
                        try {
                            attrValue.set(a, r.a());
                            count++;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

        @Override
        public void finish(XmlElement element) {
            stack.pop();
        }

        @Override
        public void text(TextElement text) {
            if (replaceTextContent != null) {
                Pair<String, Boolean> r = replaceTextContent.replace(stack, text.getContent());
                if (r.b()) {
                    try {
                        textContent.set(text, r.a());
                        count++;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public Integer result() {
            return count;
        }
    }

    public static class FindElements extends XmlTraverser<Integer> {
        private XmlElement root;
        private Stack<XmlElement> stack = new Stack<XmlElement>();
        private int count;
        private BiPredicate<Stack<XmlElement>, Element> condition;

        public FindElements when(BiPredicate<Stack<XmlElement>, Element> condition) {
            this.condition = condition;
            return this;
        }

        public boolean process(XmlElement parent, Element self, int position) {
            return true;
        }

        @Override
        public void start(XmlElement element) {
            if (condition != null)
                if (condition.test(stack, element)) {
                    XmlElement parent = stack.isEmpty() ? null : stack.peek();
                    if (process(parent, element, indexOf(parent, element)))
                        count++;
                }
            stack.push(element);
        }

        @Override
        public void finish(XmlElement element) {
            stack.pop();
        }

        @Override
        public void text(TextElement text) {
            XmlElement parent = stack.isEmpty() ? null : stack.peek();
            if ((condition == null || condition.test(stack, text)) && process(parent, text, indexOf(parent, text)))
                count++;
        }

        public XmlElement ancestor(int n) {
            if (stack.size() <= n) return null;
            return stack.get(stack.size() - n - 1);
        }

        @Override
        public Integer result() {
            return count;
        }
    }


    public static int indexOf(XmlElement parent, Element element) {
        int idx = -1;
        if (parent == null || element == null) return idx;
        if (parent.getElements() == null) return idx;
        for (Element e : parent.getElements()) {
            idx++;
            if (e == element) break;
        }
        return idx;
    }

    public static int indexOf(XmlElement parent, String elementName) {
        int idx = -1;
        if (parent == null || elementName == null) return idx;
        if (parent.getElements() == null) return idx;
        for (Element e : parent.getElements()) {
            idx++;
            if (e instanceof XmlElement && elementName.equals(((XmlElement)e).getName())) break;
        }
        return idx;
    }


    public static MixedPredicate<Stack<XmlElement>, Element> ELEMENT = new MixedPredicate<Stack<XmlElement>, Element>() {
        @Override
        public boolean test(Stack<XmlElement> xmlElements, Element element) {
            return element instanceof XmlElement;
        }
        @Override
        public boolean test(Stack<XmlElement> xmlElements) {
            return !xmlElements.isEmpty() && (xmlElements.peek() instanceof XmlElement);
        }
    };

    public static BiPredicate<Stack<XmlElement>, Element> TEXT_NODE = new AbstractBiPredicate<Stack<XmlElement>, Element>() {
        @Override
        public boolean test(Stack<XmlElement> xmlElements, Element element) {
            return element instanceof TextElement;
        }
    };

    public static MixedPredicate<Stack<XmlElement>, Element> ancestors(final String ... namesFromTopAncestorToParent) {
        return new MixedPredicate<Stack<XmlElement>, Element>() {
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                return test(xmlElements);
            }
            @Override
            public boolean test(Stack<XmlElement> xmlElements) {
                int pos = namesFromTopAncestorToParent.length;
                ListIterator<XmlElement> it = xmlElements.listIterator(xmlElements.size());
                while (it.hasPrevious() && pos > 0) {
                    XmlElement e = it.previous();
                    pos--;
                    if (!Objects.equals(namesFromTopAncestorToParent[pos], e.getName())) return false;
                }
                return pos == 0;
            }
        };
    }

    public static MixedPredicate<Stack<XmlElement>, Element> depth(final int minDepth, final int maxDepth) {
        return new MixedPredicate<Stack<XmlElement>, Element>() {
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                int depth = xmlElements.size() + 1;
                return depth >= minDepth && depth <= maxDepth;
            }

            @Override
            public boolean test(Stack<XmlElement> xmlElements) {
                int depth = xmlElements.size();
                return depth >= minDepth && depth <= maxDepth;
            }
        };
    }

    public static BiPredicate<Stack<XmlElement>, Element> ancestorsAndSelf(final String ... namesFromTopAncestorToSelf) {
        return new AbstractBiPredicate<Stack<XmlElement>, Element>() {
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                int pos = namesFromTopAncestorToSelf.length - 1;
                if (!(element instanceof XmlElement)) return false;
                if (!Objects.equals(namesFromTopAncestorToSelf[pos], ((XmlElement)element).getName()))
                    return false;
                ListIterator<XmlElement> it = xmlElements.listIterator(xmlElements.size());
                while (it.hasPrevious() && pos > 0) {
                    XmlElement e = it.previous();
                    pos--;
                    if (!Objects.equals(namesFromTopAncestorToSelf[pos], e.getName())) return false;
                }
                return pos == 0;
            }
        };
    }

    public static MixedPredicate<Stack<XmlElement>, Element> attrMatches(final String name, String regexp) {
        final Pattern pattern = Pattern.compile(regexp);
        return new MixedPredicate<Stack<XmlElement>, Element>() {
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                XmlElement e = element instanceof Element ? (XmlElement)element : xmlElements.isEmpty() ? null : xmlElements.peek();
                if (e != null && e.getAttributes() != null)
                    for (Attribute a : e.getAttributes())
                        if (a.getName().equals(name))
                            return a.getValue() != null && pattern.matcher(a.getValue()).matches();
                return false;
            }
            public boolean test(Stack<XmlElement> xmlElements) {
                return test(xmlElements, null);
            }
        };
    }

    public static BiPredicate<Stack<XmlElement>, Element> textMatches(String regexp, final boolean formatted) {
        final Pattern pattern = Pattern.compile(regexp);
        return new AbstractBiPredicate<Stack<XmlElement>, Element>() {
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                if (!(element instanceof TextElement)) return false;
                TextElement text = (TextElement)element;
                String s = Str.trim(formatted ? text.getFormattedContent(0) : text.getContent());
                return s != null && pattern.matcher(s).matches();
            }
        };
    }

    public static BiPredicate<Stack<XmlElement>, Element> skipComments() {
        return new AbstractBiPredicate<Stack<XmlElement>, Element>() {
            private boolean comment;
            @Override
            public boolean test(Stack<XmlElement> xmlElements, Element element) {
                if (!(element instanceof TextElement)) return true;
                TextElement text = (TextElement)element;
                String s = text.getFormattedContent(0).trim();
                if (!comment) {
                    comment = s.indexOf("<!--") >= 0;
                    return !comment;
                } else {
                    comment = s.indexOf("-->") < 0;
                    return false;
                }
            }
        };
    }

    public static String getAttribute(XmlElement e, String name) {
        if (e.getAttributes() != null)
            for (Attribute a : e.getAttributes())
                if (Objects.equals(a.getName(), name)) return a.getValue();
        return null;
    }

    public static XmlElement setAttribute(XmlElement e, String name, String value) {
        boolean remove = Str.trim(value) == null;
        if (e.getAttributes() != null)
            for (ListIterator<Attribute> it = e.getAttributes().listIterator(); it.hasNext(); ) {
                Attribute a = it.next();
                if (Objects.equals(a.getName(), name)) {
                    if (remove) it.remove();
                    else it.set(new Attribute(a.getName(), value));
                    break;
                }
            }
        return e;
    }

    public static String getTextContent(XmlElement e) {
        StringBuilder sb = new StringBuilder();
        if (e.getElements() != null)
            for (Element c : e.getElements()) {
                if (!(c instanceof TextElement)) continue;
                String s = ((TextElement)c).getContent();
                if (s != null) sb.append(s);
            }
        return sb.toString();
    }

    public static boolean isTopmostModelClass(Plugin.ModelClassType type, IntrospectedTable table) {
        Rules rules = table.getRules();
        if (type == Plugin.ModelClassType.PRIMARY_KEY)
            return rules.generatePrimaryKeyClass();
        if (type == Plugin.ModelClassType.BASE_RECORD)
            return !rules.generatePrimaryKeyClass() && rules.generateBaseRecordClass();
        if (type == Plugin.ModelClassType.RECORD_WITH_BLOBS)
            return !rules.generatePrimaryKeyClass() && !rules.generateBaseRecordClass() && rules.generateRecordWithBLOBsClass();
        return false;
    }

    public static boolean isUndermostModelClass(Plugin.ModelClassType type, IntrospectedTable table) {
        Rules rules = table.getRules();
        if (type == Plugin.ModelClassType.PRIMARY_KEY)
            return rules.generatePrimaryKeyClass() && !rules.generateBaseRecordClass() && !rules.generateRecordWithBLOBsClass();
        if (type == Plugin.ModelClassType.BASE_RECORD)
            return rules.generateBaseRecordClass() && !rules.generateRecordWithBLOBsClass();
        if (type == Plugin.ModelClassType.RECORD_WITH_BLOBS)
            return rules.generateRecordWithBLOBsClass();
        return false;
    }

    static {
        try {
            ReplaceText.textContent = TextElement.class.getDeclaredField("content");
            ReplaceText.textContent.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            ReplaceText.attrValue = Attribute.class.getDeclaredField("value");
            ReplaceText.attrValue.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
