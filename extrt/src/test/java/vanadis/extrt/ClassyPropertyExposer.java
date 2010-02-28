package vanadis.extrt;

import vanadis.ext.Expose;
import vanadis.ext.Property;

public class ClassyPropertyExposer {

    @Expose(properties = {@Property(name = "navn", value = "ola nordmann")},
            objectClasses = {Cloneable.class})
    public static TestService getTest() {
        return new TestService() {
        };
    }

    @Expose(properties = {@Property(name = "gift", value = "true", propertyType = Boolean.class)})
    public static TestService getBeautifulService() {
        return new TestService() {
        };
    }
}
