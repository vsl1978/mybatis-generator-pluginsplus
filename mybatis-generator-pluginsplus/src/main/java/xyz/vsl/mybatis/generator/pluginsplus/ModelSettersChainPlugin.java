package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;

/**
 * Transforms Model setters from {@code "public void setXxx(...)"} to {@code "public Model setFoo(...)"} to provide the ability to use method chaining.
 * @author Vladimir Lokhov
 */
public class ModelSettersChainPlugin extends PluginAdapter {
    private boolean generateNewSetters;
    private MethodNameGenerator gen;

    private interface MethodNameGenerator {
        public String name(String field);
    }

    private class NoPrefix implements MethodNameGenerator {
        @Override
        public String name(String field) {
            return field;
        }
    }

    private class Prefix implements MethodNameGenerator {
        private String prefix;
        public Prefix(String prefix) {
            this.prefix = prefix;
        }
        @Override
        public String name(String field) {
            return prefix + camel(field);
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        String prefix = Objects.nvl(properties.getProperty("prefix"), "set");
        if (prefix == null || prefix.trim().length() == 0)
            gen = new NoPrefix();
        else
            gen = new Prefix(prefix);
        generateNewSetters = !"set".equals(prefix);
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return generateNewSetters ? generateSetters(topLevelClass, introspectedTable) : true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return generateNewSetters ? generateSetters(topLevelClass, introspectedTable) : true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return generateNewSetters ? generateSetters(topLevelClass, introspectedTable) : true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (generateNewSetters) return true;
        method.setReturnType(topLevelClass.getType());
        method.addBodyLine("return this;");
        return true;
    }

    private boolean generateSetters(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            columns = introspectedTable.getNonBLOBColumns();
        } else {
            columns = introspectedTable.getAllColumns();
        }

        for (IntrospectedColumn introspectedColumn : columns) {
            FullyQualifiedJavaType column = introspectedColumn.getFullyQualifiedJavaType();
            String field = introspectedColumn.getJavaProperty();
            topLevelClass.addMethod(method(
                PUBLIC, topLevelClass.getType(), gen.name(field), _(column, field), __(
                    "this."+field+" = "+field+";",
                    "return this;"
            )));
        }

        return true;
    }

}
