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

import org.junit.After;
import org.junit.Before;
import vanadis.blueprints.ModuleSpecification;
import vanadis.core.io.Location;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.impl.BareBonesContext;

public abstract class ObjectManagementTestCase {

    private BareBonesContext context;

    protected static final Location location = new Location("localhost", 4200);

    protected ObjectManager manage(ModuleSpecification moduleSpecification,
                                   Object managed) {
        return ObjectManagerImpl.create
                (context, moduleSpecification, null, managed, null, null);
    }

    protected ObjectManager manage(Object managed) {
        return ObjectManagerImpl.create
                (context, null, null, managed, null, null);
    }

    @Before
    public void before() {
        context = new BareBonesContext().setLocation(location);
    }

    @After
    public void after() {
        context = null;
    }

    public BareBonesContext getContext() {
        return context;
    }
}
