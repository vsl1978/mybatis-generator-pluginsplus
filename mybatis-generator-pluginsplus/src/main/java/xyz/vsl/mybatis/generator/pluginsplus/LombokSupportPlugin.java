package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import xyz.vsl.mybatis.generator.pluginsplus.el.*;

import java.util.Iterator;
import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public class LombokSupportPlugin extends IntrospectorPlugin {
    private boolean lombokGetters;
    private boolean lombokSetters;
    private String  lombokBuilderAnnotation;
    private String  lombokAccessorAnnotation;
    private Evaluator lombokToStringFilter;
    private Evaluator lombokToStringTemplate;
    private Evaluator equalsAndHashTemplate;
    private Evaluator equalsAndHashFilter;

    @Override
    public boolean validate(List<String> list) {
        String filter, tpl, value;
        lombokGetters = Bool.bool(getPropertyByRegexp("(?i)(use-?)?lombok-?getters"), true);
        lombokSetters = Bool.bool(getPropertyByRegexp("(?i)(use-?)?lombok-?setters"), true);

        value = Str.trim(getPropertyByRegexp("(?i)(use-?)?(lombok-?)?builder"));
        if (value != null && Bool.bool(value, true) /* not 'false' */) {
            if (!Bool.bool(value, false) /* not 'true' */)
                lombokBuilderAnnotation = value;
            else
                lombokBuilderAnnotation = "lombok.Builder";
        }
        else lombokBuilderAnnotation = null;

        value = Str.trim(getPropertyByRegexp("(?i)(use-?)?(lombok-?)?accessor"));
        if (value != null && Bool.bool(value, true) /* not 'false' */) {
            if (!Bool.bool(value, false) /* not 'true' */)
                lombokAccessorAnnotation = value;
            else
                lombokAccessorAnnotation = "lombok.experimental.Accessor";
            lombokSetters = true;
            lombokBuilderAnnotation = null;
        }
        else lombokAccessorAnnotation = null;

        filter = getPropertyByRegexp("(?i)to-?string-?filter");
        tpl = getPropertyByRegexp("(?i)to-?string-?(annotation-?template|annotation|template)");
        if (filter == null && tpl != null)
            filter = "true";
        if (tpl == null && filter != null)
            tpl = "@ToString";
        lombokToStringFilter = filter != null ? ELFactory.evaluator().compile(filter) : null;
        lombokToStringTemplate = template(tpl);

        filter = getPropertyByRegexp("(?i)equals(-?and)?-?hash(code)?-?filter");
        tpl = getPropertyByRegexp("(?i)equals(-?and)?-?hash(code)?-?(annotation-?template|annotation|template)");
        equalsAndHashFilter = filter != null ? ELFactory.evaluator().compile(filter) : null;
        equalsAndHashTemplate = template(Objects.nvl(tpl, "@EqualsAndHashCode"));

        return true;
    }

    private Evaluator template(String template) {
        if (template == null)
            return null;
        Evaluator e = ELFactory.evaluator();
        String templateExpression = ELUtils.convertTemplateToExpression(template, e.getTokenizer());
        e = e.compile(templateExpression);
        return e;
    }


    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (lombokSetters && !field.isFinal()) {
            topLevelClass.addImportedType("lombok.Setter");
            field.addAnnotation("@Setter");
        }
        if (lombokGetters) {
            topLevelClass.addImportedType("lombok.Getter");
            field.addAnnotation("@Getter");
        }
        return true;
    }

    private Context buildContext(final TopLevelClass topLevelClass, final IntrospectedTable introspectedTable, final ModelClassType modelClassType) {
        Context ctx = ELFactory.context();
        ctx.set("table", introspectedTable);
        ctx.set("class", topLevelClass);
        ctx.set("model", modelClassType);
        return ctx;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !lombokSetters;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !lombokGetters;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processClass(topLevelClass, introspectedTable, ModelClassType.BASE_RECORD);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processClass(topLevelClass, introspectedTable, ModelClassType.PRIMARY_KEY);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processClass(topLevelClass, introspectedTable, ModelClassType.RECORD_WITH_BLOBS);
        return true;
    }

    private void processClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        Context ctx = buildContext(topLevelClass, introspectedTable, modelClassType);
        if (lombokToStringFilter != null) {
            if (Objects.findAnnotaion(topLevelClass.getAnnotations(), "lombok.ToString") < 0) {
                if (Bool.bool(lombokToStringFilter.evaluate(ctx), false)) {
                    topLevelClass.addImportedType("lombok.ToString");
                    topLevelClass.addAnnotation(lombokToStringTemplate.evaluate(ctx));
                    removeMethod(topLevelClass, "toString");
                }
            }
        }
        if (equalsAndHashFilter != null) {
            if (Objects.findAnnotaion(topLevelClass.getAnnotations(), "lombok.EqualsAndHashCode") < 0) {
                if (Bool.bool(equalsAndHashFilter.evaluate(ctx), false)) {
                    topLevelClass.addImportedType("lombok.EqualsAndHashCode");
                    topLevelClass.addAnnotation(equalsAndHashTemplate.evaluate(ctx));
                    removeMethod(topLevelClass, "equals", FullyQualifiedJavaType.getObjectInstance());
                    removeMethod(topLevelClass, "hashCode");
                }
            }
        }

        if (lombokBuilderAnnotation != null && Objects.findAnnotaion(topLevelClass.getAnnotations(), classNameOnly(lombokBuilderAnnotation)) < 0) {
            /*
            System.err.println("pk: "+introspectedTable.getPrimaryKeyType()+", "+introspectedTable.getPrimaryKeyColumns()+", "+introspectedTable.getTableConfiguration().getModelType()+", "+introspectedTable.getRules().generatePrimaryKeyClass());
            System.err.println("base: "+introspectedTable.getBaseRecordType()+", "+introspectedTable.getBaseColumns()+", "+introspectedTable.getTableConfiguration().getModelType()+", "+introspectedTable.getRules().generateBaseRecordClass());
            System.err.println("blob: "+introspectedTable.getRecordWithBLOBsType()+", "+introspectedTable.getBLOBColumns()+", "+introspectedTable.getTableConfiguration().getModelType()+", "+introspectedTable.getRules().generateRecordWithBLOBsClass());
            */
            if (MBGenerator.isUndermostModelClass(modelClassType, introspectedTable)) {
                //topLevelClass.addImportedType(lombokBuilderAnnotation);
                topLevelClass.addAnnotation(inlineAnnotation(lombokBuilderAnnotation));
            }
            topLevelClass.addAnnotation("@lombok.NoArgsConstructor");
            topLevelClass.addAnnotation("@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)");
        }

        if (lombokAccessorAnnotation != null && Objects.findAnnotaion(topLevelClass.getAnnotations(), classNameOnly(lombokAccessorAnnotation)) < 0) {
            //topLevelClass.addImportedType(lombokBuilderAnnotation);
            topLevelClass.addAnnotation(inlineAnnotation(lombokAccessorAnnotation));
        }
    }

    private void removeMethod(InnerClass klazz, String methodName, FullyQualifiedJavaType ... parameters) {
        if (klazz == null || klazz.getMethods() == null || klazz.getMethods().isEmpty())
            return;
        if (methodName == null)
            return;
        search:
        for (Iterator<Method> it = klazz.getMethods().iterator(); it.hasNext(); ) {
            Method m = it.next();
            if (!methodName.equals(m.getName())) continue;
            if (parameters.length != (m.getParameters() == null ? 0 : m.getParameters().size())) continue;
            if (parameters.length > 0) {
                int idx = 0;
                for (Parameter p : m.getParameters()) {
                    FullyQualifiedJavaType a = parameters[idx++];
                    if (!Objects.equals(a, p.getType())) continue search;
                }
            }
            it.remove();
            return;
        }
    }

    private String classNameOnly(String annotation) {
        annotation = Str.trim(annotation);
        if (annotation == null)
            return null;
        int pos1 = annotation.indexOf('@');
        int pos2 = annotation.indexOf('(');
        if (pos1 < pos2) { /* .... @class(.... or class(..... */
            annotation = annotation.substring(pos1 + 1, pos2);
        }
        else if (pos2 > 0) { /* .... class(...@... */
            annotation = annotation.substring(0, pos2);
        }
        else if (pos1 > 0) { /* ....@class */
            annotation = annotation.substring(pos1 + 1);
        }
        return Str.trim(annotation);
    }

    private String inlineAnnotation(String annotation) {
        annotation = Str.trim(annotation);
        if (annotation == null)
            return null;
        int pos1 = annotation.indexOf('@');
        int pos2 = annotation.indexOf('(');
        return (pos1 < 0 || pos2 > 0 && pos1 > pos2) ? "@" + annotation : annotation;
    }
}
