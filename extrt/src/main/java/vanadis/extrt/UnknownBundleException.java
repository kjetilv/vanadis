package vanadis.extrt;

class UnknownBundleException extends RuntimeException {

    UnknownBundleException(String message) {
        super(message);
    }

    UnknownBundleException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private static final long serialVersionUID = 7756235579522312376L;
}
