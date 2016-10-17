package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.*;
import java.util.regex.Pattern;

import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;
import static org.mybatis.generator.api.dom.java.JavaVisibility.*;

/**
 * <p>Provides the ability to store modified fields (<b>including {@code null}-values</b>) using insertSelective and updateBy*Selective methods.</p>
 * <p>Plugin's properties:</p><ul>
 *     <li><i>(optional)</i> tables &mdash; regular expression that specifies tables to process</li>
 *     <li><i>(optional)</i> excludeTables &mdash; regular expression that excludes tables</li>
 *     <li><i>(optional)</i> insertSelectiveWithNullMethodName &mdash; specifies name of the new insert method in the Mapper interface. Default value is '<tt>insertSelectiveWithNull</tt>'.<br>If property value is '<tt>insertSelective</tt>' then existing method's behaviour will be changed. This method will be generated (or modified) if the insert statement is enabled.</li>
 *     <li><i>(optional)</i> updateByExampleSelectiveWithNullMethodName &mdash; specifies name of the new update method in the Mapper interface. Default value is '<tt>updateByExampleSelectiveWithNull</tt>'.<br>If property value is '<tt>updateByExampleSelective</tt>' then existing method's behaviour will be changed. This method will be generated (or modified) if the update by primary key statement is enabled.</li>
 *     <li><i>(optional)</i> updateByPrimaryKeySelectiveWithNullMethodName &mdash; specifies name of the new update method in the Mapper interface. Default value is '<tt>updateByPrimaryKeySelectiveWithNull</tt>'.<br>If property value is '<tt>updateByPrimaryKeySelective</tt>' then existing method's behaviour will be changed. This method will be generated (or modified) if the update by example statement is enabled.</li>
 * </ul>
 * <p>&nbsp;</p>
 * <p>Supported Java Client generators:<br>
 * <b>ANNOTATEDMAPPER</b>: not supported<br>
 * <b>MIXEDMAPPER</b>: not supported<br>
 * <b>XMLMAPPER</b>: supported<br>
 * </p>
 * @author Vladimir Lokhov
 */
public class SelectiveWithNullPlugin extends IntrospectorPlugin {
    private final static String SKIP = "-";
    private final static String BASE_INSERT = "insertSelective";
    private final static String BASE_UPDATE_BY_EXAMPLE = "updateByExampleSelective";
    private final static String BASE_UPDATE_BY_PK = "updateByPrimaryKeySelective";

    private final static String SUFFIX = "WithNull"; // ""

    private String insertMethod;
    private String updateByExampleMethod;
    private String updateByPKMethod;

    private String fieldName;
    private String getterName;
    private String resetMethodName;

    private XmlElement insertElement;
    private XmlElement updateByExampleElement;
    private XmlElement updateByPKElement;

    private Pattern include;
    private Pattern exclude;

    @Override
    public boolean validate(List<String> warnings) {
        include = Pattern.compile(Objects.nvl(Str.trim(properties.getProperty("tables")), ".+"));
        String ex = Str.trim(properties.getProperty("excludeTables"));
        if (ex != null)
            exclude = Pattern.compile(ex);
        insertMethod = Objects.nvl(Str.trim(properties.getProperty("insertSelectiveWithNullMethodName")), BASE_INSERT+SUFFIX);
        updateByExampleMethod = Objects.nvl(Str.trim(properties.getProperty("updateByExampleSelectiveWithNullMethodName")), BASE_UPDATE_BY_EXAMPLE+SUFFIX);
        updateByPKMethod = Objects.nvl(Str.trim(properties.getProperty("updateByPrimaryKeySelectiveWithNullMethodName")), BASE_UPDATE_BY_PK+SUFFIX);
        fieldName = "$modified";
        getterName = "modifications";
        resetMethodName = "resetModifications";
        return true;
    }

    private boolean matched(IntrospectedTable introspectedTable) {
        String t = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        return include.matcher(t).matches() && (exclude == null || !exclude.matcher(t).matches());
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (introspectedTable.hasPrimaryKeyColumns() && introspectedTable.getRules().generatePrimaryKeyClass()) {
            generateInnerClass(topLevelClass, introspectedTable);
        }
        generateResetMethod(topLevelClass);
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (introspectedTable.hasPrimaryKeyColumns() && introspectedTable.getRules().generatePrimaryKeyClass()) {
        }
        else if (introspectedTable.hasBaseColumns() && introspectedTable.getRules().generateBaseRecordClass()) {
            generateInnerClass(topLevelClass, introspectedTable);
        }
        generateResetMethod(topLevelClass);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (introspectedTable.hasPrimaryKeyColumns() && introspectedTable.getRules().generatePrimaryKeyClass()) {
        }
        else if (introspectedTable.hasBaseColumns() && introspectedTable.getRules().generateBaseRecordClass()) {
        }
        else if (introspectedTable.hasBLOBColumns() && introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            generateInnerClass(topLevelClass, introspectedTable);
        }
        generateResetMethod(topLevelClass);
        return true;
    }

