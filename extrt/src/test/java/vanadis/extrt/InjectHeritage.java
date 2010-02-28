package vanadis.extrt;

import vanadis.ext.Inject;

public interface InjectHeritage {

    @Inject
    void setTestService(TestService service);
}
