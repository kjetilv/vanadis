package vanadis.extrt;

import vanadis.ext.Inject;
import vanadis.osgi.Reference;
import vanadis.osgi.ServiceProperties;

public class ReferenceInjectee {

    private Reference<TestService> propertiedTestServiceRef;

    private TestService testService;

    private ServiceProperties<TestService> properties;

    private ServiceProperties<TestService> refProperties;

    public ServiceProperties<TestService> getRefProperties() {
        return refProperties;
    }

    public Reference<TestService> getTestServiceRef() {
        return testServiceRef;
    }

    private Reference<TestService> testServiceRef;

    @Inject(injectType = TestService.class)
    public void setPropertiedTestServiceRef(Reference<TestService> registration) {
        this.testServiceRef = registration;
    }

    @Inject(injectType = TestService.class)
    public void setTestServiceRef(Reference<TestService> registration,
                                  ServiceProperties<TestService> properties) {
        this.propertiedTestServiceRef = registration;
        this.refProperties = properties;
    }

    public Reference<TestService> getPropertiedTestServiceRef() {
        return propertiedTestServiceRef;
    }

    @Inject
    public void setTestService(TestService testService,
                               ServiceProperties<TestService> properties) {
        this.testService = testService;
        this.properties = properties;
    }

    public TestService getTestService() {
        return testService;
    }

    public ServiceProperties<TestService> getPropertySet() {
        return properties;
    }
}
