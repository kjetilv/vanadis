package vanadis.core.text;

public class DotProgress {

    private final Printer printer;

    private final int interval;

    private final int maxDotColumns;

    private final String dot;

    private final String error;

    private int i;

    DotProgress(Printer printer, int interval, int maxDotColumns) {
        this(printer, interval, maxDotColumns, ".", "x");
    }

    DotProgress(Printer printer, int interval, int maxDotColumns, String dot, String error) {
        this.printer = printer;
        this.interval = interval;
        this.maxDotColumns = maxDotColumns;
        this.dot = dot;
        this.error = error;
    }

    public void tick() {
        if (i % interval == 0) {
            int steps = i / interval;
            if (steps > 0 && steps % maxDotColumns == 0) {
                status();
            }
            printer.p(dot);
        }
        i++;
    }

    public void oops() {
        printer.p(error);
    }

    public void done() {
        status();
    }

    private void status() {
        printer.p(" ").p(i).cr();
    }
}
