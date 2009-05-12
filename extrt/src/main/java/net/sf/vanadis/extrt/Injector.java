/*
 * Copyright 2009 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.vanadis.extrt;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.Strings;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import net.sf.vanadis.core.reflection.Retyper;
import net.sf.vanadis.ext.CoreProperty;
import net.sf.vanadis.ext.ObjectManagerInjectPointMBean;
import net.sf.vanadis.ext.Property;
import net.sf.vanadis.ext.RemoteInjectPoint;
import net.sf.vanadis.osgi.*;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;

import java.util.Map;
import java.util.Set;

abstract class Injector<T> extends ManagedFeature<T, ObjectManagerInjectPointMBean> {

    private final boolean remotable;

    private final Filter annotatedFilter;

    private final int minimum;

    private final boolean injectUpdates;

    private final InjectionListener listener;

    private final boolean multi;

    private final boolean retained;

    private final Map<Reference<T>, T> references = Generic.map();

    private int injectCount;

    private Registration<RemoteInjectPoint> remoteInjectPointRegistration;

    private final boolean passReference;

    private Filter filter;

    private boolean discontinued;

    Injector(FeatureAnchor<T> featureAnchor, Property[] annotatedProperties,
             boolean remotable,
             int minimum,
             boolean multi,
             boolean retained,
             boolean injectUpdates,
             boolean passReference,
             InjectionListener listener) {
        super(featureAnchor, ObjectManagerInjectPointMBean.class);
        this.annotatedFilter = filter(annotatedProperties, featureAnchor.getPropertySet());
        this.remotable = remotable;
        this.minimum = minimum;
        this.multi = multi;
        this.retained = retained;
        this.injectUpdates = injectUpdates;
        this.listener = listener;
        this.passReference = passReference;
    }

    @Override
    protected ObjectManagerInjectPointMBean mbean() {
        return new ObjectManagerInjectPointMBeanImpl(this);
    }

    final Filter getFilter() {
        return filter;
    }

    @Override
    final boolean isComplete() {
        return injectCount >= getMinimum();
    }

    final Set<Reference<T>> getReferences() {
        return references.keySet();
    }

    @Override
    final void activate() {
        if (isRemotable()) {
            registerRemoteInjectPoint(getContext());
        }
        doActivate();
    }

    @Override
    protected final void performDeactivate() {
        if (remoteInjectPointRegistration != null) {
            remoteInjectPointRegistration.unregister();
        }
        for (Map.Entry<Reference<T>, T> entry : Generic.list(references.entrySet())) {
            uninject(entry.getKey(), entry.getValue());
        }
        doDeactivate();
    }

    protected final Map.Entry<Reference<T>, T> getReplacement() {
        return references.entrySet().iterator().next();
    }

    protected final boolean containsReferences() {
        return !references.isEmpty();
    }

    protected final boolean isRemotable() {
        return remotable;
    }

    protected final Filter getAnnotatedFilter() {
        return annotatedFilter;
    }

    protected final int getMinimum() {
        return minimum;
    }

    @Override
    protected final boolean isMulti() {
        return multi;
    }

    protected final Object getManaged() {
        return getObjectManager().getManagedObject();
    }

    protected final boolean isPassReference() {
        return passReference;
    }

    protected final Filter filter() {
        if (filter == null) {
            filter = isRemotable() ? getAnnotatedFilter().and(REMOTABLE_FILTER) : getAnnotatedFilter();
        }
        return filter;
    }

    protected final void inject(Reference<T> reference, T providedService, boolean update) {
        Not.nil(reference, "reference");
        if (discontinued) {
            log.debug(this + " discontinued, skipping inject of " + providedService);
            return;
        }
        if (update && !injectUpdates) {
            return;
        }
        T service = providedService != null ? providedService : reference.getService();
        if (service == null) {
            log.warn(this + " was asked to inject null-valued service for reference " + reference);
            return;
        }
        try {
            performInject(reference, service);
            log.info(this + " injected " + service);
        } finally {
            if (retained) {
                references.put(reference, service);
            }
            injectCount++;
            fireInjected();
        }
    }

    protected final void uninject(Reference<T> reference, T providedService) {
        Not.nil(reference, "reference");
        if (discontinued) {
            log.debug(this + " discontinued, skipping uninject of " + providedService);
            return;
        }
        T service = resolveService(reference, providedService);
        if (retained) {
            references.remove(reference);
        }
        if (service == null) {
            log.warn(this + " could not uninject reference " + reference + ", no service could be resolved");
            return;
        }
        try {
            performUninject(reference, service);
        } catch (RuntimeException e) {
            processInvocationError(e);
        } finally {
            reference.unget();
            injectCount--;
            fireRetracted();
        }
    }

    private void processInvocationError(RuntimeException e) {
        if (shutdownRaceIssues(e)) {
            if (log.isDebug()) {
                log.debug(this + " failed to uninject, assuming shutdown races", e);
            }
        } else {
            throw e;
        }
    }

    private T resolveService(Reference<T> reference, T providedService) {
        T retainedService = references.get(reference);
        if (retained && retainedService == null) {
            log.warn(this + " did not have retained reference " + reference +
                    ", provided service was: " + providedService);
        }
        if (retainedService != null && providedService == null) {
            return retainedService;
        } else if (providedService != null && retainedService == null) {
            return providedService;
        }
        if (retainedService != providedService) {
            throw new IllegalArgumentException
                    (this + " contained " + retainedService + ", was asked to unregister " + providedService);
        }
        return providedService;
    }

    protected abstract void doDeactivate();

    protected abstract void doActivate();

    protected abstract void performInject(Reference<T> reference, T service);

    protected abstract void performUninject(Reference<T> reference, T service);

    private void fireInjected() {
        try {
            listener.wasInjected(this);
        } catch (Throwable e) {
            log.warn(this + " could not notify listener " + listener + " about injection", e);
        }
    }

    private void fireRetracted() {
        try {
            listener.wasRetracted(this);
        } catch (Throwable e) {
            log.warn(this + " could not notify listener " + listener + " about retraction", e);
        }
    }

    private boolean shutdownRaceIssues(RuntimeException e) {
        // Can't use CausesIterable here - it may not be accessible anymore!
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                discontinued = true;
                return true;
            }
        }
        return false;
    }

    private void registerRemoteInjectPoint(Context context) {
        ServiceProperties<RemoteInjectPoint> serviceProperties =
                ServiceProperties.create(RemoteInjectPoint.class);
        RemoteInjectPoint service =
                new RemoteInjectPoint(getServiceInterface(),
                                      getObjectManagerName(),
                                      filter());
        remoteInjectPointRegistration = context.register(service, serviceProperties);
    }

    private static final Log log = Logs.get(Injector.class);

    private static final String[] NO_STRINGS = new String[]{};

    private static final Filter REMOTABLE_FILTER = CoreProperty.REMOTABLE.filter(true);

    protected static Filter filter(Property[] properties, PropertySet configuredProperties) {
        Filter filter = Filters.NULL;
        for (Property property : properties) {
            String[] value = values(property.value(), property.values());
            String name = property.name();
            Class<?> propertyType = property.propertyType();
            Filter append = value.length == 0 ? Filters.present(name)
                    : Filters.eq(name, Retyper.coerceArray(propertyType, value));
            filter = filter.and(property.negate() ? append.not() : append);
        }
        for (String property : configuredProperties) {
            String value = configuredProperties.getString(property, PropertySets.systemProperties());
            Filter append = Strings.isEmpty(value) ? Filters.present(value)
                    : Filters.eq(property, value);
            filter = filter.and(append);
        }
        return filter;
    }

    private static String[] values(String singleValue, String[] multiValues) {
        if (multiValues.length == 0) {
            String value = singleValue.trim();
            return value.length() == 0 ? NO_STRINGS
                    : new String[]{value};
        }
        return multiValues;
    }

    @Override
    public long[] getServiceIds() {
        Map<Reference<T>, T> map = Generic.map(references);
        long[] ids = new long[references.size()];
        int i = 0;
        for (Reference<?> reference : map.keySet()) {
            ids[i++] = reference.getServiceId();
        }
        return ids;
    }

}
