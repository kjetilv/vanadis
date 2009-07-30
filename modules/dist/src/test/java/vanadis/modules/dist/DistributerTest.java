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
package vanadis.modules.dist;

import vanadis.core.io.Location;
import org.junit.Test;

import java.io.IOException;

public class DistributerTest {

    @Test
    public void dummy() {

    }

    @Test
    public void setup()
        throws IOException {
        DistributorModule dmo1 = new DistributorModule().setEndPoint(new Location(10180));
        dmo1.activate();

        //new LineNumberReader(new InputStreamReader(System.in)).readLine();
    }

}
