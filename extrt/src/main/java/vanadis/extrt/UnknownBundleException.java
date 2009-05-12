package net.sf.vanadis.extrt;

public class UnknownBundleException extends RuntimeException {

    public UnknownBundleException(String message) {
        super(message);
    }

    public UnknownBundleException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private static final long serialVersionUID = 7756235579522312376L;
}