    private void generateResetMethod(TopLevelClass topLevelClass) {
        topLevelClass.addMethod(method(PUBLIC, topLevelClass.getType(), resetMethodName, format("%s = new Fields(); return this;", fieldName)));
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (!updateByExampleMethod.equals(SKIP) && !updateByExampleMethod.equals(BASE_UPDATE_BY_EXAMPLE) && introspectedTable.getTableConfiguration().isUpdateByExampleStatementEnabled()) {
            interfaze.addMethod(method(PUBLIC, method.getReturnType(), updateByExampleMethod, parameters(method)));
        }
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (!updateByPKMethod.equals(SKIP) && !updateByPKMethod.equals(BASE_UPDATE_BY_PK) && introspectedTable.getTableConfiguration().isUpdateByPrimaryKeyStatementEnabled()) {
            interfaze.addMethod(method(PUBLIC, method.getReturnType(), updateByPKMethod, parameters(method)));
        }
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (!insertMethod.equals(SKIP) && !insertMethod.equals(BASE_INSERT) && introspectedTable.getTableConfiguration().isInsertStatementEnabled()) {
            interfaze.addMethod(method(PUBLIC, method.getReturnType(), insertMethod, parameters(method)));
        }
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (!matched(introspectedTable)) return true;
        method.addBodyLine(0, fieldName + "." + introspectedColumn.getJavaProperty() + " = true;");
        return true;
    }

    private void generateInnerClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass cls = new InnerClass("Fields");
        cls.setVisibility(PUBLIC);
        cls.setStatic(true);
        for (IntrospectedColumn c : introspectedTable.getAllColumns()) {
            String field = c.getJavaProperty();
            cls.addField(field(PROTECTED, BOOL, field));
            cls.addMethod(method(PUBLIC, BOOL, "is"+camel(field), format("return %s;", field)));
        }
        topLevelClass.addInnerClass(cls);
        topLevelClass.addField(field(PROTECTED, TRANSIENT, cls.getType(), fieldName, format("new Fields()")));
        topLevelClass.addMethod(method(PUBLIC, cls.getType(), getterName, format("return %s;", fieldName)));
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (updateByExampleMethod.equals(SKIP) || !introspectedTable.getTableConfiguration().isUpdateByExampleStatementEnabled()) {
        }
        else if (updateByExampleMethod.equals(BASE_UPDATE_BY_EXAMPLE)) {
            changeConditions(element);
        } else {
            updateByExampleElement = element;
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (updateByPKMethod.equals(SKIP) || !introspectedTable.getTableConfiguration().isUpdateByPrimaryKeyStatementEnabled()) {
        }
        else if (updateByPKMethod.equals(BASE_UPDATE_BY_PK)) {
            changeConditions(element);
        } else {
            updateByPKElement = element;
        }
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (insertMethod.equals(SKIP) || !introspectedTable.getTableConfiguration().isInsertStatementEnabled()) {
        }
        else if (insertMethod.equals(BASE_INSERT)) {
            changeConditions(element);
        } else {
            insertElement = element;
        }
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        if (!matched(introspectedTable)) return true;
        if (insertElement != null || updateByExampleElement != null || updateByPKElement != null) {
            XmlElement root = getMapperXmlRoot(sqlMap);
            copyAndChangeConditions(root, insertElement, insertMethod);
            copyAndChangeConditions(root, updateByExampleElement, updateByExampleMethod);
            copyAndChangeConditions(root, updateByPKElement, updateByPKMethod);
            insertElement = null;
            updateByExampleElement = null;
            updateByPKElement = null;
        }
        return true;
    }

    private void copyAndChangeConditions(XmlElement root, XmlElement element, String method) {
        if (element == null || root == null) return;
        root.addElement(setAttribute(changeConditions(traverse(element, new CopyXml())), "id", method));
    }

    private XmlElement changeConditions(XmlElement e) {
        boolean update = "update".equals(e.getName());
        traverse(e,
            new FindElements() {
                @Override
                public boolean process(XmlElement parent, Element self, int position) {
                    XmlElement _if = (XmlElement)self;
                    Pair<String, String> field = Str.groups(getAttribute(_if, "test"), "^(.+\\.)?([^\\.]+?)\\s*!=\\s*null$", 1, 2);
                    if (field.b() == null) return false;
                    setAttribute(_if, "test", Objects.nvl(field.a(), "_parameter.")+getterName+"()."+field.b());
                    return true;
                }
            }.when(ELEMENT.and(ancestorsAndSelf(update ? "set" : "trim", "if")))
        );
        return e;
    }
}
