package net.sf.vanadis.ext;

import net.sf.vanadis.osgi.ServiceProperties;

public interface InstanceSummary {

    String getInstanceToString();

    ServiceProperties<?> getServiceProperties();
}
