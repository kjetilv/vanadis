package vanadis.util.concurrent;

import java.util.concurrent.Callable;

class ThreadGetter implements Callable<Thread> {

    @Override
    public Thread call() {
        return Thread.currentThread();
    }
}
