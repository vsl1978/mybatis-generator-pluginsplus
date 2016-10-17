package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.api.dom.java.JavaVisibility.PRIVATE;
import static org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>This plugin adds limit and offset (top, first...skip, offset...row...fetch next, etc) clause to the Example class.</p>
 * <p>Configuration parameters:</p>
 * <ul>
 *     <li><b>(required)</b> {@code dialect} &mdash; name of the target sql dialect</li>
 * </ul>
 * <p>Supported SQL dialects:</p>
 * <ul>
 *     <li>Apache Derby</li>
 *     <li>Firebird 1.x</li>
 *     <li>Firebird 2.x</li>
 *     <li>H2</li>
 *     <li>HSQL</li>
 *     <li>SQL Server 2005</li>
 *     <li>SQL Server 2011</li>
 *     <li>MySql</li>
 *     <li>Oracle</li>
 *     <li>Oracle 12</li>
 *     <li>PostgreSQL</li>
 * </ul>
 * <p>&nbsp;</p>
 * <p>Supported Java Client generators:<br>
 * <b>ANNOTATEDMAPPER</b>: not supported<br>
 * <b>MIXEDMAPPER</b>: supported<br>
 * <b>XMLMAPPER</b>: supported<br>
 * </p>
 * @author Vladimir Lokhov
 */
public class PaginationPlugin extends IntrospectorPlugin {
    private abstract class DBDialect {
        public abstract boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields);

        public boolean appendNewElementsToXml(XmlElement root, IntrospectedTable introspectedTable) {
            return true;
        }

