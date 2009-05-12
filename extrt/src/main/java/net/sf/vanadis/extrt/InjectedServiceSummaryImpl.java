package net.sf.vanadis.extrt;

import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.InjectedServiceSummary;
import net.sf.vanadis.ext.InjectionType;
import net.sf.vanadis.osgi.Filter;

class InjectedServiceSummaryImpl extends AbstractManagedFeatureSummary implements InjectedServiceSummary {

    private final Filter filter;

    private final InjectionType injectionType;

    InjectedServiceSummaryImpl(Injector<?> injector) {
        super(injector.getFeatureName(), injector.getServiceInterface().getName(), refsSummaries(injector));
        this.injectionType = injector instanceof TrackingInjector ? InjectionType.TRACK
                : injector instanceof FieldInjector ? InjectionType.FIELD
                        : InjectionType.METHOD;
        this.filter = injector.getFilter();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public InjectionType getInjectionType() {
        return injectionType;
    }

    @Override
    public String toString() {
        return ToString.of(this, getName(), "type", injectionType, "filter", filter, "instances", summariesCount());
    }
}
