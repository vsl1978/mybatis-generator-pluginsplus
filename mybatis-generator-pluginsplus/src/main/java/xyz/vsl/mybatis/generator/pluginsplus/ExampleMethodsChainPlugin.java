package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;

/**
 * Transforms top-level methods of a XxxExample class from {@code "public void foo(...)"} to {@code "public XxxExample foo(...)"} to provide the ability to use method chaining. Adds two new methods to XxxExample class:
 * <ul>
 *     <li>top-level method <b>{@code current()}</b> returns current (last created) oredCriteria or {@code createCriteria()} if {@code createCriteria()} was not called. The name of the method can be configured by setting {@code topLevelMethodName} plugin's property.</li>
 *     <li>XxxExample$Criteria's method <b>{@code criteria()}</b> returns corresponding XxxExample class. The name of the method can be configured by setting {@code innerMethodName} plugin's property.</li>
 * </ul>
 * Transformed methods:
 * <ol>
 *     <li>clear()</li>
 *     <li>or(Criteria criteria)</li>
 *     <li>setDistinct(boolean distinct)</li>
 *     <li>setOrderByClause(String orderBy)</li>
 * </ol>
 *
 * @author Vladimir Lokhov
 */
public class ExampleMethodsChainPlugin extends PluginAdapter {
    private final static String SKIP    = "-";
    public static final String OWNER = "_owner";

    private String topLevelMethodName;
    private String innerMethodName;
    private String setOrderByMethodName;
    private String setDistinctMethodName;

    @Override
    public boolean validate(List<String> warnings) {
        innerMethodName = Objects.nvl(Str.trim(properties.getProperty("innerMethodName")), "criteria");
        topLevelMethodName = Objects.nvl(Str.trim(properties.getProperty("topLevelMethodName")), "current");
        setOrderByMethodName = Objects.nvl(Str.trim(properties.getProperty("setOrderByMethodName")), "setOrderByClause");
        setDistinctMethodName = Objects.nvl(Str.trim(properties.getProperty("setDistinctMethodName")), "setDistinct");
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (SKIP.equals(innerMethodName))
            return true;

        topLevelClass.addMethod(method(
            PUBLIC, new FullyQualifiedJavaType("Criteria"), topLevelMethodName, body(
                "if (oredCriteria == null || oredCriteria.isEmpty()) return createCriteria();",
                "return oredCriteria.get(oredCriteria.size() - 1);"
        )));

        boolean injected = false;
        List<String> cciLines = null;
        for (Method m : topLevelClass.getMethods()) {
            if (!"createCriteriaInternal".equals(m.getName())) continue;
            injected = true;
            cciLines = m.getBodyLines();
            cciLines.add(cciLines.size() - 1, "criteria."+ innerMethodName +"(this);");
            break;
        }
        InnerClass criteria = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                criteria = innerClass;
                break;
            }
        }

        if (injected) {
            if (criteria != null) {
                criteria.addField(field(PRIVATE, topLevelClass.getType(), OWNER));
                criteria.addMethod(method(PUBLIC, topLevelClass.getType(), innerMethodName, format("return %s;", OWNER)));
                criteria.addMethod(method(PRIVATE, VOID, innerMethodName, param(topLevelClass.getType(), "owner"), format("this.%s = owner;", OWNER)));
            } else {
                cciLines.remove(cciLines.size() - 2);
                injected = false;
            }
        }

        if (!SKIP.equals(setOrderByMethodName)) {
            Method m = getDeclaredMethod(topLevelClass, setOrderByMethodName, STRING);
            if (m != null) {
                if (Objects.equals(m.getReturnType(), VOID)) {
                    m.setReturnType(topLevelClass.getType());
                    m.addBodyLine("return this;");
                }
            } else {
                topLevelClass.addMethod(method(
                    PUBLIC, topLevelClass.getType(), setOrderByMethodName, param(STRING, "orderBy"), body(
                        "this.orderByClause = orderBy;",
                        "return this;"
                )));
            }
        }

        if (!SKIP.equals(setDistinctMethodName)) {
            Method m = getDeclaredMethod(topLevelClass, setDistinctMethodName, BOOL);
            if (m != null) {
                if (Objects.equals(m.getReturnType(), VOID)) {
                    m.setReturnType(topLevelClass.getType());
                    m.addBodyLine("return this;");
                }
            } else {
                topLevelClass.addMethod(method(
                    PUBLIC, topLevelClass.getType(), setDistinctMethodName, param(BOOL, "distinct"), body(
                        "this.distinct = distinct;",
                        "return this;"
                )));
            }
        }

        Method m = getDeclaredMethod(topLevelClass, "clear");
        if (m != null && Objects.equals(m.getReturnType(), VOID)) {
            m.setReturnType(topLevelClass.getType());
            m.addBodyLine("return this;");
        }

        m = criteria != null ? getDeclaredMethod(topLevelClass, "or", criteria.getType()) : null;
        if (m != null && Objects.equals(m.getReturnType(), VOID)) {
            m.setReturnType(topLevelClass.getType());
            m.addBodyLine("return this;");
        }

        return true;
    }


}
