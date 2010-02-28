package vanadis.extrt;

import vanadis.core.properties.PropertySet;
import vanadis.ext.Expose;

public class RuntimePropertiesExposer {

    @Expose
    public static TestService getTestService(PropertySet propertySet) {
        propertySet.set("foo", "bar");
        return new TestService() {
        };
    }
}
