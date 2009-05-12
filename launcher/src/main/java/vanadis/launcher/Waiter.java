package vanadis.launcher;

import java.io.PrintStream;
import java.util.concurrent.Callable;

abstract class Waiter implements Callable<Boolean> {

    private final int target;

    private final int impatience;

    private final PrintStream stream;

    private int lastCount = Integer.MAX_VALUE;

    private int unchanged;

    protected Waiter(int target, int impatience, PrintStream stream) {
        this.target = target;
        this.impatience = impatience;
        this.stream = stream;
    }

    @Override
    public final Boolean call() {
        int count;
        try {
            count = getCount();
        } catch (IllegalStateException ignore) {
            return Boolean.TRUE;
        }
        if (count == target) {
            return Boolean.TRUE;
        }
        if (count == lastCount) {
            unchanged++;
        } else {
            lastCount = count;
        }
        if (stream != null) {
            stream.print(".");
        }
        return unchanged > impatience ? Boolean.FALSE : null;
    }

    protected abstract int getCount();
}
