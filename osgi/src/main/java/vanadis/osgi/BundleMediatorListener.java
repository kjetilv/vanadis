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

package vanadis.osgi;

import org.osgi.framework.Bundle;

/**
 * A callback interface for the {@link vanadis.osgi.BundleMediator}.
 */
public interface BundleMediatorListener {

    /**
     * <p>A bundle was activated, i.e. it has entered the {@link Bundle#ACTIVE} state.</p>
     *
     * <p>This method is called for all existing, active bundles when the mediator is
     * {@link vanadis.osgi.Context#createBundleMediator(BundleMediatorListener) created}.</p>
     *
     * @param bundle Bundle
     */
    void activated(Bundle bundle);

    /**
     * <p>A bundle was deactivated, i.e. it has exited the {@link Bundle#ACTIVE} state.</p>
     *
     * @param bundle Bundle
     */
    void deactivated(Bundle bundle);
}
