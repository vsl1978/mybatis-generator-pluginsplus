package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;

/**
 * <p>Provides the ability to use custom criteria.</p>
 * <p>This plugin adds new methods in Example-class:</p>
 * <table border="1" cellspacing="0" cellpadding="0">
 *     <thead>
 *         <tr>
 *             <th>Generated method's name</th>
 *             <th>Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr valign="top">
 *             <td>andEqualTo(String field, Object value)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field '=' value}<br/>
 *                 where {@code field} is any suitable sql expression and {@code value} is a bind variable
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andEqualTo("lower(shipcity)", shipcity.toLowerCase());</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>lower(shipcity) = ?</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andNotEqualTo(String field, Object value)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field '&lt;&gt;' value}<br/>
 *                 where {@code field} is any suitable sql expression and {@code value} is a bind variable
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andNotEqualTo("lower(shipcity)", shipcity.toLowerCase());</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>lower(shipcity) &lt;&gt; ?</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andIsNull(String field)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field is null}<br/>
 *                 where {@code field} is any sql expression
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andIsNull("firstname||lastname");</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>firstname||lastname is null</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andIsNotNull(String field)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field is not null}<br/>
 *                 where {@code field} is any sql expression
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andIsNotNull("firstname||lastname");</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>firstname||lastname is not null</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>and(String expression)</td>
 *             <td>
 *                 <p>Adds any string as criterion</p>
 *                 <p>Example:<br/>
 *                 <tt>and("orderdate + interval '1 week' &gt; to_date('01/'||exprdate, 'DD/MM/YYYY')");</tt><br/>
 *                 <tt>and("coalesce(billtolastname, shiptolastname, '') = coalesce(shiptolastname, billtolastname, '')");</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andIf(String field, String operator, Object value)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field operator value}<br/>
 *                 where {@code field} is any suitable sql expression and {@code operator} is any SQL-operator and {@code value} is a bind variable
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andIf("shipcity", "ilike", shipcity);</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>shipcity ilike ?</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andIf(String field, String operator, Object value, String beforeValue, String afterValue)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code field operator  beforeValue value afterValue}<br/>
 *                 where <ul>
 *                     <li>{@code field} is any suitable sql expression</li>
 *                     <li>{@code operator} is any SQL-operator</li>
 *                     <li>{@code value} is a bind variable</li>
 *                     <li>{@code beforeValue} and {@code afterValue} is prefix and suffix for sql substring</li>
 *                 </ul>
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andIf("soundex(shiptolastname)", "=", shipcity, "soundex(", ")");</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>soundex(shiptolastname) = soundex(?)</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andRightIf(Object value, String operator, String field)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code value operator field}<br/>
 *                 where {@code field} is any suitable sql expression and {@code operator} is any SQL-operator and {@code value} is a bind variable
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andRightIf(somevalue, "~", "column_with_regexp");</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>? ~ column_with_regexp</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>andRightIf(String field, String operator, Object value, String beforeValue, String afterValue)</td>
 *             <td>
 *                 <p>Adds criterion<br/>
 *                 {@code beforeValue value afterValue operator field}<br/>
 *                 where <ul>
 *                     <li>{@code field} is any suitable sql expression</li>
 *                     <li>{@code operator} is any SQL-operator</li>
 *                     <li>{@code value} is a bind variable</li>
 *                     <li>{@code beforeValue} and {@code afterValue} is prefix and suffix for sql substring</li>
 *                 </ul>
 *                 </p>
 *                 <p>Example:<br/>
 *                 <i>Java-code:</i><br/> <tt>andIf(somevalue, "", ", column_with_regexp, 'g')", "regexp_matches(", "");</tt><br/>
 *                 <i>Result sql:</i><br/> <tt>regexp_matches(?, column_with_regexp, 'g')</tt><br/>
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>
 *                 and***In(int[] value)<br/>
 *                 and***In(long[] value)<br/>
 *                 and***In(Number[] value)<br/>
 *                 and***In(Set&lt;Number&gt;[] value)<br/>
 *             </td>
 *             <td>
 *                 Decorators for {@code and***In(List&lt;Integer&gt; values)} and {@code and***In(List&lt;Long&gt; values)}
 *             </td>
 *         </tr>
 *         <tr valign="top">
 *             <td>
 *                 and***NotIn(int[] value)<br/>
 *                 and***NotIn(long[] value)<br/>
 *                 and***NotIn(Number[] value)<br/>
 *                 and***NotIn(Set&lt;Number&gt;[] value)<br/>
 *             </td>
 *             <td>
 *                 Decorators for {@code and***NotIn(List&lt;Integer&gt; values)} and {@code and***NotIn(List&lt;Long&gt; values)}
 *             </td>
 *         </tr>
 *     </tbody>
 * </table>
 * <p></p>
 * <p>Supported Java Client generators:<br/>
 * <b>ANNOTATEDMAPPER</b>: not supported<br/>
 * <b>MIXEDMAPPER</b>: supported<br/>
 * <b>XMLMAPPER</b>: supported<br/>
 * </p>
 *
 * @author Vladimir Lokhov
 */
