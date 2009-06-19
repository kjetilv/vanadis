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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.io.IORuntimeException;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.ext.ModuleSystemException;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.Context;
import vanadis.util.concurrent.OperationQueuer;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

class SystemEventsImpl implements SystemEvents {

    private final OperationQueuer queuer;

    private final Map<Long, BundleManager> bundleManagers = Generic.map();

    private final Map<String, BundleManager> typedBundleManagers = Generic.map();

    private final Map<ModuleSpecification, ServiceStatus> servicesStatus = Generic.map();

    private SystemEvents asynch;

    private final Bundles bundles;

    private final AtomicReference<Thread> shutdownThread = new AtomicReference();

    SystemEventsImpl(BundleContext bundleContext, Context context, OperationQueuer queuer) {
        this.queuer = queuer;
        this.bundles = new Bundles(bundleContext, context);
    }

    Bundles getBundles() {
        return bundles;
    }

    public void setAsynchYou(SystemEvents asynch) {
        Not.nil(asynch, "asynch reference");
        if (this.asynch != null) {
            throw new IllegalStateException
                    (this + " already received asynch pointer to itself");
        }
        this.asynch = asynch;
        this.asynch.spool(bundles);
    }

    @Override
    public void moduleSpecificationAdded(ModuleSpecification moduleSpecification) {
        if (isShuttingDown()) {
            log.info("Ignoring service specification add:" + moduleSpecification +
                    ", shutdown initiated in " + shutdownThread() + "!");
        } else {
            store(moduleSpecification);
            launch(moduleSpecification);
        }
    }


    @Override
    public void moduleSpecificationRemoved(ModuleSpecification moduleSpecification) {
        disbandModuleSpecification(moduleSpecification);
    }

    @Override
    public void bundleSpecificationAdded(BundleSpecification bundleSpecification) {
        if (isShuttingDown()) {
            log.info("Ignoring bundle add:" + bundleSpecification +
                    ", shutdown initiated in " + shutdownThread() + "!");
        } else {
            log.info("Installing " + bundleSpecification);
            try {
                bundles.install(bundleSpecification);
            } catch (Exception e) {
                log.error(this + " failed to install " + bundleSpecification, e);
            }
        }
    }

    @Override
    public void bundleSpecificationRemoved(BundleSpecification bundleSpecification) {
        if (bundles.isHosting(bundleSpecification)) {
            Long bundleId = bundles.getBundleId(bundleSpecification);
            if (isManaged(bundleId)) {
                unmanage(bundleId);
            }
            bundles.uninstall(bundleSpecification);
        }
    }

    @Override
    public void spool(Iterable<Bundle> bundles) {
        for (Bundle bundle : bundles) {
            this.bundles.getBundle(bundle.getBundleId());
            if (bundle.getState() == Bundle.ACTIVE) {
                considerTracking(bundle);
            }
        }
    }

    @Override
    public void activated(Bundle bundle) {
        considerTracking(bundle);
    }

    @Override
    public void deactivated(Bundle bundle) {
        long bundleId = bundle.getBundleId();
        unmanage(bundleId);
        bundles.uninstall(bundleId);
    }

    @Override
    public void updated(ObjectManager objectManager) {
        log.info(this + " notified of state change in " + objectManager);
        if (objectManager.isLaunchable()) {
            objectManager.launch();
        }
    }

    @Override
    public void launchBundles(String[] strings) {
        Iterable<ModuleSpecification> serviceSpecifications = parseModuleSpecifications(strings);
        for (ModuleSpecification moduleSpecification : serviceSpecifications) {
            launch(moduleSpecification);
        }
    }

