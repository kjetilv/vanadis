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

import net.sf.vanadis.blueprints.BundleSpecification;
import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.collections.Member;
import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.osgi.Context;
import net.sf.vanadis.osgi.OSGiException;
import net.sf.vanadis.osgi.OSGiUtils;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

class Bundles implements Iterable<Bundle>, Closeable {

    private final BundleContext bundleContext;

    private final Context context;

    private final Map<BundleSpecification, Bundle> specifiedBundles = Generic.linkedHashMap();

    private final LinkedHashMap<BundleSpecification, Date> specHistory;

    private final Map<Long, BundleSpecification> idSpecs = Generic.linkedHashMap();

    private final PackageAdmin packageAdmin;

    private final StartLevel startLevel;

    Bundles(BundleContext bundleContext, Context context) {
        this(bundleContext, context, 100);
    }

    Bundles(BundleContext bundleContext, Context context, int historySize) {
        this.bundleContext = Not.nil(bundleContext, "bundle context");
        this.context = Not.nil(context, "context");
        this.specHistory = Generic.sizeLimitedHashMap(historySize);
        this.packageAdmin = context.getServiceProxy(PackageAdmin.class);
        this.startLevel = context.getServiceProxy(StartLevel.class);
    }

    List<Set<Long>> dependentBundles(long id) {
        return OSGiUtils.dependentBundles(this.packageAdmin, this.bundleContext, id);
    }

    public boolean isHosting(BundleSpecification bundleSpecification) {
        return specifiedBundles.containsKey(bundleSpecification);
    }

    public long reload(BundleSpecification specification) {
        Bundle bundle = specifiedBundles.get(specification);
        if (bundle == null) {
            throw new IllegalArgumentException(this + " does not know any bundles loaded from" + specification);
        }
        long id = bundle.getBundleId();
        uninstall(specification);
        install(specification);
        return id;
    }

    public long reload(long bundleId) {
        return reload(getSpecification(bundleId));
    }

    public void uninstall(long bundleId) {
        uninstall(getSpecification(bundleId));
    }

    public void uninstall(BundleSpecification uri) {
        Bundle bundle = removeBundle(uri);
        stop(bundle);
        uninstall(bundle);
        log.info("Uninstalled bundle " + bundle + " @ " + uri + " : " + bundle);
    }


    public void install(BundleSpecification specification) {
        if (specification.isGlobalProperties()) {
            PortUtils.writeToSystemProperties(specification.getPropertySet(), context.getLocation());
        }
        Bundle bundle = installed(specification);
        adjustStartLevel(bundle, specification);
        storeBundle(specification, bundle);
        tryStartingBundles(specification);
    }

    @Override
    public void close() {
        List<Long> ids = Generic.list(idSpecs.keySet());
        Collections.reverse(ids);
        for (long id : ids) {
            try {
                uninstall(id);
            } catch (Exception e) {
                log.warn(this + " failed to uninstall bundle " + id, e);
            }
        }
    }

    @Override
    public Iterator<Bundle> iterator() {
        return Arrays.asList(bundleContext.getBundles()).iterator();
    }

    public Long getBundleId(BundleSpecification specification) {
        Bundle bundle = getBundle(specification, false);
        return bundle == null ? null : bundle.getBundleId();
    }

    public Bundle getBundle(long bundleId) {
        for (Bundle bundle : this) {
            if (bundle.getBundleId() == bundleId) {
                return bundle;
            }
        }
        return null;
    }

    private void adjustStartLevel(Bundle bundle, BundleSpecification bundleSpecification) {
        Integer level = bundleSpecification.getStartLevel();
        if (level != null) {
            startLevel.setBundleStartLevel(bundle, level);
        }
    }

    private Bundle getBundle(BundleSpecification specification,
                             boolean remove) {
        Bundle bundle = remove ? specifiedBundles.remove(specification) : specifiedBundles.get(specification);
        if (bundle == null) {
            reportUnknownBundle(specification, remove);
        }
        return bundle;
    }

    private void reportUnknownBundle(BundleSpecification uri, boolean remove) {
        Date date = specHistory.get(uri);
        throw new IllegalArgumentException(this + " was " +
                (remove ? "asked to remove" : "asked for") +
                " unknown bundle uri " + uri +
                (date == null ? "" : ", which has been removed, last added @ " + date));
    }

    private BundleSpecification getSpecification(long bundleId) {
        BundleSpecification uri = idSpecs.get(bundleId);
        if (uri == null) {
            throw new UnknownBundleException("Unknown bundle id: " + bundleId);
        }
        return uri;
    }

    private Bundle removeBundle(BundleSpecification uri) {
        Bundle bundle = getBundle(uri, true);
        if (bundle != null) {
            BundleSpecification sameSpecification = idSpecs.remove(bundle.getBundleId());
            assert sameSpecification.equals(uri) : "Bundle " + bundle + " with id " + bundle.getBundleId() +
                    " registered on uri " + uri + " had different id/uri mapping => " + sameSpecification;
        }
        return bundle;
    }

    private void storeBundle(BundleSpecification specification, Bundle bundle) {
        specifiedBundles.put(specification, bundle);
        specHistory.put(specification, new Date());
        idSpecs.put(bundle.getBundleId(), specification);
    }

