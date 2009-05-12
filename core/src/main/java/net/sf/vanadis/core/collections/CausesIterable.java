package net.sf.vanadis.core.collections;

import net.sf.vanadis.core.lang.TraverseIterable;

public final class CausesIterable extends TraverseIterable<Throwable> {

    public CausesIterable(Throwable start) {
        super(start);
    }

    @Override
    protected Throwable getNext(Throwable current) {
        return current.getCause();
    }
}
