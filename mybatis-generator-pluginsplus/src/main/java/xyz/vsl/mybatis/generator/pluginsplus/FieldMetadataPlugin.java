package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import xyz.vsl.mybatis.generator.pluginsplus.el.*;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Vladimir Lokhov
 */
public class FieldMetadataPlugin extends IntrospectorPlugin {
    private String annotationClass;
    private String annotationClassSimpleName;
    private Evaluator filterExpressionEvaluator;
    private Evaluator fieldTemplateEvaluator;
    private Evaluator setterTemplateEvaluator;
    private Evaluator setterParameterTemplateEvaluator;
    private Evaluator getterTemplateEvaluator;

    @Override
    public boolean validate(List<String> warnings) {
        this.annotationClass = getProperty("annotationClass", "annotation-class", "annotation");
        if (this.annotationClass == null) {
            warnings.add(Messages.load(this).get("requiredProperty", "annotation-class"));
            return false;
        }
        int lastdot = this.annotationClass.lastIndexOf('.');
        int last$ = this.annotationClass.lastIndexOf('$');
        this.annotationClassSimpleName = this.annotationClass.substring(Math.max(lastdot, last$) + 1);

        filterExpressionEvaluator = ELFactory.evaluator().compile(Objects.nvl(getProperty("fieldFilter", "field-filter", "filter"), "false"));

        setterTemplateEvaluator = template(getProperty("setter-annotation-template", "setterAnnotationTemplate", "setter-annotation", "setterAnnotation"));
        setterParameterTemplateEvaluator = template(getProperty("setter-parameter-annotation-template", "setterParameterAnnotationTemplate", "setter-param-annotation-template", "setterParamAnnotationTemplate", "parameter-annotation-template", "parameterAnnotationTemplate", "parameter-annotation", "parameterAnnotation"));
        getterTemplateEvaluator = template(getProperty("getter-annotation-template", "getterAnnotationTemplate", "getter-annotation", "getterAnnotation"));
        boolean hasMethodTemplates = Objects.nvl(setterTemplateEvaluator, setterParameterTemplateEvaluator, getterTemplateEvaluator) != null;
        String defaultTemplate = hasMethodTemplates ? null : ("@" + annotationClassSimpleName);
        fieldTemplateEvaluator = template(Objects.nvl(getProperty("field-annotation-template", "annotationTemplate", "annotation-template", "field-annotation", "fieldAnnotation"), defaultTemplate));

        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (fieldTemplateEvaluator == null)
            return true;
        Context ctx = buildContext(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
        String matches = filterExpressionEvaluator.evaluate(ctx);
        if (Bool.bool(matches, false))
            addAnnotation(ctx, fieldTemplateEvaluator, field, topLevelClass);
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (setterTemplateEvaluator == null && setterParameterTemplateEvaluator == null)
            return true;
        Field field = getField(topLevelClass, introspectedColumn.getJavaProperty());
        Context ctx = buildContext(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType).set("method", method);
        String matches = filterExpressionEvaluator.evaluate(ctx);
        if (Bool.bool(matches, false)) {
            addAnnotation(ctx, setterTemplateEvaluator, method, topLevelClass);
            addAnnotation(ctx, setterParameterTemplateEvaluator, method.getParameters().get(0), topLevelClass);
        }
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (getterTemplateEvaluator == null)
            return true;
        Field field = getField(topLevelClass, introspectedColumn.getJavaProperty());
        Context ctx = buildContext(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType).set("method", method);
        String matches = filterExpressionEvaluator.evaluate(ctx);
        if (Bool.bool(matches, false))
            addAnnotation(ctx, getterTemplateEvaluator, method, topLevelClass);
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

    private Context buildContext(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        Context ctx = ELFactory.context();
        ctx.set("field", field);
        ctx.set("column", introspectedColumn);
        ctx.set("table", introspectedTable);
        return ctx;
    }

    private void addAnnotation(Context ctx, Evaluator template, JavaElement target, TopLevelClass topLevelClass) {
        if (template == null)
            return;
        String value = template.evaluate(ctx);
        if (!replaceExistingAnnotation(target.getAnnotations(), value)) {
            target.addAnnotation(value);
            topLevelClass.addImportedType(annotationClass);
        }
    }

    private void addAnnotation(Context ctx, Evaluator template, Parameter target, TopLevelClass topLevelClass) {
        if (template == null)
            return;
        String value = template.evaluate(ctx);
        if (!replaceExistingAnnotation(target.getAnnotations(), value)) {
            target.addAnnotation(value);
            topLevelClass.addImportedType(annotationClass);
        }
    }

    private boolean replaceExistingAnnotation(List<String> annotations, String newValue) {
        int index = Objects.findAnnotaion(annotations, annotationClass, annotationClassSimpleName);
        if (index < 0)
            return false;
        annotations.set(index, newValue);
        return true;
    }

    private Field getField(TopLevelClass topLevelClass, String javaPropertyName) {
        if (javaPropertyName == null)
            return null;
        List<Field> fields = topLevelClass.getFields();
        if (fields == null)
            return null;
        for (Field f : fields) {
            if (javaPropertyName.equals(f.getName()))
                return f;
        }
        //System.err.println("--------------------------- Unable to find field '" + javaPropertyName + "' in " + topLevelClass.getType().getFullyQualifiedName());
        return null;
    }


}
