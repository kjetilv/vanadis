package vanadis.extrt;

import vanadis.ext.Inject;

public class RemotableInjectee {

    public Object getLastAdded() {
        return lastAdded;
    }

    private Object lastAdded;

    @SuppressWarnings({"UnusedDeclaration"})
    @Inject(remotable = true)
    public void addX(Object x) {
        lastAdded = x;
    }
}
