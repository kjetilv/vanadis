package vanadis.core.jmx;

public class JmxException extends RuntimeException {

    public JmxException(String message) {
        super(message);
    }

    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -7634021168508883181L;
}
