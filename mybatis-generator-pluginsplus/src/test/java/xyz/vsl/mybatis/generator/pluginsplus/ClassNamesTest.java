package xyz.vsl.mybatis.generator.pluginsplus;

import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.internal.rules.Rules;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Vladimir Lokhov
 */
public class ClassNamesTest {
    private static class Table {
        String pk, base, blob, example, mapper;
        boolean hasBLOB;
        boolean generatePK, generateBLOB;

        Table(String pk, String base, String blob, String example, String mapper, boolean hasBLOB, boolean generatePK, boolean generateBLOB) {
            this.pk = pk;
            this.base = base;
            this.blob = blob;
            this.example = example;
            this.mapper = mapper;
            this.hasBLOB = hasBLOB;
            this.generatePK = generatePK;
            this.generateBLOB = generateBLOB;
        }
    }

    private Rules getRules(final Table config) {
        return new Rules() {
            @Override
            public boolean generateInsert() {
                return true;
            }

            @Override
            public boolean generateInsertSelective() {
                return true;
            }

            @Override
            public FullyQualifiedJavaType calculateAllFieldsClass() {
                return null;
            }

            @Override
            public boolean generateUpdateByPrimaryKeyWithoutBLOBs() {
                return true;
            }

            @Override
            public boolean generateUpdateByPrimaryKeyWithBLOBs() {
                return true;
            }

            @Override
            public boolean generateUpdateByPrimaryKeySelective() {
                return true;
            }

            @Override
            public boolean generateDeleteByPrimaryKey() {
                return true;
            }

            @Override
            public boolean generateDeleteByExample() {
                return true;
            }

            @Override
            public boolean generateBaseResultMap() {
                return true;
            }

            @Override
            public boolean generateResultMapWithBLOBs() {
                return true;
            }

            @Override
            public boolean generateSQLExampleWhereClause() {
                return true;
            }

            @Override
            public boolean generateMyBatis3UpdateByExampleWhereClause() {
                return true;
            }

            @Override
            public boolean generateBaseColumnList() {
                return true;
            }

            @Override
            public boolean generateBlobColumnList() {
                return true;
            }

            @Override
            public boolean generateSelectByPrimaryKey() {
                return true;
            }

            @Override
            public boolean generateSelectByExampleWithoutBLOBs() {
                return true;
            }

            @Override
            public boolean generateSelectByExampleWithBLOBs() {
                return true;
            }

            @Override
            public boolean generateExampleClass() {
                return true;
            }

            @Override
            public boolean generateCountByExample() {
                return true;
            }

            @Override
            public boolean generateUpdateByExampleSelective() {
                return true;
            }

            @Override
            public boolean generateUpdateByExampleWithoutBLOBs() {
                return true;
            }

            @Override
            public boolean generateUpdateByExampleWithBLOBs() {
                return true;
            }

            @Override
            public boolean generatePrimaryKeyClass() {
                return config.generatePK;
            }

            @Override
            public boolean generateBaseRecordClass() {
                return true;
            }

            @Override
            public boolean generateRecordWithBLOBsClass() {
                return config.generateBLOB;
            }

            @Override
            public boolean generateJavaClient() {
                return true;
            }

            @Override
            public IntrospectedTable getIntrospectedTable() {
                return null;
            }
        };
    }
    private IntrospectedTable getTable(final Table config) {
        final Rules r = getRules(config);
        return new IntrospectedTableMyBatis3Impl() {
            @Override
            public String getPrimaryKeyType() {
                return config.pk;
            }

            @Override
            public String getBaseRecordType() {
                return config.base;
            }

            @Override
            public String getExampleType() {
                return config.example;
            }

            @Override
            public String getRecordWithBLOBsType() {
                return config.blob;
            }

            @Override
            public boolean hasBLOBColumns() {
                return config.hasBLOB;
            }

            @Override
            public boolean hasBaseColumns() {
                return true;
            }

            @Override
            public Rules getRules() {
                return r;
            }

            @Override
            public boolean hasPrimaryKeyColumns() {
                return true;
            }

            @Override
            public String getMyBatis3JavaMapperType() {
                return config.mapper;
            }
        };
    }

    @Test
    public void testExample1() {
        // *SqlProvider, *DAO, *DAOImpl
        IntrospectedTable t = getTable(new Table("test.common.FooKey", "test.common.Foo", "test.common.FooWithBLOBs", "test.common.FooExample", "test.common.FooMapper", true, true, true));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "FooMapper");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNull(cls.imports.mapper);

        assertTrue(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertTrue(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);
    }

    @Test
    public void testExample2() {
        IntrospectedTable t = getTable(new Table("test.common.FooKey", "test.common.Foo", "test.common.FooWithBLOBs", "test.common.FooExample", "test.common.FooMapper", false, true, true));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "FooMapper");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNull(cls.imports.mapper);

        assertTrue(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testExample3() {
        IntrospectedTable t = getTable(new Table("test.common.FooKey", "test.common.Foo", "test.common.FooWithBLOBs", "test.common.FooExample", "test.common.FooMapper", false, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "FooMapper");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testExample4() {
        IntrospectedTable t = getTable(new Table("test.model.FooKey", "test.model.Foo", "test.model.FooWithBLOBs", "test.model.FooExample", "test.dao.FooMapper", false, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "FooMapper");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNotNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testExample5() {
        IntrospectedTable t = getTable(new Table("test.model.FooKey", "test.model.Foo", "test.model.FooWithBLOBs", "test.model.FooExample", "test.dao.FooMapper", true, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "FooMapper");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNotNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testExample6() {
        IntrospectedTable t = getTable(new Table("test.model.FooKey", "test.model.Foo", "test.model.FooWithBLOBs", "test.model.FooExample", "test.dao.Foo", true, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "FooKey");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "FooWithBLOBs");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "test.dao.Foo");

        assertNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNotNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testExample7() {
        IntrospectedTable t = getTable(new Table("test.model.pk.Foo", "test.model.Foo", "test.model.blobs.Foo", "test.model.FooExample", "test.dao.Foo", true, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getExampleType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "Foo");
        assertEquals(cls.names.base, "test.model.Foo");
        assertEquals(cls.names.blob, "test.model.blobs.Foo");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "test.dao.Foo");

        assertNotNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNotNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNotNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }

    @Test
    public void testBase1() {
        IntrospectedTable t = getTable(new Table("test.model.pk.Foo", "test.model.Foo", "test.model.blobs.Foo", "test.model.FooExample", "test.dao.Foo", true, false, false));

        MyBatisClasses cls = MyBatisClasses.calculate(new TopLevelClass(new FullyQualifiedJavaType(t.getBaseRecordType())), t);

        assertNotNull(cls);
        assertNotNull(cls.names);
        assertNotNull(cls.types);
        assertNotNull(cls.imports);
        assertNotNull(cls.exists);

        assertEquals(cls.names.pk, "test.model.pk.Foo");
        assertEquals(cls.names.base, "Foo");
        assertEquals(cls.names.blob, "test.model.blobs.Foo");
        assertEquals(cls.names.example, "FooExample");
        assertEquals(cls.names.mapper, "test.dao.Foo");

        assertNotNull(cls.imports.pk);
        assertNull(cls.imports.base);
        assertNotNull(cls.imports.blob);
        assertNull(cls.imports.example);
        assertNotNull(cls.imports.mapper);

        assertFalse(cls.exists.pk);
        assertTrue(cls.exists.base);
        assertFalse(cls.exists.blob);
        assertTrue(cls.exists.example);
        assertTrue(cls.exists.mapper);

    }
}
