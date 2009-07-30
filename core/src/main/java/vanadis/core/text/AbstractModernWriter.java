package vanadis.core.text;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractModernWriter extends Writer {
    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }
}
