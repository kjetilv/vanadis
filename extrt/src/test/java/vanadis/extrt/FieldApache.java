package vanadis.extrt;

import vanadis.core.collections.Generic;
import vanadis.ext.Track;

import java.util.Set;

public class FieldApache {

    @Track(trackedType = TestService.class)
    final Set<TestService> testServices = Generic.set();
}
