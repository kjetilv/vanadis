package vanadis.annopro;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class EarlyBreakTest {

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testNoStackTrace() {
        StackTraceElement[] stackTraceElements = new EarlyBreakException().getStackTrace();
        assertTrue(Arrays.toString(stackTraceElements), stackTraceElements.length == 0);
    }
}
