package xyz.vsl.mybatis.generator.pluginsplus;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;

/**
 * <p>Adds new methods to Example class providing the ability to join another tables</p>
 * <ul>
 *     <li>{@code addFromClause(String joinExpression)}</li>
 *     <li>{@code addFromClause(String formatString, Object firstArg, Object ... secondAndFurtherArgs)}</li>
 * </ul>
 * <p>The name of the method can be configured by setting {@code methodName} plugin's property. Default value is {@code addFromClause}.</p>
 * <p>This plugin also provides the ability to fix or remove table alias from {@code deleteByExample} statement. Property
 * {@code supportsAliasInDelete} specifies how the plugin process {@code deleteByExample}:<ul>
 *     <li>{@code true} &mdash; {@code deleteByExample} is unchanged. <b>Default value</b></li>
 *     <li>{@code mssql} &mdash; modifies {@code deleteByExample} to conform SQL Server syntax (<tt>delete <i>alias</i> from <i>table</i> <i>alias</i> where ...</tt>)</li>
 *     <li>{@code false} &mdash; remove alias from {@code deleteByExample}. Adds new method {@code getUnaliasedCondition()} to the Example$Criterion class and {@code Delete_Example_Where_Clause} element to Mapper xml</li>
 * </ul>
 * </p>
 * <p>Example:<br/>
 * <tt>new FooExample().addFromClause("join foo_type ft on ft.id=foo.type_id and ft.code='bar'");</tt><br/>
 * or, using {@link xyz.vsl.mybatis.generator.pluginsplus.AnyCriteriaPlugin}:<br/>
 * <tt>new FooExample().addFromClause("join foo_type ft on ft.id=foo.type_id").createCriteria().andEqualTo("ft.code", "bar");</tt><br/>
 * or<br/>
 * <tt>new FooExample().addFromClause("join foo_type ft on ft.id=%s.type_id", FooExample.ALIAS).createCriteria().andEqualTo("ft.code", "bar");</tt><br/>
 * where {@code FooExample.ALIAS} is auto-wasGenerated field. This field is initialized to contain the table alias (&lt;table&nbsp;...&nbsp;alias="..."&gt;)
 * or table name if alias is not specified.
 * </p>
 * <p></p>
 * <p>Supported Java Client generators:<br/>
 * <b>ANNOTATEDMAPPER</b>: not supported<br/>
 * <b>MIXEDMAPPER</b>: supported<br/>
 * <b>XMLMAPPER</b>: supported<br/>
 * </p>
 * @author Vladimir Lokhov
 */
public class JoinPlugin extends IntrospectorPlugin {
    private String methodName;
    private String tableConstName;
    private String aliasConstName;
    private boolean generateExampleConst;
    private boolean generateModelConst;

    private DeleteStatement deleteStatement;

    private XmlElement exampleWhereClauseElement;

