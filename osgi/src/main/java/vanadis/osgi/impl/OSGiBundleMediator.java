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
package net.sf.vanadis.osgi.impl;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.osgi.BundleMediator;
import net.sf.vanadis.osgi.BundleMediatorListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

class OSGiBundleMediator implements BundleMediator, BundleListener {

    private final BundleContext bundleContext;

    private final BundleMediatorListener bundleMediatorListener;

    private final ConcurrentMap<Long, Bundle> bundles = Generic.concurrentHashMap();

    OSGiBundleMediator(BundleContext bundleContext,
                       BundleMediatorListener bundleMediatorListener) {
        this.bundleContext = bundleContext;
        this.bundleMediatorListener = bundleMediatorListener;
        open();
        spoolBundles();
    }

    private void open() {
        this.bundleContext.addBundleListener(this);
    }

    private void spoolBundles() {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                active(bundle);
            }
        }
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        int type = bundleEvent.getType();
        if (type == BundleEvent.STARTED) {
            active(bundleEvent.getBundle());
        } else if (type == BundleEvent.STOPPING) {
            inactive(bundleEvent.getBundle());
        }
    }

    @Override
    public Collection<Bundle> getBundles() {
        return Generic.set(bundles.values());
    }

    @Override
    public void close() {
        this.bundleContext.removeBundleListener(this);
    }

    private void active(Bundle bundle) {
        Bundle previousEntry = bundles.putIfAbsent(bundle.getBundleId(), bundle);
        if (bundleMediatorListener != null && previousEntry == null) {
            bundleMediatorListener.activated(bundle);
        }
    }

    private void inactive(Bundle bundle) {
        boolean removed = bundles.remove(bundle.getBundleId(), bundle);
        if (bundleMediatorListener != null && removed) {
            bundleMediatorListener.deactivated(bundle);
        }
    }
}
