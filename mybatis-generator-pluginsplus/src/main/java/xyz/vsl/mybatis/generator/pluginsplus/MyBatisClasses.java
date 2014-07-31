package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.rules.Rules;

import java.util.Arrays;

/**
 *
 * @author Vladimir Lokhov
 */
class MyBatisClasses {
    public class Types<T> {
        public final T pk;
        public final T base;
        public final T blob;
        public final T example;
        public final T mapper;
        Types(T pk, T base, T blob, T example, T mapper) {
            this.pk = pk;
            this.base = base;
            this.blob = blob;
            this.example = example;
            this.mapper = mapper;
        }
    }
    public final Types<String> names;
    public final Types<Boolean> exists;
    public final Types<FullyQualifiedJavaType> types;
    public final Types<FullyQualifiedJavaType> imports;

    private MyBatisClasses(IntrospectedTable introspectedTable, FullyQualifiedJavaType klazz, FullyQualifiedJavaType pk, FullyQualifiedJavaType base, FullyQualifiedJavaType blob, FullyQualifiedJavaType example, FullyQualifiedJavaType mapper) {
        Rules rules = introspectedTable.getRules();

        FullyQualifiedJavaType[] types = {pk, base, blob, example, mapper};
        String fqklazz = klazz.getFullyQualifiedName().intern();
        String[] fq = {pk.getFullyQualifiedName().intern(), base.getFullyQualifiedName().intern(), blob.getFullyQualifiedName().intern(), example.getFullyQualifiedName().intern(), mapper.getFullyQualifiedName().intern()};

        String[] s = {pk.getShortName().intern(), base.getShortName().intern(), blob.getShortName().intern(), example.getShortName().intern(), mapper.getShortName().intern()};

        boolean[][] efq = compare(fq);
        boolean[][] es = compare(s);
        boolean[][] ep = compare(pk.getPackageName().intern(), base.getPackageName().intern(), blob.getPackageName().intern(), example.getPackageName().intern(), mapper.getPackageName().intern());

        int idx = -1;
        for (int i = 0; i < fq.length && idx < 0; i++) if (fqklazz == fq[i]) idx = i;

        String[] names = new String[fq.length];
        boolean[] imports = new boolean[fq.length];

        if (idx >= 0) {
            for (int i = 0; i < es.length; i++) {
                if (i == idx) {
                    names[i] = s[idx];
                    imports[i] = false;
                } else if (es[idx][i]) {
                    /* equal class names */
                    names[i] = ep[idx][i] ? /* same class */ s[i] : fq[i];
                    imports[i] = !ep[idx][i];
                } else {
                    names[i] = s[i];
                    imports[i] = !ep[idx][i];
                    for (int j = 0; j < i; j++) if (es[i][j]) names[i] = fq[i];
                }
            }
        } else {
            names = fq;
            Arrays.fill(imports, true);
        }

        this.names = new Types<String>(names[0], names[1], names[2], names[3], names[4]);
        this.imports  = new Types<FullyQualifiedJavaType>(imports[0] ? types[0] : null, imports[1] ? types[1] : null, imports[2] ? types[2] : null, imports[3] ? types[3] : null, imports[4] ? types[4] : null);
        this.types = new Types<FullyQualifiedJavaType>(pk, base, blob, example, mapper);

        this.exists = new Types<Boolean>(introspectedTable.hasPrimaryKeyColumns() && rules.generatePrimaryKeyClass(), rules.generateBaseRecordClass(), introspectedTable.hasBLOBColumns() && rules.generateRecordWithBLOBsClass(), rules.generateExampleClass(), true);
    }

    private boolean[][] compare(String ... strings) {
        boolean[][] eq = new boolean[strings.length][];
        for (int i = 0; i < eq.length; i++) {
            eq[i] = new boolean[strings.length];
        }
        for (int i = 0; i < eq.length; i++) {
            for (int j = i; j < strings.length; j++) eq[i][j] = eq[j][i] = strings[i] == strings[j];
        }
        return eq;
    }

    public static MyBatisClasses calculate(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType pk = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        FullyQualifiedJavaType base = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType blob = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType klazz = topLevelClass.getType();
        return new MyBatisClasses(introspectedTable, klazz, pk, base, blob, example, mapper);
    }
}
