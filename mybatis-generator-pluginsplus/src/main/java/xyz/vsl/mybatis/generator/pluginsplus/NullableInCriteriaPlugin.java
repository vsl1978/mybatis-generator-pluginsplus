package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * Modifies Example class to allow using {@code null} and empty lists as parameter of {@code andXxxIn(List)} and {@code andXxxNotIn(List)} methods.
 * @author Vladimir Lokhov
 */
public class NullableInCriteriaPlugin extends PluginAdapter {
    private final static String SKIP    = "-";

    private String falseInCondition;
    private String falseNotInCondition;
    private boolean addComment;

    @Override
    public boolean validate(List<String> warnings) {
        falseInCondition = Objects.nvl(Str.trim(properties.getProperty("falseInCondition")), "1=2");
        falseNotInCondition = Objects.nvl(Str.trim(properties.getProperty("falseNotInCondition")), "1=1");
        addComment = Bool.bool(Str.trim(properties.getProperty("addComment")), true);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass gcriteria = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                gcriteria = innerClass;
                break;
            }
        }
        if (gcriteria != null) {
            for (Method m : gcriteria.getMethods()) {
                String name = m.getName();
                String condition;
                if (name.endsWith("NotIn")) {
                    condition = falseNotInCondition;
                }
                else if (name.endsWith("In")) {
                    condition = falseInCondition;
                }
                else continue;
                String p = m.getParameters().get(0).getName();
                String t = m.getParameters().get(0).getType().getFullyQualifiedName();
                StringBuilder sb = new StringBuilder();
                sb.append("if (").append(p).append(" == null");
                if (t.endsWith("[]"))
                    sb.append(" || ").append(p).append(".length == 0");
                else
                    sb.append(" || ").append(p).append(".isEmpty()");
                sb.append(") {");
                sb.append("addCriterion(\"").append(condition);
                if (addComment)
                    sb.append(" /* ").append(name).append(" */");
                sb.append("\");");
                sb.append(m.getBodyLines().get(m.getBodyLines().size() - 1)); // add return
                sb.append("}");
                m.addBodyLine(0, sb.toString());
            }
        }

        return true;
    }


}