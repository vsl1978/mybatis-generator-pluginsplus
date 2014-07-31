package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;

/**
 * <p>Adds to Model class new static methods {@code Map&lt;field_type, Model&gt; mapByXxx(Collection&lt;Model&gt;)} and {@code Map&lt;field_type, List&lt;Model&gt;&gt; mapAllByXxx(Collection&lt;Model&gt;)}  aimed at grouping selected records by value of short-, int-, long- and String-fields.</p>
 * <p>Configuration parameters:</p>
 * <ul>
 *     <li><i>(optional)</i> {@code mapMethodPrefix} &mdash; the new {@code mapByXxx}-method's prefix. Default value is {@code "mapBy"}</li>
 *     <li><i>(optional)</i> {@code mapAllMethodPrefix} &mdash; the new {@code mapAllByXxx}-method's prefix. Default value is {@code "mapAllBy"}</li>
 *     <li><i>(optional)</i> {@code mappedVarcharColumns} &mdash; regular expression that specifies the list of varchar columns to generate {@code mapByXxx} and {@code mapAllByXxx} methods.</li>
 * </ul>
 * <p>Table configuration properties:</p>
 * <ul>
 *     <li><i>(optional)</i> {@code mappedVarcharColumns} &mdash; list of varchar column names (case insensitive, separated by comma or semicolon) to generate {@code mapByXxx} and {@code mapAllByXxx} methods</li>
 * </ul>
 * @author Vladimir Lokhov
 */
public class MapPlugin extends PluginAdapter {
    private final static String SKIP                            = "-";
    private final static String DEFAULT_MAPPED_VARCHAR_COLUMNS  = "(?i)(.*[^A-Za-z]|^)(uid|name|src|code)([^A-Za-z].*|$)";
    private final static String TABLE_SPECIFIC_PREFIX           = "table:";

    private Pattern mappedVarcharColumns;
    private List<Pair<Pattern, Pattern>> tableSpecific = new ArrayList<Pair<Pattern, Pattern>>();
    private String mapPrefix;
    private String mapAllPrefix;

    @Override
    public boolean validate(List<String> warnings) {
        mappedVarcharColumns = Pattern.compile(Objects.nvl(Str.trim(properties.getProperty("mappedVarcharColumns")), DEFAULT_MAPPED_VARCHAR_COLUMNS));
        for (Enumeration<Object> keys = properties.keys(); keys.hasMoreElements(); ) {
            String key = (String)keys.nextElement();
            if (key.startsWith(TABLE_SPECIFIC_PREFIX))
                tableSpecific.add(Pair.of(Pattern.compile(key.substring(TABLE_SPECIFIC_PREFIX.length())), Pattern.compile(properties.getProperty(key))));
        }
        mapPrefix = Objects.nvl(Str.trim(properties.getProperty("mapMethodPrefix")), "mapBy");
        mapAllPrefix = Objects.nvl(Str.trim(properties.getProperty("mapAllMethodPrefix")), "mapAllBy");
        if (!SKIP.equals(mapPrefix) && mapPrefix.equals(mapAllPrefix)) {
            warnings.add(Messages.load(this).get("failOnEqualMethodNames", mapPrefix));
            return false;
        }
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

        generateMap(topLevelClass, columns, introspectedTable);

        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateMap(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateMap(topLevelClass, introspectedTable.getAllColumns(), introspectedTable);
        return true;
    }

    protected void generateMap(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        String shortName = topLevelClass.getType().getShortName();
        assert shortName != null;

        String table = MBGenerator.tableName(introspectedTable);

        String selfType = topLevelClass.getType().getFullyQualifiedName();
        FullyQualifiedJavaType collection = new FullyQualifiedJavaType("java.util.Collection<"+selfType+">");

        boolean generated = false;

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
            String property = introspectedColumn.getJavaProperty();
            String getterMethod = JavaBeansUtil.getGetterMethodName(property, fqjt);
            String fqTypeName = fqjt.getFullyQualifiedName();
            String column = introspectedColumn.getActualColumnName();
            String targetType = null;
            if ("int".equals(fqTypeName) || "java.lang.Integer".equals(fqTypeName)) {
                targetType = "java.lang.Integer";
            } else if ("long".equals(fqTypeName) || "java.lang.Long".equals(fqTypeName))
                targetType = "java.lang.Long";
            else if ("short".equals(fqTypeName) || "java.lang.Short".equals(fqTypeName))
                targetType = "java.lang.Short";
            else if ("string".equals(fqTypeName.toLowerCase()) || "java.lang.String".equals(fqTypeName)) {
                if (mappedVarcharColumns.matcher(column).matches())
                    targetType = "java.lang.String";
                for (Pair<Pattern, Pattern> rule : tableSpecific) {
                    if (targetType == null && rule.a().matcher(table).matches())
                        if (rule.b().matcher(column).matches())
                            targetType = "java.lang.String";
                }
                String tableConfiguration = introspectedTable.getTableConfigurationProperty("mappedVarcharColumns");
                if (targetType == null && tableConfiguration != null)
                    for (StringTokenizer st = new StringTokenizer(tableConfiguration, ",; \t\r\n", false); st.hasMoreTokens(); ) {
                        if (column.equalsIgnoreCase(st.nextToken()))
                            targetType = "java.lang.String";
                    }
            }

            if (targetType == null)
                continue;

            String tt = targetType.startsWith("java.lang.") ? targetType.substring("java.lang.".length()) : targetType;
            String uProperty = camel(property);

            if (!SKIP.equals(mapPrefix)) {
                FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("java.util.Map<" + targetType + ", " + selfType + ">");
                topLevelClass.addMethod(method(
                    PUBLIC, STATIC, returnType, mapPrefix + uProperty, _(collection, "beans"), __(
                        _("Map<%s, %s> map = new LinkedHashMap<%1$s,%2$s>();", tt, shortName),
                        "if (beans == null || beans.isEmpty()) return map;",
                        _("for (%s bean : beans) {", shortName),
                        _("map.put(bean.%s(), bean);", getterMethod),
                        "}",
                        "return map;"
                )));
                generated = true;
            }

            if (!SKIP.equals(mapAllPrefix)) {
                FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("java.util.Map<" + targetType + ", java.util.List<" + selfType + ">>");
                topLevelClass.addMethod(method(
                    PUBLIC, STATIC, returnType, mapAllPrefix + uProperty, _(collection, "beans"), __(
                        _("Map<%s, List<%s>> map = new LinkedHashMap<%1$s, List<%2$s>>();", tt, shortName),
                        "if (beans == null || beans.isEmpty()) return map;",
                        _("for (%s bean : beans) {", shortName),
                            _("%s v = bean.%s();", tt, getterMethod),
                            _("List<%s> list = map.get(v);", shortName),
                            _("if (list == null) map.put(v, list = new ArrayList<%s>());", shortName),
                            "list.add(bean);",
                        "}",
                        "return map;"
                )));
                generated = true;
            }
        }

        if (generated) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Collection"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.ArrayList"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.LinkedHashMap"));
        }

    }

}
