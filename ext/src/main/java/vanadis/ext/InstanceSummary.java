package vanadis.ext;

import vanadis.osgi.ServiceProperties;

public interface InstanceSummary {

    String getInstanceToString();

    ServiceProperties<?> getServiceProperties();
}
