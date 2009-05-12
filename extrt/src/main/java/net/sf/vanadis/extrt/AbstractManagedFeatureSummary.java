package net.sf.vanadis.extrt;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.ext.InstanceSummary;
import net.sf.vanadis.ext.ManagedFeatureSummary;
import net.sf.vanadis.osgi.Reference;
import net.sf.vanadis.osgi.Registration;

import java.util.Collection;
import java.util.Iterator;

abstract class AbstractManagedFeatureSummary implements ManagedFeatureSummary {

    private final String name;

    private final String featureClass;

    private final Collection<InstanceSummary> summaries;

    protected static Collection<InstanceSummary> refsSummaries(Injector<?> injector) {
        Collection<InstanceSummary> instanceSummaries = Generic.list();
        for (Reference<?> registration : injector.getReferences()) {
            instanceSummaries.add(new InstanceSummaryImpl(registration));
        }
        return instanceSummaries;
    }

    protected static Collection<InstanceSummary> regsSummaries(Exposer<?> regs) {
        Collection<InstanceSummary> instanceSummaries = Generic.list();
        for (Registration<?> registration : regs) {
            instanceSummaries.add(new InstanceSummaryImpl(registration));
        }
        return instanceSummaries;
    }

    protected AbstractManagedFeatureSummary(String name, String featureClass, Collection<InstanceSummary> summaries) {
        this.name = name;
        this.featureClass = featureClass;
        this.summaries = summaries;
    }

    protected int summariesCount() {
        return summaries.size();
    }

    @Override
    public boolean isActive() {
        return !summaries.isEmpty();
    }

    @Override
    public String getFeatureClass() {
        return featureClass;
    }

    @Override
    public Iterator<InstanceSummary> iterator() {
        return summaries.iterator();
    }

    @Override
    public String getName() {
        return name;
    }
}
