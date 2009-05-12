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

import junit.framework.Assert;
import net.sf.vanadis.core.time.Deadline;
import net.sf.vanadis.core.time.TimeSpan;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.Arrays;

public class DeployBundlesTest extends SingleFelixTestCase {

    @Test(timeout = 60000L)
    public void simpleDeploy() {
        Bundle[] bundles = session().getLaunch().getLaunchResult().getBundleContext().getBundles();
        int count = bundles.length;
        registerVBundle(session(), "net.sf.vanadis.modules.examples.javacalc", "calcservices");
        registerVBundle(session(), "net.sf.vanadis.modules.examples.javacalc", "add");

        waitForBundle(session(), "net.sf.vanadis.modules.examples.javacalc.add", Bundle.ACTIVE);

        Deadline deadline = TimeSpan.MINUTE.newDeadline();
        int newCount = session().getLaunch().getLauncher().getLaunchResult().getAllBundles().length;
        while ((newCount - count < 2) && !deadline.hasExpired()) {
            deadline.getSleepTime(TimeSpan.SECOND).sleep();
            newCount = session().getLaunch().getLauncher().getLaunchResult().getAllBundles().length;
        }
        Assert.assertEquals(count + 2, newCount);
        System.out.println
                (Arrays.toString(
                        session().getLaunch().getLauncher().getLaunchResult().getBundleContext().getBundles()));
    }
}
