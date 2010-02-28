package vanadis.extrt;

import vanadis.ext.Expose;

public class EasyGoingExposer {

    @Expose(optional = true)
    public static TestService getOptionally() {
        return new TestService() {
        };
    }

    @Expose
    public static TestService getRequiredly() {
        return new TestService() {
        };
    }
}
