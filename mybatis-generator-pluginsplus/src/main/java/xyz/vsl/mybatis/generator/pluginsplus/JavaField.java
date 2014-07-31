package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.dom.java.Field;

/**
 * @author Vladimir Lokhov
 */
class JavaField extends Field {

    public JavaField javadoc(String ... lines) {
        addJavaDocLine("/**");
        for (String line : lines) if (line != null) addJavaDocLine(" * "+line);
        addJavaDocLine(" */");
        return this;
    }
}
