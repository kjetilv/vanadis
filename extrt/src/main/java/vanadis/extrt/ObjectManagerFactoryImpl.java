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

package vanadis.extrt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.blueprints.ModuleSpecification;
import vanadis.concurrent.OperationQueuer;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySets;
import vanadis.ext.ModuleSystemException;
import vanadis.objectmanagers.ObjectManager;
import vanadis.objectmanagers.ObjectManagerFactory;
import vanadis.objectmanagers.ObjectManagerFactoryMBean;
import vanadis.osgi.Context;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final class ObjectManagerFactoryImpl implements ObjectManagerFactory {

    private static final Logger log = LoggerFactory.getLogger(ObjectManagerFactoryImpl.class);

    private final Context context;

    private final String type;

    private final Class<?> implementationClass;

    private final Map<ModuleSpecification, ObjectManager> managers = Generic.map();

    private final Map<String, ModuleSpecification> autoLaunches;

    private final JmxRegistration<ObjectManagerFactoryMBean> jmxRegistration;

    private final ObjectManagerObserver observer;

    private final OperationQueuer dispatch;

    ObjectManagerFactoryImpl(Context context, Class<?> implementationClass,
                             String type, Collection<ModuleSpecification> autoModules,
                             ObjectManagerObserver observer,
                             OperationQueuer dispatch) {
        this.type = Not.nil(type, "type");
        this.context = Not.nil(context, "context");
        this.autoLaunches = autoServices(autoModules);
        this.implementationClass = Not.nil(implementationClass, "service interface");
        this.observer = observer;
        this.dispatch = dispatch;
        this.jmxRegistration = JmxRegistration.create
                (this.context, ObjectManagerFactoryMBean.class,
                 new ObjectManagerFactoryMBeanImpl(this), implementationClass.getName(),
                 PropertySets.create("type", this.type + "-factory"));
    }

    @Override
    public Collection<ObjectManager> autoLaunch() {
        Set<ObjectManager> objectManagers = Generic.set();
        for (ModuleSpecification moduleSpecification : autoLaunches.values()) {
            objectManagers.add(launch(moduleSpecification));
        }
        return objectManagers;
    }

    @Override
    public Class<?> getModuleClass() {
        return implementationClass;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getContextName() {
        return context.getName();
    }

    @Override
    public Iterator<ModuleSpecification> iterator() {
        return managers.keySet().iterator();
    }

    @Override
    public int getLaunchCount() {
        return managers.size();
    }

    @Override
    public boolean hasLaunched(ModuleSpecification moduleSpecification) {
        return managers.containsKey(moduleSpecification);
    }

    @Override
    public ObjectManager launch(ModuleSpecification specification) {
        failIllegalType(specification);
        if (specification.isGlobalProperties()) {
            PortUtils.writeToSystemProperties(specification.getPropertySet(), context.getLocation());
        }
        ObjectManager objectManager =
                ObjectManagerImpl.create(context, specification, implementationClass , observer, dispatch);
        managers.put(specification, objectManager);
        log.info(this + " received " + specification + ", created " + objectManager);
        return objectManager;
    }

    @Override
    public void close(ModuleSpecification specification) {
        failIllegalType(specification);
        ObjectManager objectManager = managers.remove(specification);
        close(specification, objectManager);
    }

    @Override
    public void shutdown() {
        jmxRegistration.unregister();
        for (ObjectManager objectManager : managers.values()) {
            objectManager.shutdown();
        }
    }

    private void failIllegalType(ModuleSpecification specification) {
        if (!specification.getType().equalsIgnoreCase(getType())) {
            throw new IllegalArgumentException
                    (this + " got mistyped spec " + specification.getType() +
                            ", expected " + getType() + ": " + specification);
        }
    }

    private void close(ModuleSpecification specification, ObjectManager objectManager) {
        if (objectManager == null) {
            log.info(this + " was asked to remove unknown object manager: " + specification);
        } else {
            objectManager.shutdown();
        }
    }

    private static Map<String, ModuleSpecification> autoServices(Collection<ModuleSpecification> autoModules) {
        Map<String, ModuleSpecification> map = Generic.map();
        for (ModuleSpecification autoModule : autoModules) {
            map.put(autoModule.getName(), autoModule);
        }
        return Generic.seal(map);
    }

    @Override
    public String toString() {
        return ToString.of(this, type, "impl", implementationClass.getSimpleName());
    }
}
