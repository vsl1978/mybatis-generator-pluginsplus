package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Provides the ability to remap awkward SQL Type to more convenient one (e.g. DECIMAL and java.math.BigDecimal to BIGINT and Long).</p>
 * <p>Plugin's properties:<ul>
 *     <li><i>(optional)</i> tables &mdash; regular expression that specifies tables to process</li>
 *     <li><i>(optional)</i> excludeTables &mdash; regular expression that excludes tables</li>
 *     <li><tt>name="<b>from:TYPE</b>" value="<b>RESULT</b>"</tt> &mdash; specifies SQL type to process.</li>
 * </ul>
 * where
 * <table border="1" cellpadding="0" cellspacing="0">
 *     <tbody>
 *         <tr>
 *             <td valign="top"><b>TYPE</b></td>
 *             <td>
 *                 <p>
 *                     <i>JDBCTypeName</i><br/>
 *                     One of the {@link java.sql.Types java.sql.Types} constants
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName(minLength...maxLength)</i><br/>
 *                     One of the {@link java.sql.Types java.sql.Types} constants with field length
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName(minLength...maxLength, minScale...maxScale)</i><br/>
 *                     One of the {@link java.sql.Types java.sql.Types} constants with field length and scale
 *                 </p>
 *                 <p>
 *                     <i>/regexp/</i><br/>
 *                     Regular expression to match native SQL type name
 *                 </p>
 *             </td>
 *         </tr>
 *         <tr>
 *             <td valign="top"><b>RESULT</b></td>
 *             <td>
 *                 <p>
 *                     <i>JDBCTypeName</i><br/>
 *                     Replace source {@code JDBCTypeName} with specified {@code JDBCTypeName} and source {@code length} and {@code scale}
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName(length)</i><br/>
 *                     Replace source {@code JDBCTypeName} and {@code length} with specified {@code JDBCTypeName} and {@code length} and source {@code scale}
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName(length,scale)</i><br/>
 *                     Replace source {@code JDBCTypeName}, {@code length} and {@code scale} with specified {@code JDBCTypeName}, {@code length} and {@code scale}
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName[(length[,scale])], fully qualified Java type</i><br/>
 *                     Replace source {@code JDBCTypeName} (and possibly {@code length}, {@code scale}) with specified {@code JDBCTypeName} ({@code length} and {@code scale}) and use specified Java type
 *                 </p>
 *                 <p>
 *                     <i>JDBCTypeName[(length[,scale])], javaType=fully qualified Java class name, typeHandler=fully qualified TypeHandler's Java class name</i><br/>
 *                     Replace source {@code JDBCTypeName} (and possibly {@code length}, {@code scale}) with specified {@code JDBCTypeName} ({@code length} and {@code scale}), use specified Java type and TypeHandler
 *                 </p>
 *             </td>
 *         </tr>
 *     </tbody>
 * </table>
 * </p>
 * <p></p>
 * <p>Example:
 * <pre>
 * &lt;plugin type="xyz.vsl.mybatis.generator.pluginsplus.JDBCTypesPlugin">
 *     &lt;property name="from:CLOB" value="VARCHAR"/&gt;
 *     &lt;property name="from:DECIMAL(10..20,0)" value="BIGINT, java.lang.Long"/&gt;
 *     &lt;property name="from:/(?i)TIMESTAMP\b.&#42;/" value="TIMESTAMP, java.util.Date"/&gt;
 *     &lt;property name="from:/(?i)bar_type/" value="OTHER, javaType=foo.sql.udt.Bar, typeHandler=foo.sql.mybatis.BarTypeHandler"/&gt;
 * &lt;/plugin&gt;
 * </pre>
 * </p>
 * @author Vladimir Lokhov
 */
public class JDBCTypesPlugin extends IntrospectorPlugin {
    private boolean verbose;
    private boolean introspect;
    private List<Pair<Type, Type>> convert = new ArrayList<Pair<Type, Type>>();
    private Map<String, Map<String, ColumnInfo>> columnInfo = new HashMap<String, Map<String, ColumnInfo>>();

    private Pattern include;
    private Pattern exclude;

    private static class ColumnInfo {
        String nativeTypeName;
    }

    private static class IntegerInterval {
        int min;
        int max;
        public boolean contains(int value) {
            return value >= min && value <= max;
        }

        private static Pattern INTERVAL = Pattern.compile("^(\\d++)?\\s*(\\.\\.)?\\s*(\\d+)?$");