    @Override
    public void reloadBundles(String[] bundleIdStrings) {
        for (String bundleIdString : bundleIdStrings) {
            long id;
            try {
                id = Long.parseLong(bundleIdString);
            } catch (NumberFormatException e) {
                log.error("Invalid bundle id: " + bundleIdString, e);
                continue;
            }
            List<Set<Long>> bundleIdLayers = Generic.list(bundles.dependentBundles(id));
            Collections.reverse(bundleIdLayers);
            for (Set<Long> bundleIds : bundleIdLayers) {
                for (long bundleId : bundleIds) {
                    unmanage(bundleId);
                }
            }
            for (Set<Long> bundleIds : bundleIdLayers) {
                for (long bundleId : bundleIds) {
                    try {
                        bundles.reload(bundleId);
                    } catch (UnknownBundleException e) {
                        log.warn("Unknown bundle: " + id + " Exception: " + e.getMessage());
                    } catch (Exception e) {
                        log.error("Failed to reload " + id, e);
                    }
                }
            }
        }
    }

    void close() {
        if (setShutdownThread()) {
            closeBundleManagers();
            closeResources();
        } else {
            throw new IllegalStateException
                    (this + " initiated shutdown in " + shutdownThread() +
                            ", completeClose cannot be called in " + Thread.currentThread());
        }
    }

    private void closeResources() {
        bundles.close();
        queuer.synchUp();
        try {
            queuer.close();
        } catch (IOException e) {
            throw new IORuntimeException(this + " failed to close " + queuer, e);
        }
    }

    private void closeBundleManagers() {
        for (BundleManager bundleManager : bundleManagers()) {
            if (!bundleManager.isClosed()) {
                disbandBundleManager(bundleManager);
            }
        }
    }

    private boolean setShutdownThread() {
        return shutdownThread.compareAndSet(null, Thread.currentThread());
    }

    private Thread shutdownThread() {
        return shutdownThread.get();
    }

    private boolean isShuttingDown() {
        return shutdownThread() != null;
    }

    private void considerTracking(Bundle bundle) {
        BundleManager existing = bundleManagers.get(bundle.getBundleId());
        if (existing == null) {
            BundleManager bundleManager = BundleManager.manage(bundle, asynch, queuer);
            if (bundleManager != null) {
                storeBundleManager(bundle.getBundleId(), bundleManager);
                launchUnhostedModuleSpecifications(bundleManager);
                log.info(this + " tracks bundle " + bundle + ": " + bundleManager);
            }
        } else {
            log.info(this + " already tracking " + bundle + ": " + existing);
        }
    }

    private BundleManager bundleManager(ModuleSpecification moduleSpecification) {
        return typedBundleManagers.get(moduleSpecification.getType());
    }

    private void disbandModuleSpecification(ModuleSpecification moduleSpecification) {
        if (moduleSpecification != null) {
            BundleManager bundleManager = bundleManager(moduleSpecification);
            if (bundleManager != null && !bundleManager.isClosed()) {
                disbandModuleSpecification(bundleManager, moduleSpecification);
            }
        }
    }

    private void disbandBundleManager(BundleManager bundleManager) {
        for (String type : bundleManager) {
            for (ServiceStatus serviceStatus : serviceStatii(type)) {
                if (serviceStatus != null && serviceStatus.isHosted()) {
                    disbandModuleSpecification(bundleManager, serviceStatus.getModuleSpecification());
                }
            }
        }
        bundleManager.close();
    }

    private Collection<ServiceStatus> serviceStatii(String type) {
        List<ServiceStatus> statii = Generic.list();
        for (Map.Entry<ModuleSpecification, ServiceStatus> entry : servicesStatus.entrySet()) {
            if (entry.getKey().getType().equals(type)) {
                statii.add(entry.getValue());
            }
        }
        return statii;
    }

    private void disbandModuleSpecification(BundleManager bundleManager, ModuleSpecification moduleSpecification) {
        try {
            bundleManager.disband(moduleSpecification);
        } finally {
            serviceStatus(moduleSpecification).becameUnhosted();
        }
    }

    private void launch(ModuleSpecification moduleSpecification) {
        BundleManager bundleManager = bundleManager(moduleSpecification);
        if (bundleManager == null) {
            log.info(this + " received " + moduleSpecification + ", no object manager factory for that type yet");
        } else if (bundleManager.isClosed()) {
            log.info(this + " received " + moduleSpecification + ", object manager factory closed: " + bundleManager);
        } else {
            launch(bundleManager, moduleSpecification);
        }
    }