    public boolean validate(List<String> warnings) {
        methodName = Objects.nvl(Str.trim(properties.getProperty("methodName")), "addFromClause");
        tableConstName = Objects.nvl(Str.trim(properties.getProperty("tableConstName")), "TABLE");
        aliasConstName = Objects.nvl(Str.trim(properties.getProperty("aliasConstName")), "ALIAS");
        generateExampleConst = Bool.bool(Str.trim(properties.getProperty("generateExampleConst")), true);
        generateModelConst = Bool.bool(Str.trim(properties.getProperty("generateModelConst")), false);

        String delete = Objects.nvl(Str.lower(Str.trim(properties.getProperty("supportsAliasInDelete"))), "true");
        if ("false".equals(delete) || "no".equals(delete))
            deleteStatement = new NoAliasDeleteStatement();
        else if (delete.matches("(ms|microsoft)?\\s*sql\\s*server|(ms|microsoft)\\s*sql"))
            deleteStatement = new MSSQLDeleteStatement();
        else
            deleteStatement = new DeleteStatement();

        //supportsAliasInUpdate = Bool.bool(Str.trim(properties.getProperty("supportsAliasInUpdate")));

        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (generateModelConst) addTableNameConstant(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (generateModelConst) addTableNameConstant(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (generateModelConst) addTableNameConstant(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (generateExampleConst) addTableNameConstant(topLevelClass, introspectedTable);
        addFromField(topLevelClass, introspectedTable);
        fixCriteriaColumns(topLevelClass, introspectedTable);
        deleteStatement.modelExampleClassGenerated(topLevelClass, introspectedTable);

        for (Method m : topLevelClass.getMethods()) {
            if (!"clear".equals(m.getName())) continue;
            m.getBodyLines().add(0, "from = null;");
            break;
        }
        return true;
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if ("Example_Where_Clause".equals(getAttribute(element, "id")))
            exampleWhereClauseElement = element;
        return true;
    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        deleteStatement.deleteByExampleElementGenerated(element, introspectedTable);
        return true;
    }

    private void addTableNameConstant(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
         String alias = introspectedTable.getFullyQualifiedTable().getAlias();
         String table = MBGenerator.tableName(introspectedTable);
         if (!StringUtility.stringHasValue(alias))
             alias = table;
         String constName = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("tableConstName")), tableConstName);
         topLevelClass.addField(field(PUBLIC, FINAL, STATIC, STRING, constName, _("`"+table+"`")).javadoc("This field was generated by JoinPlugin", "Name of the database table represented by this class"));
         constName = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("aliasConstName")), aliasConstName);
         topLevelClass.addField(field(PUBLIC, FINAL, STATIC, STRING, constName, _("`"+alias+"`")).javadoc("This field was generated by JoinPlugin", "SQL query alias of the database table represented by this class"));
         constName = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("aliasConstName")), aliasConstName)+"_";
         topLevelClass.addField(field(PUBLIC, FINAL, STATIC, STRING, constName, _("`"+alias+".`")).javadoc("This field was generated by JoinPlugin", "SQL query alias of the database table represented by this class"));
    }

    private boolean addJoinXMLs(final XmlElement element, final IntrospectedTable introspectedTable) {
        return traverse(element, new FindElements() {
            private int count = 0;
            @Override
            public boolean process(XmlElement parent, Element self, int position) {
                if (count > 0)
                    return false;
                addLater(parent, position + 1, e(
                    "if", a("test", "from != null"),
                        e("foreach", a("collection", "from"), a("item", "join"), a("separator", " "),
                            "${join}"
                        )
                ));
                count++;
                return true;
            }
        }.when(TEXT_NODE.and(depth(2, 2)).and(textMatches("^(.*\\s)?from\\s.*$", true)))) > 0;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addJoinXMLs(element, introspectedTable);
    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addJoinXMLs(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addJoinXMLs(element, introspectedTable);
    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addTablePrefix(element, introspectedTable);
    }

    @Override
    public boolean sqlMapBlobColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addTablePrefix(element, introspectedTable);
    }

    private boolean addTablePrefix(XmlElement element, IntrospectedTable introspectedTable) {
        if (introspectedTable.getFullyQualifiedTable() != null && StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias()))
            return true;
        final String table = MBGenerator.tableName(introspectedTable);

        traverse(element, new FindElements() {
            private boolean first = true;

            @Override
            public boolean process(XmlElement parent, Element self, int position) {
                if (!(self instanceof TextElement)) return false;
                String s = ((TextElement) self).getFormattedContent(0).trim();
                StringBuilder sb = new StringBuilder();
                if (first) first = false;
                else sb.append(',');
                for (StringTokenizer st = new StringTokenizer(s, ", ", false); st.hasMoreTokens(); ) {
                    String field = st.nextToken();
                    sb.append(table).append('.').append(field);
                    if (st.hasMoreTokens())
                        sb.append(", ");
                }

                replaceLater(parent, position, new TextElement(sb.toString()));

                return true;
            }
        }.when(TEXT_NODE.and(depth(2, 2)).and(skipComments())));

        return true;
    }

    private void addFromField(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addField(field(PRIVATE, LIST_OF_STRING, "from"));

        topLevelClass.addMethod(method(
            PUBLIC, LIST_OF_STRING, "getFrom", __(
                "return from;"
        )));

        topLevelClass.addMethod(method(
            PUBLIC, topLevelClass.getType(), methodName, _(STRING, "join"), __(
                "if (join == null || (join = join.trim()).length() == 0) return this;",
                "if (from == null) from = new java.util.ArrayList<String>();",
                "from.add(join);",
                "return this;"
        )));

        topLevelClass.addMethod(method(
            PUBLIC, topLevelClass.getType(), methodName, _(STRING, "formatString"), _(OBJECT, "firstArg"), _(OBJECT, "secondAndFurtherArgs", true), __(
                "if (formatString == null || (formatString = formatString.trim()).length() == 0) return this;",
                "if (from == null) from = new java.util.ArrayList<String>();",
                "Object[] temp = new Object[(secondAndFurtherArgs == null ? 0 : secondAndFurtherArgs.length) + 1];",
                "if (secondAndFurtherArgs != null) System.arraycopy(secondAndFurtherArgs, 0, temp, 1, secondAndFurtherArgs.length);",
                "temp[0] = firstArg;",
                "String formatted = String.format(formatString, temp);",
                "from.add(formatted);",
                "return this;"
        )).javadoc(
                "@param formatString A {@link java.util.Formatter format string} for join expression",
                "@param firstArg First argument referenced by the format specifiers in the format string",
                "@param secondAndFurtherArgs Second and further arguments referenced by the format specifiers in the format string"
        ));
    }

    private void fixCriteriaColumns(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass generatedCriteria = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
            }
        }

        if (introspectedTable.getFullyQualifiedTable() != null && StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias()))
            return;
        if (generatedCriteria == null)
            return;

        final String table = MBGenerator.tableName(introspectedTable);
        final String ADD_CRITERION = "addCriterion(\"";

        for (Method m : generatedCriteria.getMethods()) {
            List<String> bodyLines = m.getBodyLines();
            for (ListIterator<String> it = bodyLines.listIterator(); it.hasNext(); ) {
                String line = it.next();
                if (line.trim().startsWith(ADD_CRITERION)) {
                    it.set(line.replace(ADD_CRITERION, ADD_CRITERION+table+"."));
                }
            }
        }
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        deleteStatement.sqlMapGenerated(getMapperXmlRoot(sqlMap), introspectedTable);
        return true;
    }

    private class DeleteStatement {
        public void deleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        }
        public void sqlMapGenerated(XmlElement root, IntrospectedTable introspectedTable) {
        }
        public void modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        }
    }

    private class MSSQLDeleteStatement extends DeleteStatement {
        @Override
        public void deleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
            if (introspectedTable.getFullyQualifiedTable() == null || !StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias()))
                return;
            final String alias = introspectedTable.getFullyQualifiedTable().getAlias();
            traverse(element, new ReplaceText(new TextReplacer("^(?i)(\\s*delete)(\\s*)(from)", "$1$2" + Matcher.quoteReplacement(alias) + "$2$3"), null));
        }
    }

    private class NoAliasDeleteStatement extends DeleteStatement {
        private final static String INCLUDE = "Delete_Example_Where_Clause";
        @Override
        public void deleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
            if (introspectedTable.getFullyQualifiedTable() == null || !StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias()))
                return;
            final String alias = introspectedTable.getFullyQualifiedTable().getAlias();
            traverse(element, new ReplaceText(
                    new TextReplacer("^(?i)(\\s*delete\\s*from\\s*.+?)\\s*"+Pattern.quote(alias), "$1"),
                    new TextReplacer("^Example_Where_Clause$", INCLUDE)
            ));
        }
        public void sqlMapGenerated(XmlElement root, IntrospectedTable introspectedTable) {
            if (introspectedTable.getFullyQualifiedTable() == null || !StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias()))
                return;
            if (exampleWhereClauseElement == null)
                return;

            XmlElement include = setAttribute(traverse(exampleWhereClauseElement, new CopyXml()), "id", INCLUDE);
            traverse(include, new ReplaceText(new TextReplacer("\\.condition\\b", "\\.unaliasedCondition")));
            root.addElement(include);
        }

        @Override
        public void modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
            InnerClass criterion = null;
            for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
                if ("Criterion".equals(innerClass.getType().getShortName())) {
                    criterion = innerClass;
                }
            }
            if (criterion != null) {
                String constName = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("aliasConstName")), aliasConstName)+"_";
                criterion.addMethod(method(PUBLIC, STRING, "getUnaliasedCondition", _(
                    "return condition != null && condition.startsWith(%s) ? condition.substring(%1$s.length()) : condition;", constName
                )));
            }
        }
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        exampleWhereClauseElement = null;
    }
}
