package vanadis.main;

import org.junit.Test;
import vanadis.launcher.ArgumentsSpecs;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public void parseOnlyBlueprintNames() {
        assertFooBarOnly(new ArgumentsSpecs("-blueprints foo,bar"));
    }

    @Test
    public void parseOnlyNames() {
        assertFooBarOnly(new ArgumentsSpecs("foo,bar"));
    }

    @Test
    public void parseOnlySpacedNames() {
        assertFooBarOnly(new ArgumentsSpecs("foo bar"));
    }

    @Test
    public void parseNothing() {
        assertEmpty(new ArgumentsSpecs(""));
    }

    private static void assertFooBarOnly(ArgumentsSpecs ms) {
        assertEquals(Arrays.asList("foo", "bar"), ms.getBlueprintNames());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }

    private static void assertEmpty(ArgumentsSpecs ms) {
        assertTrue(ms.getBlueprintNames().toString(), ms.getBlueprintNames().isEmpty());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }
}
