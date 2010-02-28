package vanadis.extrt;

public class InheritedInject implements InjectHeritage {
    private TestService service;

    @Override
    public void setTestService(TestService service) {
        this.service = service;
    }

    public TestService getService() {
        return service;
    }
}