        protected void copyColumnsList(XmlElement root, IntrospectedTable introspectedTable) {
            String alias = introspectedTable.getFullyQualifiedTable().getAlias();
            if (!StringUtility.stringHasValue(alias))
                return;
            if (introspectedTable.hasBaseColumns()) {
                StringBuilder sb = new StringBuilder();
                for (IntrospectedColumn c : introspectedTable.getNonBLOBColumns()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(c));
                }
                root.addElement(e("sql", a("id", "Base_Column_Label_List"), sb.toString()));
            }
            if (introspectedTable.hasBLOBColumns()) {
                StringBuilder sb = new StringBuilder();
                for (IntrospectedColumn c : introspectedTable.getBLOBColumns()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(c));
                }
                root.addElement(e("sql", a("id", "Blob_Column_Label_List"), sb.toString()));
            }
        }


        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private DBDialect dialect;
    private boolean optimize;
    private boolean chain;

    public boolean validate(List<String> warnings) {
        optimize = Bool.bool(properties.getProperty("optimize"), true);
        chain = Bool.bool(properties.getProperty("methodsChain"), true);

        String dialect = properties.getProperty("dialect");
        if (dialect != null) dialect = dialect.trim().toLowerCase(); else dialect = "";
        if ("postgresql".equals(dialect))
            this.dialect = new PostgreSQL();
        if ("hsql".equals(dialect) || "h2".equals(dialect))
            this.dialect = new HSQL();
        else if (dialect.matches("oracle($|\\s*[89].*$|\\s*1[01].*$)"))
            this.dialect = new Oracle();
        else if (dialect.matches("oracle\\s*(1[2-9]|[2-9][0-1]).*$"))
            this.dialect = new Oracle12();
        else if (dialect.matches("(ms\\s*sql(\\s*server)?|(ms\\s*)?sql\\s*server)\\s*201[1-9]"))
            this.dialect = new MSSQL2012();
        else if (dialect.matches("(ms\\s*sql(\\s*server)?|(ms\\s*)?sql\\s*server)\\s*(2005|2008(\\s*r2)?)?"))
            this.dialect = new MSSQL2005();
        else if (dialect.matches("fire\\s*bird\\s*1.*"))
            this.dialect = new Firebird1x();
        else if (dialect.matches("fire\\s*bird\\s*[2-9].*"))
            this.dialect = new Firebird2x();
        else if (dialect.matches("my\\s*sql"))
            this.dialect = new MySql();
        else if (dialect.matches("(apache\\s*)?derby.*"))
            this.dialect = new Derby();

        if (this.dialect == null) {
            this.dialect = new PostgreSQL();
            warnings.add("Use default sql dialect: "+this.dialect);
        }
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addField(field(PRIVATE, INTEGER, "limit"));
        topLevelClass.addField(field(PRIVATE, INTEGER, "offset"));
        topLevelClass.addMethod(method(PUBLIC, INTEGER, "getLimit", body("return limit;")));
        topLevelClass.addMethod(method(PUBLIC, INTEGER, "getOffset", body("return offset;")));
        topLevelClass.addMethod(method(
            PUBLIC, chain ? topLevelClass.getType() : VOID, "setLimitAndOffset", param(INTEGER, "limit"), param(INTEGER, "offset"), body(
                "if (limit != null && limit > 0) {",
                    "this.limit = limit;",
                    "this.offset = (offset != null && offset > 0) ? offset : null;",
                "} else {",
                    "this.limit = this.offset = null;",
                "}",
                chain ? "return this;" : null
        )));

        topLevelClass.addMethod(method(PUBLIC, chain ? topLevelClass.getType() : VOID, "setLimit", param(INTEGER, "limit"), body((chain ? "return ": "") + "setLimitAndOffset(limit, null);")));

        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!this.dialect.modifyXml(element, introspectedTable, true, false))
            return false;
        element.addElement(0, new TextElement("-->"));
        element.addElement(0, new TextElement("sql dialect: "+dialect));
        element.addElement(0, new TextElement("<!--"));
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!this.dialect.modifyXml(element, introspectedTable, true, true))
            return false;
        element.addElement(0, new TextElement("-->"));
        element.addElement(0, new TextElement("sql dialect: "+dialect));
        element.addElement(0, new TextElement("<!--"));
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        return dialect.appendNewElementsToXml(getMapperXmlRoot(sqlMap), introspectedTable);
    }

    private class PostgreSQL extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    "limit ${limit}",
                    e("if", a("test", "offset != null"), "offset ${offset}")
                )
            );
            return true;
        }
    };


    private class Oracle extends DBDialect {
        @Override
        public boolean appendNewElementsToXml(XmlElement root, IntrospectedTable introspectedTable) {
            copyColumnsList(root, introspectedTable);
            return true;
        }

        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            String alias = introspectedTable.getFullyQualifiedTable().getAlias();
            String table = MBGenerator.tableName(introspectedTable);
            boolean hasAlias = StringUtility.stringHasValue(alias);
            if (!hasAlias)
                alias = table;

            StringMacro m = new StringMacro();
            m.put("_T1_", "\"-ofslim-sub-\"");
            m.put("_T2_", alias);
            m.put("_ROW_", "\"-ofslim-rnum-\"");
            m.put(">", "&gt;");
            m.put("<", "&lt;");

            element.addElement(0, e("if", a("test", "orderByClause != null and limit != null"),
                "select ",
                baseFields ? e("include", a("refid", hasAlias ? "Base_Column_Label_List" : "Base_Column_List")) : null,
                baseFields && blobFields ? "," : null,
                blobFields ? e("include", a("refid", hasAlias ? "Blob_Column_Label_List" : "Blob_Column_List")) : null,
                optimize ?
                    e("choose",
                        e("when", a("test", "offset != null"), m.replaceAll("from (select _T1_.*, rownum as _ROW_ from (")),
                        e("otherwise", m.replaceAll(", rownum as _ROW_ from ("))
                    )
                    :
                    m.replaceAll("from (select _T1_.*, rownum as _ROW_ from (")
            ));
            element.addElement(e("if", a("test", "orderByClause != null and limit != null"),
                optimize ?
                    e("choose",
                        e("when", a("test", "offset != null"),
                            m.replaceAll(") _T1_ where rownum <= (${offset}+${limit}) ) _T2_ where _ROW_ > ${offset}")
                        ),
                        e("otherwise", m.replaceAll(") _T2_ where rownum <= ${limit}"))
                    )
                    :
                    e("choose",
                        e("when", a("test", "offset != null"),
                                m.replaceAll(") _T1_ where rownum <= (${offset}+${limit}) ) _T2_ where _ROW_ > ${offset}")
                        ),
                        e("otherwise", m.replaceAll(") _T1_) _T2_ where _ROW_ <= ${limit}"))
                    )
                ,
                m.replaceAll("order by _ROW_")
            ));
            return true;
        }
    };

    private class Oracle12 extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    e("if", a("test", "offset != null"), "OFFSET ${offset} ROWS"),
                    "FETCH NEXT ${limit} ROWS ONLY"
                )
            );
            return true;
        }
    };

    private class Derby extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    e("if", a("test", "offset != null"), "OFFSET ${offset} ROWS"),
                    "FETCH NEXT ${limit} ROWS ONLY"
                )
            );
            return true;
        }
    };

    private class MSSQL2005 extends DBDialect {
        @Override
        public boolean appendNewElementsToXml(XmlElement root, IntrospectedTable introspectedTable) {
            copyColumnsList(root, introspectedTable);
            return true;
        }

        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            String alias = introspectedTable.getFullyQualifiedTable().getAlias();
            String table = MBGenerator.tableName(introspectedTable);
            boolean hasAlias = StringUtility.stringHasValue(alias);
            if (!hasAlias)
                alias = table;

            StringMacro m = new StringMacro();
            m.put("_T_", alias);
            m.put("_CTE_", "[-ofslim-cte-]");
            m.put("_ROW_", "[-ofslim-cte-n-]");
            m.put(">", "&gt;");
            m.put("<", "&lt;");

            List<Element> children = element.getElements();
            int idx = 0;
            int firstInclude = -1, from = -1;
            for (Element e : children) {
                if (e instanceof XmlElement) {
                    if ("include".equals(((XmlElement) e).getName())) {
                        if (firstInclude < 0) firstInclude = idx;
                    }
                }
                else if (e instanceof TextElement) {
                    String t = e.getFormattedContent(0).trim();
                    if (firstInclude >= 0 && t.startsWith("from")) {
                        if (from < 0) from = idx;
                    }
                }
                idx++;

            }

            if (firstInclude < 0 || from < 0) {
                return false;
            }

            /*
             * SELECT
             * <include ref="columns_list">
             * <-- add row number
             * FROM table
             */
            element.addElement(from,
                e("if", a("test", "orderByClause != null and limit != null"+(optimize ? " and offset != null" : "")),
                    m.replaceAll(", ROW_NUMBER() OVER (ORDER BY ${orderByClause}) AS _ROW_")
            ));

            /*
             * SELECT
             * <-- add "TOP n"
             * <include ref="columns_list">
             * , row number
             * FROM table
             */
            if (optimize)
                element.addElement(firstInclude,
                    e("if", a("test", "orderByClause != null and limit != null"),
                        "TOP",
                        e("choose",
                            e("when", a("test", "offset != null"), "(${offset}+${limit})"),
                            e("otherwise", "${limit}")
                        )
                    )
                );

            /*
             * <-- "WITH alias AS ("
             * SELECT
             * TOP n
             * <include ref="columns_list">
             * , row number
             * FROM table
             */
            element.addElement(0,
                e("if", a("test", "orderByClause != null and limit != null"+(optimize ? " and offset != null" : "")),
                    m.replaceAll("with _CTE_ as (")
                )
            );

            /*
             * WITH alias AS (
             * SELECT
             * TOP n
             * <include ref="columns_list">
             * , row number
             * FROM table
             * ....
             * WHERE
             * ....
             * ORDER BY
             * ....
             * <-- ") SELECT table.* FROM alias table WHERE row number ... ORDER BY table.row number
             */
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"+(optimize ? " and offset != null" : "")),
                    ") select ",
                    baseFields ? e("include", a("refid", hasAlias ? "Base_Column_Label_List" : "Base_Column_List")) : null,
                    baseFields && blobFields ? "," : null,
                    blobFields ? e("include", a("refid", hasAlias ? "Blob_Column_Label_List" : "Blob_Column_List")) : null,
                    m.replaceAll("from _CTE_ _T_ where "),
                    optimize ?
                        m.replaceAll("_ROW_ > ${offset}") :
                        e("choose",
                            e("when", a("test", "offset != null"), m.replaceAll("_ROW_ between ${offset}+1 and ${offset}+${limit}")),
                            e("otherwise", m.replaceAll("_ROW_ <= ${limit}"))
                        ), //
                        m.replaceAll("order by _ROW_")
                )
            );
            return true;
        }
    };

    private class MSSQL2012 extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    e("choose",
                        e("when", a("test", "offset != null"), "OFFSET ${offset} ROWS"),
                        e("otherwise", "OFFSET 0 ROWS")
                    ),
                    "FETCH NEXT ${limit} ROWS ONLY"
                )
            );
            return true;
        }
    };

    private class HSQL extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    "limit ${limit}",
                    e("if", a("test", "offset != null"), "offset ${offset}")
                )
            );
            return true;
        }
    };

    private class Firebird1x extends DBDialect {
        private Pattern SELECT = Pattern.compile("^(?i)\\s*select\\s*.*$");
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            traverse(element, new FindElements() {
                private boolean first = true;

                @Override
                public boolean process(XmlElement parent, Element self, int position) {
                    if (!(self instanceof TextElement)) return false;
                    if (!first) return false;
                    String s = ((TextElement) self).getFormattedContent(0).trim();
                    if (SELECT.matcher(s).matches()) {
                        first = false;
                        addLater(parent, position + 1,
                            e("if", a("test", "orderByClause != null and limit != null"),
                                "FIRST ${limit}",
                                e("if", a("test", "offset != null"), "SKIP ${offset}")
                            )
                        );
                        return true;
                    }
                    return false;
                }
            }.when(TEXT_NODE.and(depth(2, 2)).and(skipComments()) ));
            return true;
        }
    };

    private class Firebird2x extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    "ROWS",
                    e("choose", e("when", a("test", "offset != null"), "${offset}"), e("otherwise", "1")),
                    "TO ${limit}",
                    e("if", a("test", "offset != null"), "+${offset}")
                )
            );
            return true;
        }
    };

    private class MySql extends DBDialect {
        @Override
        public boolean modifyXml(XmlElement element, IntrospectedTable introspectedTable, boolean baseFields, boolean blobFields) {
            element.addElement(
                e("if", a("test", "orderByClause != null and limit != null"),
                    "LIMIT",
                    e("if", a("test", "offset != null"), "${offset},"),
                    "${limit}"
                )
            );
            return true;
        }
    };

}