    private void launch(BundleManager bundleManager, ModuleSpecification moduleSpecification) {
        try {
            ObjectManager objectManager = bundleManager.launch(moduleSpecification);
            serviceStatus(moduleSpecification).nowHostedBy(bundleManager);
            log.info(this + " launched " + moduleSpecification + " -> " + objectManager);
        } catch (RuntimeException e) {
            throw new ModuleSystemException(this + " failed to launch " + moduleSpecification +
                    " from " + bundleManager, e);
        }
    }

    private Set<BundleManager> bundleManagers() {
        return Generic.set(bundleManagers.values());
    }

    private boolean isManaged(long bundleId) {
        return bundleManagers.containsKey(bundleId);
    }

    private Bundle unmanage(long bundleId) {
        if (isManaged(bundleId)) {
            BundleManager bundleManager = removeBundleManager(bundleId);
            if (bundleManager != null) {
                disbandBundleManager(bundleManager);
                bundleManager.close();
            }
            return bundleManager.getBundle();
        }
        return null;
    }

    private void launchUnhostedModuleSpecifications(BundleManager bundleManager) {
        for (String type : bundleManager) {
            for (ServiceStatus serviceStatus : serviceStatii(type)) {
                if (serviceStatus != null && !serviceStatus.isHosted()) {
                    launch(bundleManager, serviceStatus.getModuleSpecification());
                }
            }
        }
    }

    private void storeBundleManager(long bundleId, BundleManager bundleManager) {
        for (String type : bundleManager) {
            BundleManager existing = typedBundleManagers.get(type);
            if (existing != null) {
                throw new ModuleSystemException
                        (this + " already maps type '" + type + "' to " +
                                existing + ", refusing to manage " + bundleManager);
            }
        }
        BundleManager existing = bundleManagers.get(bundleId);
        if (existing != null) {
            throw new ModuleSystemException
                    (this + " already maps id " + bundleId + " to " +
                            existing + ", refusing to manage " + bundleManager);
        }
        for (String type : bundleManager) {
            typedBundleManagers.put(type, bundleManager);
        }
        bundleManagers.put(bundleId, bundleManager);
    }

    private BundleManager removeBundleManager(long bundleId) {
        BundleManager bundleManager = bundleManagers.remove(bundleId);
        if (bundleManager == null) {
            log.warn(this + " tried to remove unknown bundle manager for bundle " + bundleId);
            return null;
        }
        for (String type : bundleManager) {
            typedBundleManagers.remove(type);
        }
        return bundleManager;
    }

    private void store(ModuleSpecification moduleSpecification) {
        ServiceStatus existing = servicesStatus.get(moduleSpecification);
        if (existing != null && existing.isHosted()) {
            throw new ModuleSystemException
                    (this + " already hosts " + moduleSpecification + ": " + existing);
        }
        if (existing == null) {
            servicesStatus.put(moduleSpecification, new ServiceStatus(moduleSpecification));
        }
    }

    private ServiceStatus serviceStatus(ModuleSpecification moduleSpecification) {
        ServiceStatus existing = servicesStatus.get(moduleSpecification);
        if (existing != null) {
            return existing;
        }
        ServiceStatus serviceStatus = new ServiceStatus(moduleSpecification);
        this.servicesStatus.put(moduleSpecification, serviceStatus);
        return serviceStatus;
    }

    private static Iterable<ModuleSpecification> parseModuleSpecifications(String[] nameTypeArray) {
        List<ModuleSpecification> moduleSpecifications = Generic.list(nameTypeArray.length);
        for (String nameType : nameTypeArray) {
            if (nameType.contains(":")) {
                String[] nameAndType = nameType.split(":");
                moduleSpecifications.add(ModuleSpecification.create(nameAndType[1], nameAndType[0]));
            } else {
                moduleSpecifications.add(ModuleSpecification.create(nameType, nameType));
            }
        }
        return moduleSpecifications;
    }

    private static final Log log = Logs.get(SystemEventsImpl.class);

    @Override
    public String toString() {
        return ToString.of(this, "types", typedBundleManagers.keySet());
    }
}
