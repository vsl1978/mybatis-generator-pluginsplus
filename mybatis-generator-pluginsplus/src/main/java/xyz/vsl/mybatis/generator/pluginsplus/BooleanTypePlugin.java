package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.*;
import static xyz.vsl.mybatis.generator.pluginsplus.MBGenerator.FQJT.*;

/**
 * &lt;property name="boolean-varchar-columns" value="column_a (y/n), column_b"/&gt;
 * @author Vladimir Lokhov
 */
public class BooleanTypePlugin extends PluginAdapter {
    private String defaultTrueString;
    private String defaultFalseString;
    private int defaultTrueInt;
    private int defaultFalseInt;

    private List<GeneratedJavaFile> javaFiles = new ArrayList<GeneratedJavaFile>();
    private Set<String> handlers = new HashSet<String>();

    @Override
    public boolean validate(List<String> warnings) {
        defaultTrueString = Objects.nvl(Str.trim(properties.getProperty("varchar:true")), "Y");
        defaultFalseString = Objects.nvl(Str.trim(properties.getProperty("varchar:false")), "N");

        defaultTrueInt = Integer.parseInt(Objects.nvl(Str.trim(properties.getProperty("integer:true")), "1"));
        defaultFalseInt = Integer.parseInt(Objects.nvl(Str.trim(properties.getProperty("integer:false")), "0"));

        return !Objects.equals(defaultTrueString, defaultFalseString) && defaultTrueInt != defaultFalseInt;
    }

    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return modifyResultMap(element, introspectedTable);
    }

    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return modifyResultMap(element, introspectedTable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return modifyModelJavaTypes(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return modifyModelJavaTypes(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        changeColumnTypes(topLevelClass, introspectedTable);
        removeCriteriaMethods(topLevelClass, introspectedTable, "GreaterThan", "GreaterThanOrEqualTo", "LessThan", "LessThanOrEqualTo", "Like", "NotLike", "In", "NotIn", "Between", "NotBetween");
        replaceCriteriaParameters(topLevelClass, introspectedTable, "EqualTo", "NotEqualTo");
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addPlaceholderTypeHandler(element, introspectedTable);
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return javaFiles;
    }

    private final static Pattern PLACEHOLDER = Pattern.compile("(?<=\\#\\{)([^,\\}]++)");
    private boolean addPlaceholderTypeHandler(XmlElement element, IntrospectedTable introspectedTable) {
        final List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        if (columns.isEmpty())
            return true;
        traverse(element, new FindElements() {
            @Override
            public boolean process(XmlElement parent, Element self, int position) {
                String text = ((TextElement)self).getContent();
                if (!text.contains("#{")) return false;
                boolean modified = false;
                StringBuffer sb = new StringBuffer();
                Matcher matcher = PLACEHOLDER.matcher(text);
                while (matcher.find()) {
                    String property = matcher.group(1);
                    for (BooleanColumn<?> c : columns) {
                        int pos = property.lastIndexOf('.');
                        if (pos >= 0 && pos < property.length() - 1)
                            property = property.substring(pos + 1);
                        if (Objects.equals(property, c.getIntrospectedColumn().getJavaProperty())) {
                            matcher.appendReplacement(sb, property);
                            sb.append(",typeHandler=").append(c.getPackage()).append('.').append(c.getHandlerClassName());
                            modified = true;
                            break;
                        }
                    }
                }
                if (!modified)
                    return false;
                matcher.appendTail(sb);
                replaceLater(parent, position, new TextElement(sb.toString()));
                return true;
            }
        }.when(TEXT_NODE));
        return true;
    }

    private void changeColumnTypes(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        for (BooleanColumn<?> c : columns)
            c.getIntrospectedColumn().setFullyQualifiedJavaType(BOOLEAN);
    }

    private void removeCriteriaMethods(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String ... methods) {
        InnerClass generatedCriteria = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
            }
        }
        if (generatedCriteria == null)
            return;

        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        for (BooleanColumn<?> c : columns) {
            Set<String> set = new HashSet<String>();
            for (String m : methods)
                set.add("and"+camel(c.getIntrospectedColumn().getJavaProperty())+m);
            for (Iterator<Method> it = generatedCriteria.getMethods().iterator(); it.hasNext(); )
                if (set.contains(it.next().getName()))
                    it.remove();
        }
    }

    private void replaceCriteriaParameters(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String ... methods) {
        InnerClass generatedCriteria = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                generatedCriteria = innerClass;
            }
        }
        if (generatedCriteria == null)
            return;

        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        for (BooleanColumn<?> c : columns) {
            Set<String> set = new HashSet<String>();
            for (String m : methods)
                set.add("and"+camel(c.getIntrospectedColumn().getJavaProperty())+m);
            for (Method m : generatedCriteria.getMethods())
                if (set.contains(m.getName()))
                    m.getParameters().set(0, new Parameter(BOOLEAN, m.getParameters().get(0).getName()));
        }
    }

    private boolean modifyModelJavaTypes(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (topLevelClass.getFields() == null || topLevelClass.getFields().isEmpty())
            return true;
        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        if (columns.isEmpty())
            return true;
        for (final BooleanColumn<?> column : columns) {
            boolean found = false;
            for (Field f : topLevelClass.getFields()) {
                if (Objects.equals(f.getName(), column.getIntrospectedColumn().getJavaProperty())) {
                    f.setType(BOOLEAN);
                    found = true;
                }
            }
            if (!found) continue;
            for (Method m : topLevelClass.getMethods()) {
                String methodName = m.getName();
                String getter = "get"+camel(column.getIntrospectedColumn().getJavaProperty());
                String setter = "set"+camel(column.getIntrospectedColumn().getJavaProperty());
                //System.out.println("compare "+methodName+" with "+getter+" and "+setter);
                if (Objects.equals(methodName, getter)) {
                    m.setReturnType(BOOLEAN);
                }
                else if (Objects.equals(methodName, setter)) {
                    Parameter param = m.getParameters().get(0);
                    m.getParameters().set(0, new Parameter(BOOL, param.getName()));
                }
            }
        }
        return true;
    }

    private boolean modifyResultMap(XmlElement element, IntrospectedTable introspectedTable) {
        List<BooleanColumn<?>> columns = enumerateColumns(introspectedTable);
        if (columns.isEmpty()) return true;
        for (final BooleanColumn<?> column : columns) {
            if (!handlers.contains(column.getHandlerId())) {
                Context ctx = introspectedTable.getContext();

                StringMacro sm = new StringMacro();
                sm.text("${TRUE}", column.getTRUE().toString()).text("${FALSE}", column.getFALSE().toString());
                String targetProject = ctx.getJavaClientGeneratorConfiguration().getTargetProject();
                String fileEncoding = context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING);
                JavaFormatter javaFormatter = new DefaultJavaFormatter();
                if (!handlers.contains(column.getClass().getName())) {
                    handlers.add(column.getClass().getName());
                    CompilationUnit unit = new Template(column.getAbstractHandlerClassName()+".java", column.getPackage(), column.getAbstractHandlerClassName(), sm);
                    javaFiles.add(new GeneratedJavaFile(unit, targetProject, fileEncoding, javaFormatter));
                }
                handlers.add(column.getHandlerId());
                CompilationUnit unit = new Template(column.getHandlerTemplate(), column.getPackage(), column.getHandlerClassName(), sm);
                javaFiles.add(new GeneratedJavaFile(unit, targetProject, fileEncoding, javaFormatter));
            }

            traverse(element, new FindElements() {
                @Override
                public boolean process(XmlElement parent, Element self, int position) {
                    if (!column.getIntrospectedColumn().getJavaProperty().equals(getAttribute((XmlElement) self, "property")))
                        return false;
                    ((XmlElement) self).addAttribute(a("typeHandler", column.getPackage()+"."+column.getHandlerClassName()));
                    return true;
                }
            }.when(ELEMENT.and(ancestorsAndSelf("result"))));
        }
        return true;
    }

    private final static Pattern COLUMN = Pattern.compile("^\\s*+(.+?)\\s*+(?:\\(\\s*+(.+?)\\s*+/\\s*+(.+?)\\s*+\\))?\\s*$");

    private List<BooleanColumn<?>> enumerateColumns(IntrospectedTable introspectedTable) {
        List<BooleanColumn<?>> list = new ArrayList<BooleanColumn<?>>();
        String columns = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("boolean-integer-columns")), "");
        for (StringTokenizer st = new StringTokenizer(columns, ",;", false); st.hasMoreTokens(); ) {
            String columnDefinition = st.nextToken();
            IntegerBooleanColumn c = new IntegerBooleanColumn(introspectedTable, columnDefinition, String.valueOf(defaultTrueInt), String.valueOf(defaultFalseInt));
            if (c.isValid())
                list.add(c);
        }
        columns = Objects.nvl(Str.trim(introspectedTable.getTableConfigurationProperty("boolean-varchar-columns")), "");
        for (StringTokenizer st = new StringTokenizer(columns, ",;", false); st.hasMoreTokens(); ) {
            String columnDefinition = st.nextToken();
            VarcharBooleanColumn c = new VarcharBooleanColumn(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
            if (c.isValid())
                list.add(c);
        }
        return list;
    }

    private abstract static class BooleanColumn<T> {
        private T TRUE;
        private T FALSE;
        private String pakkage;
        private String handlerClassName;
        private String handlerId;
        private String column;
        private IntrospectedColumn introspectedColumn;

        boolean valid;

        BooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            Matcher matcher = COLUMN.matcher(columnDefinition);
            if (!matcher.matches()) {
                System.err.println("Invalid column definition: '"+columnDefinition+"'");
                return;
            }
            column = matcher.group(1);
            TRUE = cast(Objects.nvl(matcher.group(2), defaultTrueString));
            FALSE = cast(Objects.nvl(matcher.group(3), defaultFalseString));

            handlerId = TRUE + "/" + FALSE;
            pakkage = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType()).getPackageName();
            handlerClassName = generateClassName();

            for (IntrospectedColumn c : introspectedTable.getAllColumns()) {
                if (column.equalsIgnoreCase(c.getActualColumnName())) {
                    introspectedColumn = c;
                    break;
                }
            }
            if (introspectedColumn == null) {
                System.err.println("Column '"+column+"' not found in table "+introspectedTable.getFullyQualifiedTableNameAtRuntime());
                return;
            }

            valid = true;
        }

        public T getTRUE() {
            return TRUE;
        }

        public T getFALSE() {
            return FALSE;
        }

        public String getPackage() {
            return pakkage;
        }

        public String getHandlerClassName() {
            return handlerClassName;
        }

        public String getHandlerId() {
            return handlerId;
        }

        public String getColumn() {
            return column;
        }

        public boolean isValid() {
            return valid;
        }

        public IntrospectedColumn getIntrospectedColumn() {
            return introspectedColumn;
        }

        public abstract T cast(String s);

        public abstract String generateClassName();

        public abstract String getHandlerTemplate();

        public abstract String getAbstractHandlerClassName();
    }

    private static class VarcharBooleanColumn extends BooleanColumn<String> {
        VarcharBooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            super(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
        }

        @Override
        public String cast(String s) {
            return s;
        }

        @Override
        public String generateClassName() {
            return "VarcharBoolean_"+toCodes(getTRUE())+"_"+toCodes(getFALSE());
        }

        @Override
        public String getHandlerTemplate() {
            return "CustomVarcharBooleanTypeHandler.java";
        }

        @Override
        public String getAbstractHandlerClassName() {
            return "AbstractVarcharBooleanTypeHandler";
        }
    }

    private static class IntegerBooleanColumn extends BooleanColumn<Integer> {
        IntegerBooleanColumn(IntrospectedTable introspectedTable, String columnDefinition, String defaultTrueString, String defaultFalseString) {
            super(introspectedTable, columnDefinition, defaultTrueString, defaultFalseString);
        }

        @Override
        public Integer cast(String s) {
            return Integer.valueOf(s);
        }

        private String toString(int v) {
            return v < 0 ? "m"+(-v) : String.valueOf(v);
        }

        @Override
        public String generateClassName() {
            return "IntegerBoolean_"+toString(getTRUE()) +"_"+toString(getFALSE());
        }

        @Override
        public String getHandlerTemplate() {
            return "CustomIntegerBooleanTypeHandler.java";
        }

        @Override
        public String getAbstractHandlerClassName() {
            return "AbstractIntegerBooleanTypeHandler";
        }
    }

    private static String toCodes(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            String h = Integer.toHexString((int) s.charAt(i));
            if (h.length() < 4) sb.append("0000".substring(h.length()));
            sb.append(h);
        }
        return sb.toString();
    }

    private static class Template implements CompilationUnit {
        private String content;
        private FullyQualifiedJavaType type;

        public Template(String resource, String pkg, String className, StringMacro macro) {
            String uri = "/"+getClass().getPackage().getName().replace('.', '/')+"/"+resource;
            InputStream is = getClass().getResourceAsStream(uri);
            if (is != null) {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[32768];
                    for (int count; (count = is.read(buf)) >= 0; ) os.write(buf, 0, count);
                    is.close();
                    macro.text("${PACKAGE}", pkg).text("${CLASS_NAME}", className);
                    this.content = macro.replaceAll(new String(os.toByteArray(), "UTF-8"));
                } catch (IOException e) {
                    System.err.println("Failed to load "+uri);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Unable to load "+uri+" - resource not found");
            }
            type = new FullyQualifiedJavaType(pkg+"."+className);
        }

        @Override
        public String getFormattedContent() {
            return content;
        }

        @Override
        public Set<FullyQualifiedJavaType> getImportedTypes() {
            return null;
        }

        @Override
        public Set<String> getStaticImports() {
            return null;
        }

        @Override
        public FullyQualifiedJavaType getSuperClass() {
            return null;
        }

        @Override
        public boolean isJavaInterface() {
            return false;
        }

        @Override
        public boolean isJavaEnumeration() {
            return false;
        }

        @Override
        public Set<FullyQualifiedJavaType> getSuperInterfaceTypes() {
            return null;
        }

        @Override
        public FullyQualifiedJavaType getType() {
            return type;
        }

        @Override
        public void addImportedType(FullyQualifiedJavaType fullyQualifiedJavaType) {

        }

        @Override
        public void addImportedTypes(Set<FullyQualifiedJavaType> fullyQualifiedJavaTypes) {

        }

        @Override
        public void addStaticImport(String s) {

        }

        @Override
        public void addStaticImports(Set<String> strings) {

        }

        @Override
        public void addFileCommentLine(String s) {

        }

        @Override
        public List<String> getFileCommentLines() {
            return null;
        }
    }
}
