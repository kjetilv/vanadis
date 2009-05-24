package vanadis.extrt;

import vanadis.objectmanagers.InstanceSummary;
import vanadis.osgi.Reference;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;

class InstanceSummaryImpl implements InstanceSummary {

    private final ServiceProperties<?> properties;

    private final String toString;

    InstanceSummaryImpl(Registration<?> registration) {
        this(properties(registration), serviceString(registration));
    }

    InstanceSummaryImpl(Reference<?> reference) {
        this(properties(reference), serviceString(reference));
    }

    private InstanceSummaryImpl(ServiceProperties<?> properties, String toString) {
        this.properties = properties;
        this.toString = toString;
    }

    private static String serviceString(Registration<?> registration) {
        return String.valueOf(registration.getInstance());
    }

    private static ServiceProperties<?> properties(Registration<?> registration) {
        return registration.getServiceProperties();
    }

    private static ServiceProperties<?> properties(Reference<?> reference) {
        return reference.getServiceProperties();
    }

    private static String serviceString(Reference<?> reference) {
        try {
            Object service = reference.getService();
            return String.valueOf(service);
        } finally {
            reference.unget();
        }
    }

    @Override
    public String getInstanceToString() {
        return toString;
    }

    @Override
    public ServiceProperties<?> getServiceProperties() {
        return properties;
    }
}