    private void tryStartingBundles(BundleSpecification uri) {
        Collection<Throwable> exceptions = Generic.list();
        startBundles(exceptions);
        if (!exceptions.isEmpty()) {
            log.error(this + " got " + exceptions.size() + " exceptions when installing " + uri);
        }
    }

    private void startBundles(Collection<Throwable> exceptions) {
        int currentInactive = Integer.MAX_VALUE;
        while (true) {
            int previousInactive = currentInactive;
            currentInactive = startResolvedBundles(exceptions);
            if (currentInactive == 0 || currentInactive == previousInactive) {
                return;
            }
        }
    }

    private Bundle installed(BundleSpecification specification) {
        String uriString = specification.getUriString();
        if (specification.isFile()) {
            return installBundle(uriString);
        } else {
            InputStream stream = openStream(specification.getUri());
            try {
                return validateInstall(uriString, stream);
            } finally {
                Closeables.close(stream);
            }
        }
    }

    private Bundle validateInstall(String uriString, InputStream stream) {
        Bundle bundle = installBundle(uriString, stream);
        log.info("Installed bundle @ " + uriString + " : " + bundle);
        return bundle;
    }

    private Bundle installBundle(String uriString) {
        try {
            return nonNull(bundleContext.installBundle(uriString), uriString);
        } catch (Exception e) {
            throw new OSGiException("Failed to install bundle @ " + uriString, e);
        }
    }

    private Bundle installBundle(String uriString, InputStream stream) {
        try {
            return nonNull(bundleContext.installBundle(uriString, stream), uriString);
        } catch (Exception e) {
            throw new OSGiException("Failed to install bundle @ " + uriString + " from stream " + stream, e);
        }
    }

    private int startResolvedBundles(Collection<Throwable> exceptions) {
        int inactive = 0;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (!(isStarted(bundle) || wasStarted(bundle, exceptions))) {
                inactive++;
            }
        }
        return inactive;
    }

    private static final Log log = Logs.get(net.sf.vanadis.extrt.Bundles.class);

    private static final String EQUINOX_UNRESOLVED = "bundle could not be resolved";

    private static final String FELIX_UNRESOLVED = "unresolved package in bundle";

    private static final String FELIX_UNRESOLVED_2 = "unresolved constraint in bundle";

    private static Bundle nonNull(Bundle bundle, Object source) {
        if (bundle != null) {
            return bundle;
        }
        throw new IllegalStateException("Unexpected null bundle installed @ " + source);
    }

    private static InputStream openStream(URI uri) {
        try {
            return uri.toURL().openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid URI for streaming: " + uri, e);
        }
    }

    private static void uninstall(Bundle bundle) {
        if (bundle.getState() != Bundle.UNINSTALLED) {
            try {
                bundle.uninstall();
            } catch (IllegalStateException e) {
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    throw new OSGiException("Failed to uninstall bundle " + bundle, e);
                } else {
                    // There was a race
                }
            } catch (BundleException e) {
                throw new OSGiException("Failed to uninstall bundle " + bundle, e);
            }
        }
    }

    private static boolean wasStarted(Bundle bundle, Collection<Throwable> exceptions) {
        int initialState = bundle.getState();
        long id = bundle.getBundleId();
        if (isStarted(initialState)) {
            return true;
        }
        if (Member.of(initialState, Bundle.INSTALLED, Bundle.RESOLVED)) {
            String name = bundle.getSymbolicName();
            try {
                bundle.start();
                log.info("Started bundle " + id + ", symbolic name: " + name + " @ " + bundle.getLocation());
                return true;
            } catch (BundleException e) {
                handle(name, exceptions, e);
            }
        }
        int finalBundleState;
        try {
            finalBundleState = bundle.getState();
        } catch (Exception e) {
            log.error("Bundle " + id + " failed to reveal its state after start attempt", e);
            return false;
        }
        return Member.of(finalBundleState, Bundle.ACTIVE, Bundle.STARTING);
    }

    private static void handle(String name, Collection<Throwable> exceptions,
                               BundleException bundleException) {
        String message = bundleException.getMessage().toLowerCase();
        if (justAnotherUnresolvedBundle(message)) {
            log.info("Not yet resolved: " + name + ": " + message);
        } else {
            log.error("Failed to start " + name, bundleException);
            exceptions.add(bundleException);
        }
    }

    private static void stop(Bundle bundle) {
        if (Member.of(bundle.getState(), Bundle.ACTIVE, Bundle.STARTING)) {
            try {
                bundle.stop();
            } catch (BundleException e) {
                log.error("Failed to stop bundle " + bundle.getSymbolicName(), e);
            }
        }
    }

    private static boolean isStarted(Bundle bundle) {
        return isStarted(bundle.getState());
    }

    private static boolean isStarted(int state) {
        return Member.of(state, Bundle.ACTIVE, Bundle.STARTING, Bundle.STOPPING);
    }

    private static boolean justAnotherUnresolvedBundle(String message) {
        return message.contains(EQUINOX_UNRESOLVED) ||
                message.contains(FELIX_UNRESOLVED) ||
                message.contains(FELIX_UNRESOLVED_2);
    }

    @Override
    public String toString() {
        return ToString.of(this, "uris", specifiedBundles.size(), "uriHistory", specHistory.size());
    }
}