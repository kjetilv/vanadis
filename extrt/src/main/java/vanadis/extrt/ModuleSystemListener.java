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

import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import org.osgi.framework.Bundle;

interface ModuleSystemListener {

    /**
     * <P>Notify that a bundle has been added.  Will install the bundle from the URI.  Later,
     * {@link org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)} is invoked.
     * When this happens, it is either because we installed the bundle, or someone else did.  Either way,
     * that will force us to look through the bundle for
     * {@link vanadis.ext.ObjectManagerFactory object manager factories}.</P>
     *
     * <p>Properties respected:</p>
     *
     * <ul>
     * <li><dt><code>start-level</code></dt> <dd>start level for bundle</dd></li>
     * </ul>
     *
     * @param bundleSpecification Bundle specification
     */
    void bundleSpecificationAdded(BundleSpecification bundleSpecification);

    /**
     * Bundle was removed.
     *
     * @param bundleSpecification URI for bundle
     */
    void bundleSpecificationRemoved(BundleSpecification bundleSpecification);

    /**
     * <P>Notify that a launch has been added.  This launch will refer to an existing (or future)
     * {@link vanadis.ext.ObjectManagerFactory object manager factory},
     * and specify the creation of an actual, named
     * {@link vanadis.ext.ObjectManager object manager}.
     *
     * @param moduleSpecification Actual launch specification
     */
    void moduleSpecificationAdded(ModuleSpecification moduleSpecification);

    /**
     * A launch has been removed.  Take down the {@link vanadis.ext.ObjectManager object manager}
     * that was specified by it.
     *
     * @param moduleSpecification Actual launch specification
     */
    void moduleSpecificationRemoved(ModuleSpecification moduleSpecification);

    /**
     * Notify that these bundles existed prior to the startup.
     *
     * @param bundles The bundles
     */
    void spool(Iterable<Bundle> bundles);
}