        public static IntegerInterval parse(String s) {
            if (Str.trim(s) == null)
                return null;
            Matcher matcher = INTERVAL.matcher(s);
            if (!matcher.matches())
                throw new IllegalArgumentException("Invalid interval definition: '"+s+"'");
            IntegerInterval i = new IntegerInterval();
            if (matcher.group(2) != null) {
                i.min = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : Integer.MIN_VALUE;
                i.max = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : Integer.MAX_VALUE;
            } else {
                i.min = i.max = Integer.parseInt(matcher.group(1));
            }
            return i;
        }

        @Override
        public String toString() {
            return min != max ? (min == Integer.MIN_VALUE ? "" : String.valueOf(min)) + ".." + (max == Integer.MAX_VALUE ? "" : String.valueOf(max)) : String.valueOf(min);
        }
    }

    private static class Type {
        String name;
        String regexp;
        IntegerInterval length;
        IntegerInterval scale;
        String javaType;
        String typeHandler;

        private static Pattern SIMPLE = Pattern.compile("^[^\\(]+$");
        private static Pattern WITH_LEN = Pattern.compile("^([^(]+?)\\s*\\(([^,]+)(?:,([^,)]+))?\\)$");
        public static Type parse(String s) {
            s = Str.trim(s);
            if (s == null) return null;
            Type type = new Type();
            if (s.charAt(0) == '/') {
                type.regexp = s.substring(1, s.length() - 1).trim();
                return type;
            }
            List<String> options = new ArrayList<String>();
            while (true) {
                int comma = s.lastIndexOf(',');
                if (comma > 0) {
                    if (s.indexOf(')', comma) < 0) {
                        options.add(Str.trim(s.substring(comma + 1)));
                        s = s.substring(0, comma).trim();
                    } else break;
                } else break;
            }

            for (String option : options) {
                if (option == null) continue;
                int eq = option.indexOf('=');
                if (eq < 0) {
                    if (options.size() == 1)
                        type.javaType = option;
                } else {
                    String optionName = Str.trim(option.substring(0, eq));
                    String optionValue = Str.trim(option.substring(eq + 1));
                    if ("javaType".equals(optionName))
                        type.javaType = optionValue;
                    else if ("typeHandler".equals(optionName))
                        type.typeHandler = optionValue;
                }
            }

            if (SIMPLE.matcher(s).matches()) {
                type.name = s;
            } else {
                Matcher matcher = WITH_LEN.matcher(s);
                if (!matcher.matches())
                    throw new IllegalArgumentException("Invalid type definition: '"+s+"'");
                type.name = matcher.group(1);
                type.length = IntegerInterval.parse(matcher.group(2));
                if (matcher.groupCount() > 2)
                    type.scale = IntegerInterval.parse(matcher.group(3));
            }
            return type;
        }

        public boolean equals(Object o) {
            if (o instanceof IntrospectedColumn) {
                IntrospectedColumn c = (IntrospectedColumn)o;
                if (regexp != null) {
                    String nativeTypeName = c.getProperties().getProperty(NATIVE_TYPE);
                    if (nativeTypeName != null && nativeTypeName.matches(regexp))
                        return true;
                }
                if (!Objects.equals(name, c.getJdbcTypeName()))
                    return false;
                if (length != null && !length.contains(c.getLength()))
                    return false;
                if (scale != null && !scale.contains(c.getScale()))
                    return false;
                return true;
            }
            else return super.equals(o);
        }

        @Override
        public String toString() {
            return regexp == null ?  name + (length != null ? "("+length + (scale != null ? ", "+scale : "") + ")" : "") : ("/"+regexp+"/");
        }
    }


    @Override
    public boolean validate(List<String> warnings) {
        verbose = Bool.bool(Str.trim(properties.getProperty("verbose")), false);
        include = Pattern.compile(Objects.nvl(Str.trim(properties.getProperty("tables")), ".+"));
        exclude = Pattern.compile(Objects.nvl(Str.trim(properties.getProperty("excludeTables")), "^-$"));

        introspectNativeSQLTypes();
        for (Enumeration<?> names = properties.propertyNames(); names.hasMoreElements(); ) {
            String name = (String)names.nextElement();
            String value = properties.getProperty(name);
            name = Str.trim(name);
            if (name == null || !name.startsWith("from:")) continue;
            String from = name.substring("from:".length()).trim();
            try {
                convert.add(Pair.of(Type.parse(from), Type.parse(value)));
            } catch (IllegalArgumentException e) {
                warnings.add(e.getMessage());
                return false;
            }
        }
        introspect = Bool.bool(Str.trim(properties.getProperty("introspectColumns")), convert.isEmpty());
        return true;
    }

