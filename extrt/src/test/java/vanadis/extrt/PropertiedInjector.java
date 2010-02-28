package vanadis.extrt;

import vanadis.ext.Inject;
import vanadis.ext.Property;

public class PropertiedInjector {

    public TestService getHrService() {
        return hrService;
    }

    private TestService hrService;

    @Inject(properties = {
            @Property(name = "foo", value = "1", propertyType = Integer.class),
            @Property(name = "bar", value = "true", propertyType = Boolean.class)})
    public void setFoo(TestService hrService) {
        this.hrService = hrService;
    }
}
