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
package net.sf.vanadis.integrationtests;

import net.sf.vanadis.blueprints.BundleSpecification;
import net.sf.vanadis.blueprints.ModuleSpecification;
import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.ext.ManagedState;
import net.sf.vanadis.ext.ObjectManager;
import net.sf.vanadis.osgi.Reference;
import net.sf.vanadis.osgi.Registration;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.Bundle;

public abstract class SingleFelixTestCase extends FelixTestCase {

    private FelixTestSession session;

    static {
        System.setProperty("vanadis.felix.noshell", "true");
        System.setProperty("vanadis.felix.notext", "true");
    }

    @Before
    public void setupFelix() {
        session = newFelixSession(TimeSpan.MINUTE, true);
    }

    @After
    public void teardownFelix() {
        freeFelixSession(session);
    }

    protected FelixTestSession session() {
        return session;
    }

    protected Registration<ModuleSpecification> registerLaunch(String name) {
        return registerLaunch(session(), name, name);
    }

    protected void assertObjectManagerState(String name, ManagedState state) {
        assertObjectManagerState(session(), name, state);
    }

    protected Reference<ObjectManager> waitForObjectManager(String name) {
        return waitForObjectManager(session(), name);
    }

    protected void waitForObjectManagerFactory(String name) {
        waitForObjectManagerFactory(session(), name);
    }

    protected void waitForLostObjectManagerFactory(String name) {
        waitForLostObjectManagerFactory(session(), name);
    }

    protected void waitForActiveBundle(String name) {
        waitForBundle(session(), name, Bundle.ACTIVE, Bundle.STARTING);
    }

    protected Registration<BundleSpecification> registerBundle(String groupAndArtifactPrefix, String artifact) {
        return registerVBundle(session(), groupAndArtifactPrefix, artifact);
    }
}
