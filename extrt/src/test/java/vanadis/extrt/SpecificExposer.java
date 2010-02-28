package vanadis.extrt;

import vanadis.ext.Expose;

public class SpecificExposer {

    @Expose(exposedType = TestService.class)
    public static Cloneable getCloneable() {
        return new TestService() {
        };
    }

}
