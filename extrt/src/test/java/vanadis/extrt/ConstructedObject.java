package vanadis.extrt;

import vanadis.ext.Expose;

import static junit.framework.Assert.assertNotNull;

public class ConstructedObject {

    public ConstructedObject(TestService testService) {
        assertNotNull(testService);
    }

    @Expose
    public TestService2 getMe() {
        return new TestService2() {};
    }
}
