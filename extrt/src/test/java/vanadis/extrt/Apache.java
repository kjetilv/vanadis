package vanadis.extrt;

import vanadis.core.collections.Generic;
import vanadis.ext.Track;

import java.util.Collection;
import java.util.Set;

public class Apache {

    private final Set<TestService> testServices = Generic.set();

    @Track(trackedType = TestService.class)
    public Collection<TestService> testServices() {
        return testServices;
    }
}
