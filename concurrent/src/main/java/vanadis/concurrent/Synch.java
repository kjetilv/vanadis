package vanadis.concurrent;

import java.util.concurrent.Callable;

class Synch implements Callable<Object> {

    @Override
    public Object call() {
        return this;
    }
}
