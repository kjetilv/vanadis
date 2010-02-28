package vanadis.extrt;

import vanadis.ext.Expose;

public class VanillaExposer {

    @Expose
    public static TestService getTestService() {
        return new TestService() {
        };
    }

}
