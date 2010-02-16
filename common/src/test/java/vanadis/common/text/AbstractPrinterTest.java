package vanadis.common.text;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import vanadis.core.system.VM;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public abstract class AbstractPrinterTest {

    private ByteArrayOutputStream baos;

    private PrintStream ps;

    private Printer printer;

    @Test
    public void dotProgress() {
        DotProgress dp = getPrinter().dotProgress();
        dp.tick();
        dp.tick();
        dp.done();
        getPrinter().close();
        assertOutput(".. 2" + VM.LN);
    }

    @Test
    public void dotProgressMore() {
        DotProgress dp = getPrinter().ind().dotProgress(4, 3);
        for (int i = 0; i < 27; i++) {
            dp.tick();
        }
        dp.done();
        getPrinter().close();
        assertOutput("  ... 12" + VM.LN +
                     "  ... 24" + VM.LN +
                     "  . 27" + VM.LN);
    }

    @Test
    public void testImmediateIndent() {
        getPrinter().ind().ind().p("foo").close();
        assertOutput("    foo" + VM.LN);
    }

    @Test
    public void sanityCheck() {
        printer = printer(ps, -5);
        getPrinter().p("foo").ind().cr().p("bar").close();
        assertOutput("foo" + VM.LN + " bar" + VM.LN);
    }

    @Test
    public void sanityCheck2() {
        printer = printer(ps, 234325);
        getPrinter().p("foo").ind().cr().p("bar").close();
        assertOutput("foo" + VM.LN + "                bar" + VM.LN);

    }

    @Test
    public void twoLines() {
        getPrinter().p("foo").cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "bar" + VM.LN);
    }

    @Test
    public void twoLinesNoNewLine() {
        getPrinter().terminateWithNewLine(false).p("foo").cr().
                p("bar").close();
        assertOutput("foo" + VM.LN +
                     "bar");
    }

    @Test
    public void threeLinesIndent() {
        getPrinter().p("foo").ind().cr().
                p("bar").cr().
                p("zot").close();
        assertOutput("foo" + VM.LN +
                     "  bar" + VM.LN +
                     "  zot" + VM.LN);
    }

    @Test
    public void fiveLinesIndentAndBack() {
        getPrinter().p("foo").ind().cr().
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

    @Test
    public void limitNewLines() {
        getPrinter().singleBlankLine(true).p("foo").cr().cr().cr().p("bar").close();
        assertOutput("foo" + VM.LN + VM.LN + "bar" + VM.LN);
    }

    @Test
    public void fiveMultiLinesIndentAndBack() {
        getPrinter().p("foo").ind().cr().
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
        if (getClass() == NullPrinterTest.class) {
            assertEquals("", string());
        } else {
            assertEquals(string, string());
        }
    }

    @Test
    public void printSpaces() {
        getPrinter().p("foo").spc(3).p("bar").close();
        assertOutput("foo   bar" + VM.LN);
    }

    private String string() {
        getPrinter().close();
        return new String(baos.toByteArray());
    }

    @Before
    public void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        printer = vanillaPrinter(ps);
    }

    protected abstract Printer vanillaPrinter(PrintStream ps);

    protected abstract Printer printer(PrintStream ps, int x);

    protected Printer getPrinter() {
        return printer;
    }
}
