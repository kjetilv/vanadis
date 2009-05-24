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

import vanadis.core.lang.ToString;
import vanadis.objectmanagers.ObjectManager;

class ObjectManagerMBeanImpl implements ObjectManagerMBean {

    private final ObjectManager objectManager;

    ObjectManagerMBeanImpl(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    @Override
    public String getName() {
        return objectManager.getName();
    }

    @Override
    public String getState() {
        return objectManager.getManagedState().name();
    }

    @Override
    public String getObject() {
        return objectManager.getManagedObject().toString();
    }

    @Override
    public String getClassLoader() {
        return objectManager.getManagedObjectClassLoader().toString();
    }

    @Override
    public String toString() {
        return ToString.of(this, objectManager);
    }
}
