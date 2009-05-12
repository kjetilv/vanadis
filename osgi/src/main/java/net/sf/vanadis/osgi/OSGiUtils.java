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
package net.sf.vanadis.osgi;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.collections.Member;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class OSGiUtils {

    private static final Log log = Logs.get(OSGiUtils.class);

    public static boolean isStale(Bundle bundle) {
        return state(bundle) == null;
    }

    public static boolean isActive(Bundle bundle) {
        Integer state = state(bundle);
        return state != null && Member.of
                (state, Bundle.ACTIVE, Bundle.STARTING, Bundle.STOPPING);
    }

    public static boolean bundleNoLongerValid(IllegalStateException e) {
        return e.getMessage().contains("no longer valid");
    }

    public static List<Long> closeableBundles(BundleContext bundleContext, Long... ids) {
        ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
        try {
            return closeableBundles((PackageAdmin) bundleContext.getService(ref), bundleContext, ids);
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    public static List<Long> closeableBundles(PackageAdmin packageAdmin, BundleContext bundleContext, Long... ids) {
        List<Set<Long>> dependencies = mutableDependentBundles(packageAdmin, bundleContext, ids);
        Collections.reverse(dependencies);
        List<Long> closeableIds = Generic.list();
        for (Set<Long> layer : dependencies) {
            closeableIds.addAll(layer);
        }
        return closeableIds;
    }

    public static List<Set<Long>> dependentBundles(BundleContext bundleContext, Long... ids) {
        ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
        try {
            return dependentBundles((PackageAdmin) bundleContext.getService(ref), bundleContext, ids);
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    public static List<Set<Long>> dependentBundles(PackageAdmin packageAdmin, BundleContext bundleContext, Long... ids) {
        List<Set<Long>> idSets = mutableDependentBundles(packageAdmin, bundleContext, ids);
        return idSets == null || idSets.isEmpty() ? Collections.<Set<Long>>emptyList() : Generic.seal(idSets);
    }

    private static List<Set<Long>> mutableDependentBundles(PackageAdmin packageAdmin, BundleContext bundleContext, Long... ids) {
        LinkedList<Set<Long>> layers = Generic.linkedList();
        layers.add(Generic.set(ids));
        buildLayers(packageAdmin, bundleContext, layers);
        return pruneDuplicates(layers);
    }

    private static Integer state(Bundle bundle) {
        try {
            return bundle.getState();
        } catch (IllegalStateException e) {
            if (log.isDebug()) {
                log.debug("Got exception when checking bundle " + bundle + " state, assuming non-active", e);
            }
            return null;
        }
    }

    private static void buildLayers(PackageAdmin packageAdmin, BundleContext bundleContext, LinkedList<Set<Long>> layers) {
        for (Long bundleId : layers.getLast()) {
            Set<Long> dependants = findDependants(packageAdmin, bundleContext, bundleId);
            if (dependants.isEmpty()) {
                return;
            }
            layers.add(dependants);
            buildLayers(packageAdmin, bundleContext, layers);
        }
    }

    private static Set<Long> findDependants(PackageAdmin packageAdmin, BundleContext bundleContext, Long bundleId) {
        return findDependants(packageAdmin, bundleContext.getBundle(bundleId));
    }

    private static Set<Long> findDependants(PackageAdmin packageAdmin, Bundle bundle) {
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
        if (exportedPackages == null || exportedPackages.length == 0) {
            return Collections.emptySet();
        }
        Set<Long> layer = Generic.set();
        for (ExportedPackage exportedPackage : exportedPackages) {
            Bundle[] importingBundles = exportedPackage.getImportingBundles();
            if (importingBundles != null && exportedPackages.length > 0) {
                for (Bundle importingBundle : importingBundles) {
                    if (importingBundle != null) {
                        layer.add(importingBundle.getBundleId());
                    }
                }
            }
        }
        return layer;
    }

    private static List<Set<Long>> pruneDuplicates(List<Set<Long>> layers) {
        List<Set<Long>> pruned = Generic.list();
        Set<Long> knownIds = Generic.set();
        for (Set<Long> layer : layers) {
            layer.removeAll(knownIds);
            pruned.add(Generic.seal(layer));
            knownIds.addAll(layer);
        }
        return pruned;
    }

    private OSGiUtils() {
        // Don't make me
    }
}
