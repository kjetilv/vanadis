package vanadis.extrt;

import vanadis.core.lang.ToString;
import vanadis.ext.ExposedServiceSummary;

class ExposedServiceSummaryImpl extends AbstractManagedFeatureSummary implements ExposedServiceSummary {

    ExposedServiceSummaryImpl(Exposer<?> exposer) {
        super(exposer.getFeatureName(), exposer.getServiceInterface().getName(), regsSummaries(exposer));
    }

    @Override
    public String toString() {
        return ToString.of(this, getName());
    }
}
