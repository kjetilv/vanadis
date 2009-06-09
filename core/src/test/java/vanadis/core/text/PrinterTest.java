package vanadis.core.text;

import junit.framework.Assert;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import vanadis.core.system.VM;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrinterTest {
    private ByteArrayOutputStream baos;

    private PrintStream ps;

    private Printer printer;

    @Test
    public void dotProgress() {
        DotProgress dp = printer.dotProgress();
        dp.tick();
        dp.tick();
        dp.done();
        printer.close();
        assertOutput(".. 2" + VM.LN);
    }

    @Test
    public void dotProgressMore() {
        DotProgress dp = printer.ind().dotProgress(3, 2);
        for (int i = 0; i < 13; i++) {
            dp.tick();
        }
        dp.done();
        printer.close();
        assertOutput("  .. 6" + VM.LN +
                     "  .. 12" + VM.LN +
                     "  . 13" + VM.LN);
    }

    @Test
    public void testImmediateIndent() {
        printer.ind().ind().p("foo").close();
        assertOutput("    foo" + VM.LN);
    }

    @Test
    public void sanityCheck() {
        printer = new Printer(ps, -5);
        printer.p("foo").ind().cr().p("bar").close();
        assertOutput("foo" + VM.LN + " bar" + VM.LN);
    }

    @Test
    public void sanityCheck2() {
        printer = new Printer(ps, 234325);
        printer.p("foo").ind().cr().p("bar").close();
        assertOutput("foo" + VM.LN + "                bar" + VM.LN);

    }

    @Test
    public void twoLines() {
        printer.p("foo").cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "bar" + VM.LN);
    }

    @Test
    public void twoLinesNoNewLine() {
        printer.terminateWithNewLine(false).p("foo").cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "bar");
    }

    @Test
    public void threeLinesIndent() {
        printer.p("foo").ind().cr().
                p("bar").cr().
                p("zot").close();
        assertOutput("foo" + VM.LN +
                     "  bar" + VM.LN +
                     "  zot" + VM.LN);
    }

    @Test
    public void fiveLinesIndentAndBack() {
        printer.p("foo").ind().cr().
                p("bar").ind().cr().
                p("zot").outd().cr().
                p("foo").outd().cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "  bar" + VM.LN +
                     "    zot" + VM.LN +
                     "  foo" + VM.LN+
                     "bar" + VM.LN);
    }

    @Test public void setSingleBlankLine() {
        assertFalse(printer.isSingleBlankLine());
        assertTrue(printer.singleBlankLine(true).isSingleBlankLine());
    }

    @Test public void setPrintStackTraces() {
        assertFalse(printer.isPrintStackTrace());
        assertTrue(printer.printStackTrace(true).isPrintStackTrace());
    }

    @Test public void setTerminateWithNewLine() {
        assertTrue(printer.isTerminateWithNewLine());
        assertFalse(printer.terminateWithNewLine(false).isTerminateWithNewLine());
    }

    @Test
    public void limitNewLines() {
        printer.singleBlankLine(true).p("foo").cr().cr().cr().p("bar").close();
        assertOutput("foo" + VM.LN + VM.LN + "bar" + VM.LN);
    }

    @Test
    public void fiveMultiLinesIndentAndBack() {
        printer.p("foo").ind().cr().
                p("bar").p("123").ind().cr().
                p("zot").p("456").outd().cr().
                p("foo").outd().cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "  bar123" + VM.LN +
                     "    zot456" + VM.LN +
                     "  foo" + VM.LN +
                     "bar" + VM.LN);
    }

    private void assertOutput(String string) {
        Assert.assertEquals(string, string());
    }

    @Test
    public void failClosed() {
        try {
            Assert.fail("Should be closed: " + printer.close().cr());
        } catch (IllegalStateException ignore) { }
    }

    private String string() {
        ps.flush();
        return new String(baos.toByteArray());
    }

    @Before
    public void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        printer = new Printer(ps);
    }
}
