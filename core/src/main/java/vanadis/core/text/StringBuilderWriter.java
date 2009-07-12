package vanadis.core.text;

public class StringBuilderWriter extends AbstractModernWriter {

    private final StringBuilder sb;

    public StringBuilderWriter(StringBuilder sb) {
        this.sb = sb;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        sb.append(cbuf, off, len);
    }
}
