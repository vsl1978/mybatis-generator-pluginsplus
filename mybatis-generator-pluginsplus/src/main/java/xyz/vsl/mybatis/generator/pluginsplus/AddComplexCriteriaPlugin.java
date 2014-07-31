package xyz.vsl.mybatis.generator.pluginsplus;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * <p>Adds custom criterion to specified *Example-class. Custom criterion is a combination of another criteria.</p>
 * <p>Configuration parameters:<dl>
 *     <dt>targetTable</dt>
 *     <dd><b>(Required)</b> A regular expression that specifies table to be processed</dd>
 *     <dt>methodName</dt>
 *     <dd><b>(Required)</b> New method's name</dd>
 *     <dt>methodBody</dt>
 *     <dd>New method's body (without {@code return} statement</dd>
 * </dl></p>
 * <p>Example configuration:</p>
 * <pre>
 * &lt;plugin type="xyz.vsl.mybatis.generator.pluginsplus.AddComplexCriteriaPlugin"&gt;
 *     &lt;property name="targetTable" value="(?i)orders"/&gt;
 *     &lt;property name="methodName" value="andIsLocalShipping"/&gt;
 *     &lt;property name="methodBody" value="andShipcountryEqualTo(&amp;quot;USA&amp;quot;); andShipstateEqualTo(&amp;quot;CA&amp;quot;); andShipcityEqualTo(&amp;quot;Los Angeles&amp;quot;); "/&gt;
 * &lt;/plugin&gt;
 * </pre>
 * <p>This will add to OrdersExample.java the following method:</p>
 * <pre>
 * protected abstract static class GeneratedCriteria {
 *     ....
 *     public Criteria andIsLocalShipping() {
 *         andShipcountryEqualTo("USA"); andShipstateEqualTo("CA"); andShipcityEqualTo("Los Angeles");
 *         return (Criteria)this;
 *     }
 * </pre>
 *
 * @author Vladimir Lokhov
 */
public class AddComplexCriteriaPlugin extends PluginAdapter {
    private Pattern target;
    private String methodName;
    private String methodBody;

    @Override
    public boolean validate(List<String> warnings) {
        String target = properties.getProperty("targetTable");
        if (target == null) {
            warnings.add(Messages.load(this).get("requiredProperty", "targetTable"));
            return false;
        }
        this.target = Pattern.compile(target);

        methodName = properties.getProperty("methodName");
        if (methodName == null) {
            warnings.add(Messages.load(this).get("requiredProperty", "methodName"));
            return false;
        }
        methodBody = properties.getProperty("methodBody");
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String table = MBGenerator.tableName(introspectedTable);
        if (!target.matcher(table).matches())
            return true;

        InnerClass generatedCriteria = null;

        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
                break;
            }
        }
        if (generatedCriteria == null) return false;

        Method m = new Method();
        m.setName(methodName);
        m.setVisibility(JavaVisibility.PUBLIC);
        m.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        if (methodBody != null)
            m.addBodyLine(methodBody);
        m.addBodyLine("return (Criteria)this;");
        generatedCriteria.addMethod(m);

        return true;
    }
}
