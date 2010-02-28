package vanadis.extrt;

import vanadis.ext.Inject;

public class ObjectInjectee implements GetObject<Object> {

    private Object object;

    @Override
    public Object getObject() {
        return object;
    }

    @Inject
    public void setObject(Object object) {
        this.object = object;
    }
}