    private void introspectNativeSQLTypes() {
        Connection conn = null;
        try {
            conn = getConnection();
            DatabaseMetaData dbmd = conn.getMetaData();
            for (TableConfiguration tc : context.getTableConfigurations()) {
                String tableName = tc.getTableName();
                Map<String, ColumnInfo> map = new HashMap<String, ColumnInfo>();
                columnInfo.put(tableName, map);

                TableName tn = new TableName(tc, dbmd);

                ResultSet rset = dbmd.getColumns(tn.catalog, tn.schema, tn.table, null);

                while (rset.next()) {
                    ColumnInfo info = new ColumnInfo();
                    map.put(rset.getString("COLUMN_NAME"), info);
                    info.nativeTypeName = rset.getString("TYPE_NAME");
                }

                rset.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn);
        }
    }

    private final static String NATIVE_TYPE = "nativeSQLTypeName";
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        if (!include.matcher(tableName).matches() || exclude.matcher(tableName).matches())
            return;

        Map<String, ColumnInfo> map = columnInfo.get(tableName);
        for (IntrospectedColumn c : introspectedTable.getAllColumns()) {
            ColumnInfo info = map.get(c.getActualColumnName());
            if (info != null)
                c.getProperties().setProperty(NATIVE_TYPE, info.nativeTypeName);

            if (introspect)
                System.out.println(String.format("Table %s, column %s - %s {%s} (%d, %d)",
                        introspectedTable.getFullyQualifiedTableNameAtRuntime(), c.getActualColumnName(),
                        c.getJdbcTypeName(), c.getProperties().getProperty(NATIVE_TYPE), c.getLength(), c.getScale()
                ));

            int patternNumber = 0;
            for (Pair<Type, Type> p : convert) {
                patternNumber++;
                if (p.a().equals(c)) {
                    String targetTypeName = p.b().name;
                    Integer targetType = typeToId.get(targetTypeName);
                    if (targetType == null) {
                        System.err.println("Unknown JDBC Type Name: '"+targetTypeName+"'");
                    } else {
                        boolean wasBLOB = c.isBLOBColumn();

                        int length = p.b().length != null ? p.b().length.max : c.getLength();
                        int scale = p.b().scale != null ? p.b().scale.max : c.getScale();
                        String targetJavaType = p.b().javaType != null ? p.b().javaType : c.getFullyQualifiedJavaType().getFullyQualifiedName();
                        if (verbose)
                            System.out.println(
                                String.format("Table %s, column %s - transform from %s (%d, %d) - %s to %s (%d, %d) - %s. Pattern %s",
                                    introspectedTable.getFullyQualifiedTableNameAtRuntime(), c.getActualColumnName(),
                                    c.getJdbcTypeName(), c.getLength(), c.getScale(), c.getFullyQualifiedJavaType().getFullyQualifiedName(),
                                    targetTypeName, length, scale, targetJavaType,
                                    p.a()
                            ));
                        c.setJdbcType(targetType);
                        c.setJdbcTypeName(targetTypeName);
                        c.setLength(length);
                        c.setScale(scale);
                        c.setFullyQualifiedJavaType(new FullyQualifiedJavaType(targetJavaType));
                        if (p.b().typeHandler != null)
                            c.setTypeHandler(p.b().typeHandler);

                        if (wasBLOB && !c.isBLOBColumn()) {
                            for (Iterator<IntrospectedColumn> it = introspectedTable.getBLOBColumns().iterator(); it.hasNext(); )
                                if (it.next() == c) {
                                    it.remove();
                                    introspectedTable.getBaseColumns().add(c);
                                    break;
                                }
                        }
                        else if (!wasBLOB && c.isBLOBColumn()) {
                            for (Iterator<IntrospectedColumn> it = introspectedTable.getBaseColumns().iterator(); it.hasNext(); )
                                if (it.next() == c) {
                                    it.remove();
                                    introspectedTable.getBLOBColumns().add(c);
                                    break;
                                }
                        }

                    }
                    break;
                }
            }
        }
    }

    private static Map<String, Integer> typeToId = new HashMap<String, Integer>();
    private static Map<Integer, String> idToType  = new HashMap<Integer, String>();

    static {
        Field[] constants = java.sql.Types.class.getFields();
        for (Field c : constants) {
            if (!Modifier.isStatic(c.getModifiers())) continue;
            if (c.getType() != int.class && c.getType() != Integer.class) continue;
            try {
                Integer value = (Integer)c.get(null);
                typeToId.put(c.getName(), value);
                idToType.put(value, c.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
