package vanadis.objectmanagers;

import vanadis.osgi.ServiceProperties;

public interface InstanceSummary {

    String getInstanceToString();

    ServiceProperties<?> getServiceProperties();
}
