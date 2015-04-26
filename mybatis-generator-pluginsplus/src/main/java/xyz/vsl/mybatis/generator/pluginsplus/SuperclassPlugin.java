package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public class SuperclassPlugin extends IntrospectorPlugin {
    private ClassNameTemplate modelClassExtends;
    private ClassNameTemplate exampleClassExtends;
    private List<ClassNameTemplate> modelClassImplements;
    private List<ClassNameTemplate> exampleClassImplements;
    private List<ClassNameTemplate> mapperInterfaceExtends;

    @Override
    public boolean validate(List<String> list) {
        modelClassExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)model(-?class)?-?extends?"));
        exampleClassExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)(example|criteria)(-?class)?-?extends?"));
        modelClassImplements = ClassNameTemplate.parse(getPropertyByRegexp("(?i)model(-?class)?-?implements?"), ";");
        exampleClassImplements = ClassNameTemplate.parse(getPropertyByRegexp("(?i)(example|criteria)(-?class)?-?implements?"), ";");
        mapperInterfaceExtends = ClassNameTemplate.parse(getPropertyByRegexp("(?i)mapper(-?class|-?interface)?(-?implements?|-?extends?)"), ";");
        return true;
    }

    private static class ClassNameTemplate {
        private String name;
        private int pos;

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

        private ClassNameTemplate(String s) {
            name = s;
            pos = name.indexOf('*');
        }

        public String getClassName(FullyQualifiedJavaType current) {
            if (pos < 0)
                return name;
            return name.substring(0, pos) + current.getShortName() + name.substring(pos + 1);
        }

        public FullyQualifiedJavaType getType(FullyQualifiedJavaType current) {
            return new FullyQualifiedJavaType(getClassName(current));
        }

        public void applyAsSuperclass(TopLevelClass topLevelClass) {
            if (topLevelClass.getSuperClass() != null)
                return;
            FullyQualifiedJavaType javaType = getType(topLevelClass.getType());
            topLevelClass.setSuperClass(javaType);
            topLevelClass.addImportedType(javaType);
        }

        public void applyAsSuperinterface(CompilationUnit unit) {
            FullyQualifiedJavaType javaType = getType(unit.getType());
            unit.getSuperInterfaceTypes().add(javaType);
            unit.addImportedType(javaType);
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.BASE_RECORD, introspectedTable)) {
            if (modelClassExtends != null) {
                modelClassExtends.applyAsSuperclass(topLevelClass);
            }
            for (ClassNameTemplate t : modelClassImplements) {
                t.applyAsSuperinterface(topLevelClass);
            }
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.PRIMARY_KEY, introspectedTable)) {
            if (modelClassExtends != null) {
                modelClassExtends.applyAsSuperclass(topLevelClass);
            }
            for (ClassNameTemplate t : modelClassImplements) {
                t.applyAsSuperinterface(topLevelClass);
            }
        }
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (MBGenerator.isTopmostModelClass(ModelClassType.RECORD_WITH_BLOBS, introspectedTable)) {
            if (modelClassExtends != null) {
                modelClassExtends.applyAsSuperclass(topLevelClass);
            }
            for (ClassNameTemplate t : modelClassImplements) {
                t.applyAsSuperinterface(topLevelClass);
            }
        }
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (exampleClassExtends != null) {
            exampleClassExtends.applyAsSuperclass(topLevelClass);
        }
        for (ClassNameTemplate t : exampleClassImplements) {
            t.applyAsSuperinterface(topLevelClass);
        }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (ClassNameTemplate t : mapperInterfaceExtends) {
            t.applyAsSuperinterface(interfaze);
        }
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }
}
