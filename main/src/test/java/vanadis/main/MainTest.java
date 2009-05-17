package vanadis.main;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;

public class MainTest {

    @Test
    public void parseOnlyBlueprintNames() {
        assertFooBarOnly(new Main.Specification("-blueprints foo,bar"));
    }

    @Test
    public void parseOnlyNames() {
        assertFooBarOnly(new Main.Specification("foo,bar"));
    }

    @Test
    public void parseOnlySpacedNames() {
        assertFooBarOnly(new Main.Specification("foo bar"));
    }

    @Test
    public void parseNothing() {
        assertEmpty(new Main.Specification(""));
    }

    private static void assertFooBarOnly(Main.Specification ms) {
        assertEquals(Arrays.asList("foo", "bar"), ms.getBlueprintNames());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }

    private static void assertEmpty(Main.Specification ms) {
        assertTrue(ms.getBlueprintNames().toString(), ms.getBlueprintNames().isEmpty());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }
}
