package xyz.vsl.mybatis.generator.pluginsplus;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Vladimir Lokhov
 */
public class StrTest {

    @Test
    public void trim() {
        assertNull(Str.trim(null));
        assertNull(Str.trim(""));
        assertNull(Str.trim(" "));
        assertNull(Str.trim(" \t\n "));
        assertNotNull(Str.trim("."));
        assertNotNull(Str.trim(" . "));
    }

    @Test
    public void group() {
        assertNull("asdf : a -> null", Str.group("asdf", "a"));
        assertNull("asdf : q -> null", Str.group("asdf", "q"));
        assertEquals("asdf : (s) -> s", Str.group("asdf", "(s)"), "s");
        assertEquals("asdf : ^.(..) -> sd", Str.group("asdf", "^.(..)"), "sd");
        assertNull("asdf : ^.{5}(.+) -> null", Str.group("asdf", "^.{5}(.+)"));
    }

}