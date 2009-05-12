package vanadis.core.collections;

import vanadis.core.lang.TraverseIterable;

public final class CausesIterable extends TraverseIterable<Throwable> {

    public CausesIterable(Throwable start) {
        super(start);
    }

    @Override
    protected Throwable getNext(Throwable current) {
        return current.getCause();
    }
}
