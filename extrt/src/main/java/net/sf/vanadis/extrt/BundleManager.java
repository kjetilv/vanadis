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

import net.sf.vanadis.blueprints.ModuleSpecification;
import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.ext.*;
import net.sf.vanadis.osgi.Context;
import net.sf.vanadis.osgi.Contexts;
import net.sf.vanadis.osgi.Registration;
import net.sf.vanadis.osgi.ServiceProperties;
import net.sf.vanadis.util.concurrent.OperationQueuer;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.framework.Bundle;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class BundleManager implements Iterable<String> {

    private final Map<Registration<ObjectManagerFactory>, ObjectManagerFactory> factories;

    private final Map<String, ObjectManagerFactory> typedFactories;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Bundle bundle;

    public static BundleManager manage(Bundle bundle,
                                       ObjectManagerObserver observer,
                                       OperationQueuer dispatch) {
        Context context = Contexts.create(bundle.getBundleContext());
        Map<String, ObjectManagerFactory> factories =
                ModulesProcessor.managedFactories(context, bundle, observer, dispatch);
        if (factories == null || factories.isEmpty()) {
            return null;
        }
        return new BundleManager(context, bundle, factories.values());
    }

    private BundleManager(Context context, Bundle bundle, Collection<ObjectManagerFactory> factories) {
        this.bundle = bundle;
        this.typedFactories = typedFactories(factories);
        this.factories = registerFactories(context, factories);
        autoLaunch();
    }

    public boolean isClosed() {
        return closed.get();
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void close() {
        if (isClosed()) {
            return;
        }
        try {
            unregisterFactories();
        } finally {
            closed.set(true);
        }
    }

    public ObjectManager launch(ModuleSpecification moduleSpecification) {
        ObjectManagerFactory objectManagerFactory = objectManagerFactory(moduleSpecification);
        if (objectManagerFactory == null) {
            throw new ModuleSystemException
                    (this + " does not host object manager factory for " + moduleSpecification);
        }
        return objectManagerFactory.launch(moduleSpecification);
    }

    public void disband(ModuleSpecification moduleSpecification) {
        ObjectManagerFactory objectManagerFactory = objectManagerFactory(moduleSpecification);
        if (objectManagerFactory == null) {
            throw new IllegalArgumentException("No object manager factory for " + moduleSpecification);
        }
        objectManagerFactory.close(moduleSpecification);
    }

    @Override
    public Iterator<String> iterator() {
        return typedFactories.keySet().iterator();
    }

    private Collection<ObjectManagerFactory> objectManagerFactories() {
        return factories.values();
    }

    private void autoLaunch() {
        int total = 0;
        for (ObjectManagerFactory factory : objectManagerFactories()) {
            total += factory.autoLaunch().size();
        }
        log.info(this + " registered " + factories.size() + " object manager factories " +
                ", auto-launched " + total + " object managers");
    }

    private ObjectManagerFactory objectManagerFactory(ModuleSpecification moduleSpecification) {
        return typedFactories.get(moduleSpecification.getType());
    }

    private void unregisterFactories() {
        for (Map.Entry<Registration<ObjectManagerFactory>, ObjectManagerFactory> entry : factories.entrySet()) {
            ObjectManagerFactory factory = entry.getValue();
            Registration<?> registration = entry.getKey();
            try {
                factory.shutdown();
            } finally {
                Throwable throwable = registration.unregisterSafely();
                if (throwable != null && log.isDebug()) {
                    log.debug(registration + " failed to unregister properly", throwable);
                }
            }
        }
    }

    private static final Log log = Logs.get(BundleManager.class);

    private static Map<Registration<ObjectManagerFactory>, ObjectManagerFactory> registerFactories(Context context,
                                                                                                   Collection<ObjectManagerFactory> managedFactories) {
        Map<Registration<ObjectManagerFactory>, ObjectManagerFactory> registeredFactories = Generic.map();
        for (ObjectManagerFactory factory : managedFactories) {
            PropertySet propertySet = properties(factory);
            Registration<ObjectManagerFactory> registration = context.register
                    (factory, ServiceProperties.create(ObjectManagerFactory.class, propertySet));
            registeredFactories.put(registration, factory);
        }
        return Generic.seal(registeredFactories);
    }

    private static PropertySet properties(ObjectManagerFactory factory) {
        return CoreProperty.OBJECTMANAGER_TYPE.set(factory.getType());
    }

    private static Map<String, ObjectManagerFactory> typedFactories(Collection<ObjectManagerFactory> factories) {
        Map<String, ObjectManagerFactory> typeFactories = Generic.map();
        for (ObjectManagerFactory factory : factories) {
            ObjectManagerFactory existing = typeFactories.put(factory.getType(), factory);
            if (existing != null) {
                throw new IllegalArgumentException
                        ("Duplicate factory type: " + factory.getType() + ", for modules " +
                                existing.getModuleClass() + " and " + factory.getModuleClass());
            }
        }
        return Generic.seal(typeFactories);
    }

    @Override
    public String toString() {
        return ToString.of(this, typedFactories.keySet());
    }
}
