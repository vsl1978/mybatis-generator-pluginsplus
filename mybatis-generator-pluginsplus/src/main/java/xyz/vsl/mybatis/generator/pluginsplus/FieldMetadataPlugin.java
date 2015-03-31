package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import xyz.vsl.mybatis.generator.pluginsplus.el.Context;
import xyz.vsl.mybatis.generator.pluginsplus.el.ELFactory;
import xyz.vsl.mybatis.generator.pluginsplus.el.Evaluator;
import xyz.vsl.mybatis.generator.pluginsplus.el.Tokenizer;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Vladimir Lokhov
 */
public class FieldMetadataPlugin extends IntrospectorPlugin {
    private String annotationClass;
    private String annotationClassSimpleName;
    private String template;
    private String filter;
    private Evaluator filterExpressionEvaluator;
    private Evaluator templateEvaluator;

    @Override
    public boolean validate(List<String> warnings) {
        this.annotationClass = Str.trim(properties.getProperty("annotationClass", properties.getProperty("annotation-class")));
        if (this.annotationClass == null) {
            warnings.add(Messages.load(this).get("requiredProperty", "annotationClass"));
            return false;
        }
        this.template = Str.trim(properties.getProperty("annotationTemplate", properties.getProperty("annotation-template")));
        int lastdot = this.annotationClass.lastIndexOf('.');
        int last$ = this.annotationClass.lastIndexOf('$');
        this.annotationClassSimpleName = this.annotationClass.substring(Math.max(lastdot, last$) + 1);
        if (this.template == null) {
            this.template = "@"+annotationClassSimpleName;
        }
        this.filter = Str.trim(properties.getProperty("fieldFilter", properties.getProperty("field-filter", properties.getProperty("filter"))));
        if (this.filter == null)
            this.filter = "true";
        filterExpressionEvaluator = ELFactory.evaluator().compile(this.filter);
        this.templateEvaluator = ELFactory.evaluator();
        String templateExpression = convertTemplateToExpression(this.template, this.templateEvaluator.getTokenizer());
        templateEvaluator = templateEvaluator.compile(templateExpression);
        return true;
    }

    private String convertTemplateToExpression(String template, Tokenizer tokenizer) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int pos;
        while (start < template.length() && (pos = template.indexOf("#{", start)) >= 0) {
            int last = pos;
            int toClose = 0;
            for (; last < template.length(); last++)
                if (template.charAt(last) == '{')
                    toClose++;
                else if (template.charAt(last) == '}') {
                    toClose--;
                    if (toClose == 0) break;
                }
            if (pos > start) {
                if (sb.length() > 0)
                    sb.append(" + ");
                sb.append('(').append(tokenizer.escape(template.substring(start, pos))).append(')');
            }
            pos += "#{".length();
            if (pos < last) {
                if (sb.length() > 0)
                    sb.append(" + ");
                sb.append('(').append(template.substring(pos, last)).append(')');
            }
            start = last + 1;
        }
        if (start == 0)
            return tokenizer.escape(template);
        if (start < template.length()) {
            sb.append(" + ");
            sb.append('(').append(tokenizer.escape(template.substring(start))).append(')');
        }
        return sb.toString();
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        Context ctx = buildContext(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
        String matches = filterExpressionEvaluator.evaluate(ctx);
        if (Bool.bool(matches, false)) {
            String value = templateEvaluator.evaluate(ctx);
            List<String> annotations = field.getAnnotations();
            boolean replaced = false;
            if (annotations != null)
                for (ListIterator<String> it = annotations.listIterator(); it.hasNext(); ) {
                    String a = Str.trim(it.next());
                    if (a == null) continue;
                    if (a.startsWith(annotationClass, 1) || a.startsWith(annotationClassSimpleName, 1)) {
                        it.set(value);
                        replaced = true;
                        break;
                    }
                }
            if (!replaced) {
                field.addAnnotation(value);
                topLevelClass.addImportedType(annotationClass);
            }
        }
        return true;
    }

    private Context buildContext(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        Context ctx = ELFactory.context();
        ctx.set("field", field);
        ctx.set("column", introspectedColumn);
        ctx.set("table", introspectedTable);
        return ctx;
    }


}
