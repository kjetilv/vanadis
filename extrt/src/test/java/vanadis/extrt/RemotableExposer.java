package vanadis.extrt;

import vanadis.ext.Expose;

public class RemotableExposer {

    private TestService lastExposed;

    public TestService getLastExposed() {
        return lastExposed;
    }

    @Expose(remotable = true)
    public TestService getTest() {
        lastExposed = new TestService() {
        };
        return lastExposed;
    }
}
