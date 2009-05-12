package net.sf.vanadis.ext;

/**
 * Common interface for simple status reports, providing some reflection into
 * an {@link net.sf.vanadis.ext.ObjectManager}.
 *
 * @see ObjectManager#getExposedServices()
 * @see ObjectManager#getInjectedServices()
 * @see ObjectManager#getConfigureSummaries()
 */
public interface ManagedFeatureSummary extends Iterable<InstanceSummary> {

    boolean isActive();

    String getName();

    String getFeatureClass();
}