public class AnyCriteriaPlugin extends PluginAdapter {
    private static final String AFTER_VALUE = "afterValue";
    private static final String BEFORE_VALUE = "beforeValue";
    private static final String RIGHT_VALUE = "rightValue";

    private boolean addRightIf;

    public boolean validate(List<String> warnings) {
        addRightIf = Bool.bool(Str.trim(properties.getProperty("addRightIf")), true);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass generatedCriteria = null;
        InnerClass criterion = null;

        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
            }
            else if ("Criterion".equals(innerClass.getType().getShortName())) {
                criterion = innerClass;
            }

        }
        if (generatedCriteria != null) {
            andEqualTo(generatedCriteria);
            andNotEqualTo(generatedCriteria);
            andIsNull(generatedCriteria);
            andIsNotNull(generatedCriteria);
            and(generatedCriteria);
            andIn(generatedCriteria, topLevelClass);
            andIf(generatedCriteria, criterion);
            if (addRightIf) andRightIf(generatedCriteria, criterion);
        }
        return true;
    }


    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!andIf(element, introspectedTable)) return false;
        if (addRightIf && !andRightIf(element, introspectedTable)) return false;
        return true;
    }



    private void andEqualTo(InnerClass generatedCriteria) {
        and_a_op_b(generatedCriteria, "andEqualTo", "=", "andIsNull");
    }
    private void andNotEqualTo(InnerClass generatedCriteria) {
        and_a_op_b(generatedCriteria, "andNotEqualTo", "<>", "andIsNotNull");
    }
    private void andIsNull(InnerClass generatedCriteria) {
        and_a_op_null(generatedCriteria, "andIsNull", "is");
    }
    private void andIsNotNull(InnerClass generatedCriteria) {
        and_a_op_null(generatedCriteria, "andIsNotNull", "is not");
    }
    private void and(InnerClass generatedCriteria) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "and", new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"), __(
                "addCriterion(condition);",
                "return (Criteria)this;"
        )));
    }
    private void andIf(InnerClass generatedCriteria, InnerClass criterion) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andIf", _(STRING, "field"), _(STRING, "operator"), _(OBJECT, "value"), __(
                "return andIf(field, operator, value, null, null);"
        )));

        String invalidConditionIdent = "`+field.replaceAll(`[^0-9A-Za-z _.()]`,``)+`";
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andIf", __(
                _(STRING, "field"), _(STRING, "operator"), _(OBJECT, "value"), _(STRING, BEFORE_VALUE), _(STRING, AFTER_VALUE)
            ), __(
                "if (field == null || field.trim().length() == 0) { field = `null`; }",
                _("if (operator == null) { addCriterion(`1=2 /* %s.operator */ `); return (Criteria)this; }", invalidConditionIdent),
                _("if (value == null) { addCriterion(`1=2 /* %s.value */ `); return (Criteria)this; }", invalidConditionIdent),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                "String condition = field + ` `+operator+` `;",
                "Criterion c = new Criterion(condition, value);",
                _("if (%1$s != null) c.%1$s = %1$s;", BEFORE_VALUE),
                _("if (%1$s != null) c.%1$s = %1$s;", AFTER_VALUE),
                "criteria.add(c);",
                "return (Criteria)this;"
            )
        ));

        MBGenerator.addStringField(criterion, AFTER_VALUE);
        MBGenerator.addStringField(criterion, BEFORE_VALUE);
    }

    private void and_a_op_b(InnerClass generatedCriteria, String method, String operator, String nullFn) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, method, _(STRING, "field"), _(OBJECT, "value"), __(
                _("if (value == null) return %s(field);", nullFn),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                _("addCriterion(field+` %s `, value, field);", operator),
                "return (Criteria)this;"
        )));
    }

    private void and_a_op_null(InnerClass generatedCriteria, String method, String operator) {
        generatedCriteria.addMethod(method(
        PUBLIC, CRITERIA, method, _(STRING, "field"), __(
            _("addCriterion(field+` %s null`);", operator),
            "return (Criteria)this;"
        )));
    }

    private void andRightIf(InnerClass generatedCriteria, InnerClass criterion) {
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andRightIf", _(OBJECT, "value"), _(STRING, "operator"), _(STRING, "field"), __(
                "return andRightIf(value, operator, field, null, null);"
        )));

        String invalidConditionIdent = "`+field.replaceAll(`[^0-9A-Za-z _.()]`,``)+`";
        generatedCriteria.addMethod(method(
            PUBLIC, CRITERIA, "andRightIf", __(
                _(OBJECT, "value"), _(STRING, "operator"), _(STRING, "field"), _(STRING, BEFORE_VALUE), _(STRING, AFTER_VALUE)
            ), __(
                "if (field == null || field.trim().length() == 0) { field = `null`; }",
                _("if (operator == null) { addCriterion(`1=2 /* %s.operator */ `); return (Criteria)this; }", invalidConditionIdent),
                _("if (value == null) { addCriterion(`1=2 /* %s.value */ `); return (Criteria)this; }", invalidConditionIdent),
                "if (value instanceof java.util.Date) value = new java.sql.Date(((java.util.Date)value).getTime());",
                "String condition = ` `+operator+` `+field;",
                "Criterion c = new Criterion(condition, value);",
                _("if (%1$s != null) c.%1$s = %1$s;", BEFORE_VALUE),
                _("if (%1$s != null) c.%1$s = %1$s;", AFTER_VALUE),
                _("c.%s = true;", RIGHT_VALUE),
                "criteria.add(c);",
                "return (Criteria)this;"
            )
        ));

        MBGenerator.addStringField(criterion, AFTER_VALUE);
        MBGenerator.addStringField(criterion, BEFORE_VALUE);
        MBGenerator.addBoolField(criterion, RIGHT_VALUE);
    }

    private void andIn(InnerClass generatedCriteria, TopLevelClass topLevelClass) {
        List<Method> newMethods = new ArrayList<Method>();
        boolean toInteger = false;
        boolean toLong = false;
        for (Method m : generatedCriteria.getMethods()) {
            String name = m.getName();
            if (!name.endsWith("In") && !name.endsWith("NotIn")) continue;
            FullyQualifiedJavaType arg = m.getParameters().get(0).getType();
            if (arg.getShortName().endsWith("<Integer>")) {
                toInteger = overloadAndXxIn(newMethods, m, int[].class, null, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, long[].class, null, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, Number[].class, null, Integer.class) || toInteger;
                //toInteger = overloadAndXxIn(newMethods, m, List.class, Long.class, Integer.class) || toInteger;
                toInteger = overloadAndXxIn(newMethods, m, Set.class, Number.class, Integer.class) || toInteger;
            }
            else if (arg.getShortName().endsWith("<Long>")) {
                toLong = overloadAndXxIn(newMethods, m, int[].class, null, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, long[].class, null, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, Number[].class, null, Long.class) || toLong;
                //toLong = overloadAndXxIn(newMethods, m, List.class, Integer.class, Long.class) || toLong;
                toLong = overloadAndXxIn(newMethods, m, Set.class, Number.class, Long.class) || toLong;
            }
        }
        if (!newMethods.isEmpty()) {
            if (toInteger) addToIntTypeConverters(generatedCriteria);
            if (toLong) addToLongTypeConverters(generatedCriteria);
            for (Method m : newMethods)
                generatedCriteria.addMethod(m);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Set"));
        }
    }

    private boolean overloadAndXxIn(List<Method> list, Method base, Class<?> arg, Class<?> item, Class<?> to) {
        StringBuilder sb = new StringBuilder();
        String typeName = null, componentTypeName = null;
        if (arg.isArray()) {
            sb.append("array");
            item = arg.getComponentType();
        } else if (arg == List.class) {
            sb.append("list");
            typeName = "List";
        } else if (arg == Set.class) {
            sb.append("set");
            typeName = "Set";
        }
        sb.append("Of");
        if (item == int.class) {
            sb.append("Int");
            componentTypeName = "int";
        }
        else if (item == Integer.class) {
            sb.append("Integer");
            componentTypeName = "java.lang.Integer";
        }
        else if (item == long.class) {
            sb.append("Long");
            componentTypeName = "long";
        } else if (item == Long.class) {
            sb.append("Long");
            componentTypeName = "java.lang.Long";
        } else if (item == Number.class) {
            sb.append("Number");
            componentTypeName = "java.lang.Number";
        }

        if (!arg.isArray() && componentTypeName != null) {
            if (Modifier.isAbstract(item.getModifiers()))
                componentTypeName = "? extends " + componentTypeName;
        }

        sb.append("ToListOf");
        if (to == int.class || to == Integer.class)
            sb.append("Integer");
        else if (to == long.class || to == Long.class)
            sb.append("Long");

        String converter = sb.toString();
        Method m = new Method();
        m.setName(base.getName());
        m.setReturnType(base.getReturnType());
        m.setVisibility(base.getVisibility());

        Parameter param = base.getParameters().get(0);
        FullyQualifiedJavaType type = null;
        if (typeName == null) {
            if (componentTypeName == null) return false;
            type = new JavaArray(componentTypeName);
        }
        else if (componentTypeName != null) {
            type = new FullyQualifiedJavaType(typeName+"<"+componentTypeName+">");
        }
        else {
            type = new FullyQualifiedJavaType(typeName);
        }
        m.addParameter(new Parameter(type, param.getName()));
        m.addBodyLine("return "+m.getName()+"("+converter+"("+param.getName()+"));");
        list.add(m);
        return true;
    }

    private void addToIntTypeConverters(InnerClass generatedCriteria) {
        String collectionOfWrappers =
            "if (values != null) " +
            "for (%s i : values) " +
                "if (i != null && i.longValue() >= Integer.MIN_VALUE && i.longValue() <= Integer.MAX_VALUE) " +
                    "list.add(i.intValue());";

        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfIntToListOfInteger", _(new JavaArray("int"), "values"), __(
                "List<Integer> list = new ArrayList<Integer>();",
                "if (values != null) for (int i : values) list.add(i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfLongToListOfInteger", _(new JavaArray("long"), "values"), __(
                "List<Integer> list = new ArrayList<Integer>();",
                "if (values != null) for (long i : values) if (i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE) list.add((int)i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "arrayOfNumberToListOfInteger", _(new JavaArray("java.lang.Number"), "values"), __(
                "List<Integer> list = new ArrayList<Integer>();",
                _(collectionOfWrappers, "Number"),
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "setOfNumberToListOfInteger", _(new FullyQualifiedJavaType("java.util.Set<? extends java.lang.Number>"), "values"), __(
                "List<Integer> list = new ArrayList<Integer>();",
                _(collectionOfWrappers, "Number"),
                "return list;"
        )));
        /*
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_INTEGER, "listOfLongToListOfInteger", _(new FullyQualifiedJavaType("java.util.List<java.lang.Long>"), "values"), __(
                "List<Integer> list = new ArrayList<Integer>();",
                _(collectionOfWrappers, "Long"),
                "return list;"
        )));
        */
    }

    private void addToLongTypeConverters(InnerClass generatedCriteria) {
        String collectionOfWrappers = "if (values != null) for (%s i : values) if (i != null) list.add(i.longValue());";
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfIntToListOfLong", _(new JavaArray("int"), "values"), __(
                "List<Long> list = new ArrayList<Long>();",
                "if (values != null) for (int i : values) list.add((long)i);",
                "return list;"
        )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfLongToListOfLong", _(new JavaArray("long"), "values"), __(
                "List<Long> list = new ArrayList<Long>();",
                "if (values != null) for (long i : values) list.add(i);",
                "return list;"
            )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "arrayOfNumberToListOfLong", _(new JavaArray("java.lang.Number"), "values"), __(
                "List<Long> list = new ArrayList<Long>();",
                _(collectionOfWrappers, "Number"),
                "return list;"
            )));
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "setOfNumberToListOfLong", _(new FullyQualifiedJavaType("java.util.Set<? extends java.lang.Number>"), "values"), __(
                "List<Long> list = new ArrayList<Long>();",
                _(collectionOfWrappers, "Number"),
                "return list;"
            )));
        /*
        generatedCriteria.addMethod(method(
            PRIVATE, LIST_OF_LONG, "listOfIntegerToListOfLong", _(new FullyQualifiedJavaType("java.util.List<? extends java.lang.Integer>"), "values"), __(
                "List<Long> list = new ArrayList<Long>();",
                _(collectionOfWrappers, "Integer"),
                "return list;"
        )));
        */
    }

    private static class JavaArray extends FullyQualifiedJavaType {
        private JavaArray(String fullTypeSpecification) {
            super(fullTypeSpecification);
        }

        @Override
        public String getFullyQualifiedName() {
            return super.getFullyQualifiedName()+"[]";
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public PrimitiveTypeWrapper getPrimitiveTypeWrapper() {
            return null;
        }

        @Override
        public String getShortName() {
            return super.getShortName()+"[]";
        }
    }

    public boolean andIf(XmlElement element, IntrospectedTable introspectedTable) {
        return traverse(element, new ReplaceText(
            new ConTextReplacer(
                "#\\{([^\\}]+?)\\.value\\}",
                "\\${$1."+BEFORE_VALUE+"}$0\\${$1."+AFTER_VALUE+"}",
                ancestors("when").and((Predicate<Stack<XmlElement>>) attrMatches("test", "^.*\\.singleValue$"))
            ), null
        )) > 0;
    }

    public boolean andRightIf(XmlElement element, IntrospectedTable introspectedTable) {
        return traverse(element, new MBGenerator.FindElements() {
            @Override
            public boolean process(XmlElement parent, Element self, int position) {
                String test = Str.trim(getAttribute((XmlElement) self, "test"));
                if (test == null)
                    return false;
                String item = Str.group(test, "^(.*)\\.singleValue$");
                if (item == null)
                    return false;
                String text = Str.trim(getTextContent((XmlElement) self));
                String op = Str.group(text, "^(.+?)\\s*[\\#\\$]\\{.+$");
                if (op == null)
                    return false; // unable to get operator ("and", "or")
                addLater(parent, position,
                    e("when",
                        a("test", item+"."+RIGHT_VALUE),
                        String.format("%s ${%s.%s}#{%2$s.value}${%2$s.%4$s} ${%2$s.condition}", op, item, BEFORE_VALUE, AFTER_VALUE)
                    )
                );
                return true;
            }
        }.when(ELEMENT.and(ancestorsAndSelf("when")).and(attrMatches("test", "^.*\\.singleValue$")))) > 0;
    }
}
