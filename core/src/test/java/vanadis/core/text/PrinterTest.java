package vanadis.core.text;

import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.PrintStream;

public class PrinterTest extends AbstractPrinterTest {

    @Test
    public void failClosed() {
        try {
            Assert.fail("Should be closed: " + getPrinter().close().cr());
        } catch (IllegalStateException ignore) { }
    }

    @Test
    public void setSingleBlankLine() {
        assertFalse(getPrinter().isSingleBlankLine());
        assertTrue(getPrinter().singleBlankLine(true).isSingleBlankLine());
    }

    @Test public void setPrintStackTraces() {
        assertFalse(getPrinter().isPrintStackTrace());
        assertTrue(getPrinter().printStackTrace(true).isPrintStackTrace());
    }

    @Test public void setTerminateWithNewLine() {
        assertTrue(getPrinter().isTerminateWithNewLine());
        assertFalse(getPrinter().terminateWithNewLine(false).isTerminateWithNewLine());
    }

    @Override
    protected Printer vanillaPrinter(PrintStream ps) {
        return new Printer(ps);
    }

    @Override
    protected Printer printer(PrintStream ps, int x) {
        return new Printer(ps, x);
    }
}
