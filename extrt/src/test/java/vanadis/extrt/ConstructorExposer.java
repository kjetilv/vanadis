package vanadis.extrt;

import vanadis.ext.Expose;
import vanadis.ext.Inject;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConstructorExposer {

    public static ConstructorExposer instance;

    private TestService testService;

    public ConstructorExposer(@Inject TestService testService) {
        this.testService = testService;
        instance = this;
    }

    @Expose
    public TestService2 getTestService2() {
        return new TestService2() {};
    }
}
