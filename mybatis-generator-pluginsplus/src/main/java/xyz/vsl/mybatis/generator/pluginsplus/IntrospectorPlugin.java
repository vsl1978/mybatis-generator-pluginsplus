package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * @author Vladimir Lokhov
 */
abstract class IntrospectorPlugin extends PluginAdapter {

    static class TableName {
        final String catalog;
        final String schema;
        final String table;

        public TableName(TableConfiguration tc, DatabaseMetaData dbmd) throws SQLException {
            String localCatalog = tc.getCatalog();
            String localSchema = tc.getSchema();
            String localTableName = tc.getTableName();
            try {
                boolean delimitIdentifiers = tc.isDelimitIdentifiers() || Str.containsSpaces(localCatalog, localSchema, localTableName);
                if (!delimitIdentifiers && dbmd.storesLowerCaseIdentifiers()) {
                    localCatalog = Str.lower(localCatalog);
                    localSchema = Str.lower(localSchema);
                    localTableName = Str.lower(localTableName);
                } else if (!delimitIdentifiers && dbmd.storesUpperCaseIdentifiers()) {
                    localCatalog = Str.upper(localCatalog);
                    localSchema = Str.upper(localSchema);
                    localTableName = Str.upper(localTableName);
                }
                if (tc.isWildcardEscapingEnabled()) {
                    String searchStringEscape = dbmd.getSearchStringEscape();
                    localSchema = escape(localSchema, searchStringEscape);
                    localTableName = escape(localTableName, searchStringEscape);
                }

            } finally {
                catalog = localCatalog;
                schema = localSchema;
                table = localTableName;
            }
        }

        private String escape(String s, String escape) {
            if (s == null) return s;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '_' || c == '%') sb.append(escape);
                sb.append(c);
            }
            return sb.toString();
        }
    }

    Connection getConnection() {
        try {
            Method m = Context.class.getDeclaredMethod("getConnection");
            m.setAccessible(true);
            return (Connection)m.invoke(context);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    void closeConnection(Connection conn) {
        try {
            Method m = Context.class.getDeclaredMethod("closeConnection", Connection.class);
            m.setAccessible(true);
            m.invoke(context, conn);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    XmlElement getMapperXmlRoot(GeneratedXmlFile x) {
        Document doc = getDocument(x);
        if (doc == null) return null;
        if (doc.getPublicId() != null && !"-//mybatis.org//DTD Mapper 3.0//EN".equals(doc.getPublicId()))
            return null;
        if (doc.getRootElement() == null || !"mapper".equals(doc.getRootElement().getName()))
            return null;
        return doc.getRootElement();
    }

    private Document getDocument(GeneratedXmlFile file) {
        try {
            return (Document)xmlDocument.get(file);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    String getProperty(String ... names) {
        for (String name : names) {
            String value = Str.trim(properties.getProperty(name));
            if (value != null)
                return value;
        }
        return null;
    }

    String getPropertyByRegexp(String name) {
        Pattern p = Pattern.compile(name);
        for (String key : properties.stringPropertyNames())
            if (p.matcher(key).matches())
                return properties.getProperty(key);
        return null;
    }

    private static Field xmlDocument;

    static {
        try {
            xmlDocument = GeneratedXmlFile.class.getDeclaredField("document");
            xmlDocument.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
