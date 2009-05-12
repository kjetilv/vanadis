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
package vanadis.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

public class ReallyChallengingDistCalcFailoverTest extends MultipleFelixRemotingTestCase {

    @Test(timeout = 600000L)
    public void reallyDistCalcFailoverSpanner() {
        rDC(SHORT_TIMEOUT, false, true);
    }

    @Test
    // Comment out this line to run test in IDE and avoid timeouts (at least for a day!)
    @Ignore
    public void reallyDebuggableDistCalcFailoverSpanner() {
        rDC(LONG_TIMEOUT, false, true);
    }
}