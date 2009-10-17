package vanadis.main;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;

import vanadis.launcher.ArgumentsSpecs;

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
        assertEquals(Arrays.asList("foo", "bar"), ms.getAdditionalBlueprintNames());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }

    private static void assertEmpty(ArgumentsSpecs ms) {
        assertTrue(ms.getAdditionalBlueprintNames().toString(), ms.getAdditionalBlueprintNames().isEmpty());
        assertNull(ms.getHome());
        assertNull(ms.getRepoRoot());
        assertNull(ms.getLocation());
        assertTrue(ms.getBlueprintPaths().isEmpty());
        assertTrue(ms.getBlueprintResources().isEmpty());
    }
}
