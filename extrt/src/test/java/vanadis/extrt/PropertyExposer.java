package vanadis.extrt;

import vanadis.ext.Expose;
import vanadis.ext.Property;

public class PropertyExposer {

    @Expose(properties = {@Property(name = "navn", value = "ola nordmann")})
    public static TestService getTest() {
        return new TestService() {
        };
    }
}
