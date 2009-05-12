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
package net.sf.vanadis.integrationtests;

import net.sf.vanadis.ext.CoreProperty;
import net.sf.vanadis.ext.ObjectManager;
import net.sf.vanadis.osgi.Filter;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class DeployLaunchesTest extends SingleFelixTestCase {

    private static final String NAME = "javacalc-add";

    private static final String TYPE = "javacalc-add";

    private static final Filter OMF_FILTER = CoreProperty.OBJECTMANAGER_TYPE.filter(NAME);

    private static final Filter OM_FILTER =
            CoreProperty.OBJECTMANAGER_NAME.filter(NAME).and(OMF_FILTER);

    @Test(timeout = 60000L)
    public void simpleLaunch() {
        registerVBundle(session(), "net.sf.vanadis.modules.examples.javacalc", "calcservices");
        int[] states1 = new int[]{Bundle.STARTING, Bundle.ACTIVE};
        waitForBundle(session(), "net.sf.vanadis.modules.examples.javacalc.calcservices", states1);

        registerVBundle(session(), "net.sf.vanadis.modules.examples.javacalc", "add");
        int[] states = new int[]{Bundle.STARTING, Bundle.ACTIVE};
        waitForBundle(session(), "net.sf.vanadis.modules.examples.javacalc.add", states);
        waitForObjectManagerFactory(session(), "javacalc-add");
        registerLaunch(session(), TYPE, NAME);
        waitForNonNull(session(), ObjectManager.class, OM_FILTER);
    }
}