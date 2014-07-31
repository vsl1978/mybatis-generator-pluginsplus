package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * Generates a <tt>toString</tt> method that returns a string representation of all fields.
 * 
 * @author Vladimir Lokhov
 * 
 */
public class ToStringPlugin extends PluginAdapter {

    public ToStringPlugin() {
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            columns = introspectedTable.getNonBLOBColumns();
        } else {
            columns = introspectedTable.getAllColumns();
        }

        generateToString(topLevelClass, columns, introspectedTable);

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateToString(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateToString(topLevelClass, introspectedTable.getAllColumns(), introspectedTable);

        return true;
    }

    protected void generateToString(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("toString");
        if (introspectedTable.isJava5Targeted()) method.addAnnotation("@Override");

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.addBodyLine("StringBuilder sb = new StringBuilder();");
        method.addBodyLine("sb.append(\""+topLevelClass.getType().getShortName()+" [\");");

        boolean first = true;
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType fqJavaType = introspectedColumn.getFullyQualifiedJavaType();
            String getterMethod = JavaBeansUtil.getGetterMethodName(introspectedColumn.getJavaProperty(), fqJavaType);

            StringBuilder sb = new StringBuilder();
            sb.append(first ? "sb" : "sb.append(\", \")");
            first = false;

            sb.append(".append(\"").append(introspectedColumn.getJavaProperty()).append("=\")");

            if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                sb.append(".append(this.").append(getterMethod).append("())");
            }
            else if ("java.lang.String".equals(fqJavaType.getFullyQualifiedName())) {
                sb.append("; ");
                sb.append("if (this.").append(getterMethod).append("() == null) ");
                sb.append("sb.append(\"null\"); else sb.append('\"').append(this.").append(getterMethod).append("()).append('\"')");
            } 
            else {
                sb.append(".append(this.").append(getterMethod).append("())");
            }
            sb.append(';');

            method.addBodyLine(sb.toString());
        }
        method.addBodyLine("sb.append(\"]\");");
        method.addBodyLine("return sb.toString();");

        topLevelClass.addMethod(method);
    }
}
