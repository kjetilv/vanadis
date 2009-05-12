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
package net.sf.vanadis.launcher;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.List;

public final class LaunchResult {

    private final BundleContext bundleContext;

    private final List<Bundle> autoBundles;

    public LaunchResult(BundleContext bundleContext, List<Bundle> autoBundles) {
        this.bundleContext = Not.nil(bundleContext, "bundle context");
        this.autoBundles = autoBundles == null ? Collections.<Bundle>emptyList() : autoBundles;
    }

    public List<Bundle> getNonCoreBundles() {
        List<Bundle> noncore = Generic.list();
        long systemId = bundleContext.getBundle().getBundleId();
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getBundleId() > systemId && !autoBundles.contains(bundle)) {
                noncore.add(bundle);
            }
        }
        Collections.reverse(noncore);
        return noncore;
    }

    public List<Bundle> getAutoBundles() {
        return getAutoBundles(false);
    }

    public List<Bundle> getAutoBundles(boolean reverse) {
        if (!reverse) {
            return autoBundles;
        }
        List<Bundle> reversed = Generic.list(autoBundles);
        Collections.reverse(reversed);
        return reversed;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public String toString() {
        return ToString.of(this, "bundleContext", bundleContext, "bundles", autoBundles);
    }

    public Bundle[] getAllBundles() {
        return bundleContext.getBundles();
    }
}
