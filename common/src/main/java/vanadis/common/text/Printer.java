package vanadis.common.text;

import java.io.PrintStream;

public final class Printer {

    public static final Printer NULL = new Printer(null, -1); // The NULL printer.

    public static final int MAX_INDENT = 16;

    private final PrintStream ps;

    private final int indentSize;

    private final String indentString;

    private int currentIndent;

    private boolean onNewLine = true;

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
        boolean isNull = SPC == null; // This the NULL field - really
        this.ps = isNull ? null : ps;
        this.indentSize = isNull ? -1 : Math.max(1, Math.min(MAX_INDENT, indentSize));
        this.indentString = isNull ? null : SPC[this.indentSize];
    }

    public Printer autoFlush() {
        return autoFlush(true);
    }

    public Printer printStackTrace() {
        return printStackTrace(true);
    }

    public Printer terminateWithNewLine() {
        return terminateWithNewLine(true);
    }

    public Printer singleBlankLine() {
        return singleBlankLine(true);
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

    public Printer singleBlankLine(boolean sbl) {
        if (ps == null) {
            return this;
        }
        this.singleBlankLine = sbl;
        return this;
    }

    public Printer autoFlush(boolean af) {
        if (ps == null) {
            return this;
        }
        this.autoFlush = af;
        return this;
    }

    public Printer printStackTrace(boolean pst) {
        if (ps == null) {
            return this;
        }
        this.printStackTrace = pst;
        return this;
    }

    public Printer terminateWithNewLine(boolean twnl) {
        if (ps == null) {
            return this;
        }
        this.terminateWithNewLine = twnl;
        return this;
    }

    public Printer resetIndent() {
        if (ps == null) {
            return this;
        }
        currentIndent = 0;
        return this;
    }

    public Printer printStackTrace(Throwable throwable) {
        return print(throwable, true);
    }

    public Printer spc(int spaces) {
        return printSpaces(spaces);
    }

    public Printer p(Object object) {
        return print(object, printStackTrace);
    }

    public Printer cr() {
        if (ps == null) {
            return this;
        }
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
        if (ps == null) {
            return this;
        }
        currentIndent += ind;
        return this;
    }

    public Printer outd() {
        return indOut(1);
    }

    public Printer indOut(int out) {
        if (ps == null) {
            return this;
        }
        currentIndent = Math.max(0, currentIndent - out);
        return this;
    }

    public Printer close() {
        if (ps == null) {
            return this;
        }
        if (closed) {
            return this;
        }
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

    public DotProgress dotProgress() {
        return dotProgress(1);
    }

    public DotProgress dotProgress(int interval) {
        return dotProgress(interval, 20);
    }

    public DotProgress dotProgress(int interval, int maxDotColumns) {
        return new DotProgress(this, interval, maxDotColumns);
    }

    private Printer print(Object object, boolean pst) {
        if (ps == null) {
            return this;
        }
        failOnClosed();
        try {
            makeIndents();
            printOut(object, pst);
        } finally {
            noLongerOnNewLine();
        }
        return this;
    }

    private void makeIndents() {
        if (onNewLine) {
            spaceOut();
        }
    }

    private void failOnClosed() {
        if (closed) {
            throw new IllegalStateException(this + " closed");
        }
    }

    private void spaceOut() {
        if (currentIndent > 0) {
            try {
                printSpaces(currentIndent * indentSize);
            } finally {
                noLongerOnNewLine();
            }
        }
    }

    private Printer printSpaces(int spaces) {
        if (ps == null) {
            return this;
        }
        if (spaces < SPC.length) { // Can we shortcut it?
            ps.print(SPC[spaces]);
        } else {
            int indents = spaces / currentIndent;
            for (int i = 0; i < indents; i++) {
                ps.print(indentString);
            }
            int singleSpaces = spaces % currentIndent;
            for (int i = 0; i < singleSpaces; i++) {
                ps.print(SPC[1]);
            }
        }
        return this;
    }

    private String printOut(Object object, boolean pst) {
        String string = toString(object);
        try {
            if (object instanceof Throwable && pst) {
                ((Throwable)object).printStackTrace(ps);
            } else {
                ps.print(string);
            }
        } finally {
            considerFlush();
        }
        return string;
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

    private static String toString(Object object) {
        try {
            return String.valueOf(object);
        } catch (Exception e) {
            return error(object, e);
        }
    }

    private static String error(Object object, Exception e) {
        return "[ERROR: String.valueOf(" + object.getClass() + "@" + System.identityHashCode(object) + ") => " + e + "]";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[lines:" + lines + (closed ? ", closed" : "") + "]";
    }

    // This is not a wtf, just a premature optimization; indent strings stored in the constant pool!
    // Admit it, it's an optimization with class.
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
                                          "                ",
                                          "                 ",
                                          "                  ",
                                          "                   ",
                                          "                    ",
                                          "                     ",
                                          "                      ",
                                          "                       ",
                                          "                        ",
                                          "                         ",
                                          "                          ",
                                          "                           ",
                                          "                            ",
                                          "                             " };
}
