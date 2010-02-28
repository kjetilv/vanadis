package vanadis.extrt;

import vanadis.ext.Expose;

public interface Vanilla {

    @Expose
    TestService getTestService();
}
