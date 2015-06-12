package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.rules.Rules;
import xyz.vsl.mybatis.generator.pluginsplus.el.Context;
import xyz.vsl.mybatis.generator.pluginsplus.el.ELFactory;
import xyz.vsl.mybatis.generator.pluginsplus.el.Evaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir Lokhov
 */
public class SuperclassPlugin extends IntrospectorPlugin {
    private ClassNameTemplate modelClassExtends;
    private Evaluator modelClassExtendsFilter;
    private ClassNameTemplate exampleClassExtends;
    private Evaluator exampleClassExtendsFilter;
    private List<ClassNameTemplate> modelClassImplements;
    private Evaluator modelClassImplementsFilter;
    private List<ClassNameTemplate> exampleClassImplements;
    private Evaluator exampleClassImplementsFilter;
    private List<ClassNameTemplate> mapperInterfaceExtends;
    private Evaluator mapperInterfaceExtendsFilter;

    @Override
    public boolean validate(List<String> list) {
        modelClassExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)model(-?class)?-?extends?"));
        exampleClassExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)(example|criteria)(-?class)?-?extends?"));
        modelClassImplements = ClassNameTemplate.parse(getPropertyByRegexp("(?i)model(-?class)?-?implements?"), ";");
        exampleClassImplements = ClassNameTemplate.parse(getPropertyByRegexp("(?i)(example|criteria)(-?class)?-?implements?"), ";");
        mapperInterfaceExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)mapper(-?class|-?interface)?(-?implements?|-?extends?)"), ";");
        
        modelClassExtendsFilter = evaluator("(?i)model(-?class)?-?extends?-?(if|when|filter)", modelClassExtends != null || !modelClassImplements.isEmpty());
        exampleClassExtendsFilter = evaluator("(?i)(example|criteria)(-?class)?-?extends?-?(if|when|filter)", exampleClassExtends != null || !exampleClassImplements.isEmpty());
        modelClassImplementsFilter = Objects.nvl(evaluator("(?i)model(-?class)?-?implements?-?(if|when|filter)", false), modelClassExtendsFilter);
        exampleClassImplementsFilter = Objects.nvl(evaluator("(?i)(example|criteria)(-?class)?-?implements?-?(if|when|filter)", false), exampleClassExtendsFilter);
        
        mapperInterfaceExtendsFilter = evaluator("(?i)mapper(-?class|-?interface)?(-?implements?|-?extends?)-?(if|when|filter)", !mapperInterfaceExtends.isEmpty());

        return true;
    }

    private Evaluator evaluator(String propertyRegex, boolean defaultAll) {
        String expression = Str.trim(getPropertyByRegexp(propertyRegex));
        if (expression == null)
            if (defaultAll)
                expression = "true";
            else
                return null;
        return ELFactory.evaluator().compile(expression);
    }

    private static class ClassNameTemplate {
        private String name;
        private FullyQualifiedJavaType type;
        private IntrospectedTable table;

        private ClassNameTemplate(String s) {
            name = s;
        }

        public ClassNameTemplate(ClassNameTemplate t) {
            this.name = t.name;
            this.type = t.type;
            this.table = t.table;
        }

        public static List<ClassNameTemplate> parse(String s, String splitBy) {
            List<ClassNameTemplate> list = new ArrayList<ClassNameTemplate>();
            s = Str.trim(s);
            if (s == null) return list;
            String[] parts = s.split(splitBy);
            for (String part : parts) {
                ClassNameTemplate t = ClassNameTemplate.parse(part);
                if (t != null)
                    list.add(t);
            }
            return list;
        }

        public static ClassNameTemplate parse(String s) {
            s = Str.trim(s);
            return s == null ? null : new ClassNameTemplate(s);
        }

        public ClassNameTemplate using(FullyQualifiedJavaType type) {
            ClassNameTemplate t = new ClassNameTemplate(this);
            t.type = type;
            return t;
        }

        public ClassNameTemplate using(IntrospectedTable table) {
            ClassNameTemplate t = new ClassNameTemplate(this);
            t.table = table;
            return t;
        }

        public Pair<String, Set<FullyQualifiedJavaType>> genClassName() {
            String s = name;
            Set<FullyQualifiedJavaType> imports = new HashSet<FullyQualifiedJavaType>();
            if (table != null) {
                Rules rules = table.getRules();
                boolean genPK = rules.generatePrimaryKeyClass(), genBase = rules.generateBaseRecordClass(), genBLOB = rules.generateRecordWithBLOBsClass();
                boolean hasPK = table.hasPrimaryKeyColumns(), hasBase = table.hasBaseColumns(), hasBLOB = table.hasBLOBColumns();
                FullyQualifiedJavaType pk = genPK && hasPK ? new FullyQualifiedJavaType(table.getPrimaryKeyType()) : null;
                FullyQualifiedJavaType base = genBase && (hasBase || !genPK && hasPK || !genBLOB && hasBLOB) ? new FullyQualifiedJavaType(table.getBaseRecordType()) : null;
                FullyQualifiedJavaType blob = genBLOB && hasBLOB ? new FullyQualifiedJavaType(table.getRecordWithBLOBsType()) : null;
                if (!genPK && hasPK)
                    pk = base;
                if (!genBLOB && hasBLOB)
                    blob = base;
                FullyQualifiedJavaType model = Objects.nvl(blob, base, pk);

                s = replaceAndImport(s, "(?i)\\*(example|criteria)", new FullyQualifiedJavaType(table.getExampleType()), imports);
                s = replaceAndImport(s, "(?i)\\*mapper", new FullyQualifiedJavaType(table.getMyBatis3JavaMapperType()), imports);
                s = replaceAndImport(s, "(?i)\\*model", model, imports);
                s = replaceAndImport(s, "(?i)\\*pk", pk, imports);
                s = replaceAndImport(s, "(?i)\\*base", base, imports);
                s = replaceAndImport(s, "(?i)\\*blob", blob, imports);
                if (hasPK) {
                    FullyQualifiedJavaType javaPK = table.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType();
                    s = replaceAndImport(s, "(?i)\\*(java\\.?pk(\\.?class|\\.?type)?|pk\\.?(class|type))", javaPK, imports);
                }
            }
            s = replaceAndImport(s, "\\*", type, imports);
            return Pair.of(s, imports);
        }

        private String replaceAndImport(String s, String pattern, FullyQualifiedJavaType replace, Set<FullyQualifiedJavaType> imports) {
            if (replace == null)
                return s;
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(s);
            if (m.find()) {
                imports.add(replace);
                return m.replaceAll(replace.getShortName());
            }
            return s;
        }

        public Pair<FullyQualifiedJavaType, Set<FullyQualifiedJavaType>> genType() {
            Pair<String, Set<FullyQualifiedJavaType>> cn = genClassName();
            return Pair.of(new FullyQualifiedJavaType(cn.a()), cn.b());
        }

        public void applyAsSuperclass(TopLevelClass topLevelClass) {
            if (topLevelClass.getSuperClass() != null)
                return;
            Pair<FullyQualifiedJavaType, Set<FullyQualifiedJavaType>> cn = using(topLevelClass.getType()).genType();
            topLevelClass.setSuperClass(cn.a());
            topLevelClass.addImportedType(cn.a());
            topLevelClass.addImportedTypes(cn.b());
        }

        public void applyAsSuperinterface(CompilationUnit unit) {
            Pair<FullyQualifiedJavaType, Set<FullyQualifiedJavaType>> cn = using(unit.getType()).genType();
            unit.getSuperInterfaceTypes().add(cn.a());
            unit.addImportedType(cn.a());
            unit.addImportedTypes(cn.b());
        }
    }

    private Context buildContext(CompilationUnit unit, IntrospectedTable table) {
        Context ctx = ELFactory.context().set("table", table).set("class", unit);
        List<FullyQualifiedJavaType> javapk = new ArrayList<FullyQualifiedJavaType>();
        if (table.hasPrimaryKeyColumns()) {
            for (IntrospectedColumn c : table.getPrimaryKeyColumns())
                javapk.add(c.getFullyQualifiedJavaType());
        }
        ctx.set("pk", javapk);
        return ctx;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.BASE_RECORD, introspectedTable)) {
            Context ctx = (modelClassExtends != null || !modelClassImplements.isEmpty()) ? buildContext(topLevelClass, introspectedTable) : null;

            if (modelClassExtends != null) {
                if (modelClassExtendsFilter.evaluateBoolean(ctx))
                    modelClassExtends.using(introspectedTable).applyAsSuperclass(topLevelClass);
            }
            if (!modelClassImplements.isEmpty() && modelClassImplementsFilter.evaluateBoolean(ctx))
                for (ClassNameTemplate t : modelClassImplements) {
                    t.using(introspectedTable).applyAsSuperinterface(topLevelClass);
                }
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.PRIMARY_KEY, introspectedTable)) {
            Context ctx = (modelClassExtends != null || !modelClassImplements.isEmpty()) ? buildContext(topLevelClass, introspectedTable) : null;

            if (modelClassExtends != null) {
                if (modelClassExtendsFilter.evaluateBoolean(ctx))
                    modelClassExtends.using(introspectedTable).applyAsSuperclass(topLevelClass);
            }
            if (!modelClassImplements.isEmpty() && modelClassImplementsFilter.evaluateBoolean(ctx))
                for (ClassNameTemplate t : modelClassImplements) {
                    t.using(introspectedTable).applyAsSuperinterface(topLevelClass);
                }
        }
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.RECORD_WITH_BLOBS, introspectedTable)) {
            Context ctx = (modelClassExtends != null || !modelClassImplements.isEmpty()) ? buildContext(topLevelClass, introspectedTable) : null;

            if (modelClassExtends != null) {
                if (modelClassExtendsFilter.evaluateBoolean(ctx))
                    modelClassExtends.using(introspectedTable).applyAsSuperclass(topLevelClass);
            }
            if (!modelClassImplements.isEmpty() && modelClassImplementsFilter.evaluateBoolean(ctx))
                for (ClassNameTemplate t : modelClassImplements) {
                    t.using(introspectedTable).applyAsSuperinterface(topLevelClass);
                }
        }
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Context ctx = (exampleClassExtends != null || !exampleClassImplements.isEmpty()) ? buildContext(topLevelClass, introspectedTable) : null;

        if (exampleClassExtends != null) {
            if (exampleClassExtendsFilter.evaluateBoolean(ctx))
                exampleClassExtends.using(introspectedTable).applyAsSuperclass(topLevelClass);
        }
        if (!exampleClassImplements.isEmpty() && exampleClassImplementsFilter.evaluateBoolean(ctx))
            for (ClassNameTemplate t : exampleClassImplements) {
                t.using(introspectedTable).applyAsSuperinterface(topLevelClass);
            }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!mapperInterfaceExtends.isEmpty()) {
            Context ctx = buildContext(interfaze, introspectedTable);
            if (mapperInterfaceExtendsFilter.evaluateBoolean(ctx))
                for (ClassNameTemplate t : mapperInterfaceExtends) {
                    t.using(introspectedTable).applyAsSuperinterface(interfaze);
                }
        }
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }
}
