package net.sf.vanadis.util.concurrent;

import java.util.concurrent.ThreadFactory;

class SingleThreadFactory implements ThreadFactory {

    private boolean called;

    private final String name;

    SingleThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        if (called) {
            throw new IllegalStateException(this + " returns only a single thread!");
        }
        try {
            return new Thread(r, name);
        } finally {
            called = true;
        }
    }
}
