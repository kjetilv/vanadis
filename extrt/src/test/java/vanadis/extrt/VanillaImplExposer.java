package vanadis.extrt;

public class VanillaImplExposer implements Vanilla {

    @Override
    public TestService getTestService() {
        return new TestService() {
        };
    }

}
