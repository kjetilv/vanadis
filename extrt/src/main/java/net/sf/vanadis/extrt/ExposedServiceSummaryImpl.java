package net.sf.vanadis.extrt;

import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.ExposedServiceSummary;

class ExposedServiceSummaryImpl extends AbstractManagedFeatureSummary implements ExposedServiceSummary {

    ExposedServiceSummaryImpl(Exposer<?> exposer) {
        super(exposer.getFeatureName(), exposer.getServiceInterface().getName(), regsSummaries(exposer));
    }

    @Override
    public String toString() {
        return ToString.of(this, getName());
    }
}
