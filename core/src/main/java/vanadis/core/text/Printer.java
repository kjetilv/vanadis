package vanadis.core.text;

import vanadis.core.lang.ToString;

import java.io.PrintStream;

public class Printer {

    private final PrintStream ps;

    private final String indentString;

    private int currentIndent;

    private boolean onNewLine;

    private boolean closed;

    private int lines;

    private int blankLines;

    private boolean terminateWithNewLine = true;

    private boolean singleBlankLine;

    private boolean printStackTrace;

    private boolean autoFlush;

    public Printer(PrintStream ps) {
        this(ps, 2);
    }

    public Printer(PrintStream ps, int indentSize) {
        this.ps = ps;
        this.indentString = SPC[Math.min(16, Math.max(1, indentSize))];
    }

    public Printer autoFlush() {
        return autoFlush(true);
    }

    public Printer autoFlush(boolean af) {
        this.autoFlush = af;
        return this;
    }

    public Printer printStackTrace() {
        return printStackTrace(true);
    }

    public Printer printStackTrace(boolean pst) {
        this.printStackTrace = pst;
        return this;
    }

    public Printer terminateWithNewLine() {
        return terminateWithNewLine(true);
    }

    public Printer terminateWithNewLine(boolean twnl) {
        this.terminateWithNewLine = twnl;
        return this;
    }

    public Printer singleBlankLine() {
        return singleBlankLine(true);
    }

    public Printer singleBlankLine(boolean sbl) {
        this.singleBlankLine = sbl;
        return this;
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public boolean isTerminateWithNewLine() {
        return terminateWithNewLine;
    }

    public boolean isSingleBlankLine() {
        return singleBlankLine;
    }

    public boolean isPrintStackTrace() {
        return printStackTrace;
    }

    public Printer printStackTrace(Throwable throwable) {
        return print(throwable, true);
    }

    public Printer p(Object object) {
        return print(object, printStackTrace);
    }

    private Printer print(Object object, boolean pst) {
        failOnClosed();
        try {
            makeIndents();
            printOut(object, pst);
        } finally {
            noLongerOnNewLine();
        }
        return this;
    }

    public Printer cr() {
        failOnClosed();
        if (!(onNewLine && singleBlankLine && blankLines > 0)) {
            newLine();
        }
        return this;
    }

    public Printer ind() {
        return ind(1);
    }

    public Printer ind(int ind) {
        currentIndent += ind;
        return this;
    }

    public Printer outd() {
        return indOut(1);
    }

    public Printer indOut(int out) {
        currentIndent = Math.max(0, currentIndent - out);
        return this;
    }

    public Printer close() {
        try {
            ps.flush();
            if (terminateWithNewLine && !onNewLine) {
                newLine();
            }
            return this;
        } finally {
            closed = true;
        }
    }

    private void makeIndents() {
        if (onNewLine) {
            spaceOut(currentIndent);
        }
    }

    private void failOnClosed() {
        if (closed) {
            throw new IllegalStateException(this + " closed");
        }
    }

    private void spaceOut(int ind) {
        if (ind > 0) {
            try {
                for (int i = 0; i < ind; i++) {
                    ps.print(indentString);
                }
            } finally {
                noLongerOnNewLine();
            }
        }
    }

    private String printOut(Object object, boolean pst) {
        try {
            if (object instanceof Throwable && pst) {
                ((Throwable)object).printStackTrace(ps);
                return object.toString();
            } else {
                String string = String.valueOf(object);
                ps.print(string);
                return string;
            }
        } finally {
            considerFlush();
        }
    }

    private void noLongerOnNewLine() {
        onNewLine = false;
        blankLines = 0;
    }

    private void newLine() {
        try {
            try {
                ps.println();
            } finally {
                onNewLine();
            }
        } finally {
            considerFlush();
        }
    }

    private void considerFlush() {
        if (autoFlush) {
            ps.flush();
        }
    }

    private void onNewLine() {
        lines++;
        if (onNewLine) {
            blankLines++;
        } else {
            onNewLine = true;
        }
    }

    private static final String[] SPC = { null,
                                          " ",
                                          "  ",
                                          "   ",
                                          "    ",
                                          "     ",
                                          "      ",
                                          "       ",
                                          "        ",
                                          "         ",
                                          "          ",
                                          "           ",
                                          "            ",
                                          "             ",
                                          "              ",
                                          "               ",
                                          "                " }; // This is not a wtf - indent strings actually
    // thrive in the constant pool.

    @Override
    public String toString() {
        return ToString.of(this, "lines", lines,
                           "closed", closed);
    }

}
