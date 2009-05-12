/*
 * Copyright 2008 Kjetil Valstadsve
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

import org.osgi.framework.Bundle;

import java.util.Collection;

/**
 * <P>This mediator helps keep track of the bundles in the {@link Bundle#ACTIVE ACTIVE} state.</P>
 *
 * <P>Create this mediator from the {@link net.sf.vanadis.osgi.Context#createBundleMediator(BundleMediatorListener)}
 * method, optionally passing a {@link net.sf.vanadis.osgi.BundleMediatorListener mediator listener}.
 */
public interface BundleMediator {

    /**
     * Get currently known active bundles.
     *
     * @return Bundles
     */
    Collection<Bundle> getBundles();

    /**
     * Stop the mediation.
     */
    void close();
}
