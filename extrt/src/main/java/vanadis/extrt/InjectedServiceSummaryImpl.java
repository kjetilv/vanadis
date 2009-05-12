package vanadis.extrt;

import vanadis.core.lang.ToString;
import vanadis.ext.InjectedServiceSummary;
import vanadis.ext.InjectionType;
import vanadis.osgi.Filter;

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
