package vanadis.common.text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.PrintStream;

public class NullPrinterTest extends AbstractPrinterTest {

    @Test
    public void setSingleBlankLine() {
        assertFalse(getPrinter().isSingleBlankLine());
        assertFalse(getPrinter().singleBlankLine(true).isSingleBlankLine());
    }

    @Test public void setPrintStackTraces() {
        assertFalse(getPrinter().isPrintStackTrace());
        assertFalse(getPrinter().printStackTrace(true).isPrintStackTrace());
    }

    @Test public void setTerminateWithNewLine() {
        assertTrue(getPrinter().isTerminateWithNewLine());
        assertTrue(getPrinter().terminateWithNewLine(false).isTerminateWithNewLine());
    }

    @Override
    protected Printer printer(PrintStream ps, int x) {
        return Printer.NULL;
    }

    @Override
    protected Printer vanillaPrinter(PrintStream ps) {
        return Printer.NULL;
    }
}
