package xyz.vsl.mybatis.generator.pluginsplus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static xyz.vsl.mybatis.generator.pluginsplus.Objects.findAnnotaion;

/**
 * @author Vladimir Lokhov
 */
public class FindAnnotationTest {

    private List<String> annotations;

    @Before
    public void initialize() {
        annotations = new ArrayList<String>();
        annotations.add("@NotNull");
        annotations.add("\t@\tColumn(name=\"test\")");
    }

    @Test
    public void found() {
        assertEquals("#0, null", 0, findAnnotaion(annotations, "javax.validation.constraints.NotNull", null));
        assertEquals("#0, empty string", 0, findAnnotaion(annotations, "javax.validation.constraints.NotNull", ""));
        assertEquals("#0, with simple name", 0, findAnnotaion(annotations, "javax.validation.constraints.NotNull", "NotNull"));
        assertEquals("#1, null", 1, findAnnotaion(annotations, "javax.persistance.Column", null));
        assertEquals("#1, empty string", 1, findAnnotaion(annotations, "javax.persistance.Column", ""));
        assertEquals("#1, with simple name", 1, findAnnotaion(annotations, "javax.persistance.Column", "Column"));
        assertEquals("#1, simple name-1", 1, findAnnotaion(annotations, "Column", null));
        assertEquals("#1, simple name-2", 1, findAnnotaion(annotations, "Column", "Column"));
        assertEquals("#2, invalid names", 1, findAnnotaion(annotations, "Table", "Column"));
        assertEquals("#3, invalid names", 0, findAnnotaion(annotations, "NotNull", "Column"));
    }

    @Test
    public void notFound() {
        assertEquals("list is null", -1, findAnnotaion(null, "javax.validation.constraints.NotNull", null));
        assertEquals("empty list", -1, findAnnotaion(Collections.<String>emptyList(), "javax.validation.constraints.NotNull", null));
        assertEquals("empty string", -1, findAnnotaion(annotations, "", ""));
        assertEquals("name is null", -1, findAnnotaion(annotations, null, ""));
        assertEquals("unknown name", -1, findAnnotaion(annotations, "there.are.no.such.Annotation", null));
        assertEquals("unknown name with simple name", -1, findAnnotaion(annotations, "there.are.no.such.Annotation", "Annotation"));
        assertEquals("too short", -1, findAnnotaion(annotations, "xyz.vsl.Not", null));
        assertEquals("too short 2", -1, findAnnotaion(annotations, "xyz.vsl.Not", "Not"));
        assertEquals("too long", -1, findAnnotaion(annotations, "xyz.vsl.ColumnSize", null));
        assertEquals("too long 2", -1, findAnnotaion(annotations, "xyz.vsl.ColumnSize", "ColumnSize"));
    }
}
