package vanadis.extrt;

import vanadis.core.collections.Generic;
import vanadis.ext.Track;
import vanadis.osgi.Reference;

import java.util.Set;

public class ApacheReference {

    @Track(trackedType = TestService.class, trackReferences = true)
    final Set<Reference<TestService>> testServices = Generic.set();
}
