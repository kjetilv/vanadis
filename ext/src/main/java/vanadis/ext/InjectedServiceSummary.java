package net.sf.vanadis.ext;

import net.sf.vanadis.osgi.Filter;

/**
 * Information about an injection points.
 *
 * @see ObjectManager#getExposedServices()
 * @see ObjectManager#getInjectedServices()
 * @see ObjectManager#getConfigureSummaries()
 */
public interface InjectedServiceSummary extends ManagedFeatureSummary {

    /**
     * Either field, method or track.
     *
     * @return Injection type
     */
    InjectionType getInjectionType();

    /**
     * A filter, if any.
     *
     * @return Filter
     */
    Filter getFilter();
}