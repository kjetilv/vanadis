package vanadis.extrt;

import vanadis.ext.Inject;

public class FieldObjectInjectee implements GetObject<Object> {

    @SuppressWarnings({"UnusedDeclaration"})
    @Inject
    private Object object;

    @Override
    public Object getObject() {
        return object;
    }
}
