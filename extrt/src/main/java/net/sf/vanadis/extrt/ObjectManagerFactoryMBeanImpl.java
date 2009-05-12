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

package net.sf.vanadis.extrt;

import net.sf.vanadis.blueprints.ModuleSpecification;
import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.ObjectManagerFactory;
import net.sf.vanadis.ext.ObjectManagerFactoryMBean;

import java.util.List;

class ObjectManagerFactoryMBeanImpl implements ObjectManagerFactoryMBean {

    private final ObjectManagerFactory objectManagerFactory;

    ObjectManagerFactoryMBeanImpl(ObjectManagerFactory objectManagerFactory) {
        this.objectManagerFactory = objectManagerFactory;
    }

    @Override
    public String getModuleClass() {
        return objectManagerFactory.getModuleClass().getName();
    }

    @Override
    public String getContextName() {
        return objectManagerFactory.getContextName();
    }

    @Override
    public String[] getLaunchedServices() {
        List<String> names = Generic.list();
        for (ModuleSpecification moduleSpecification : objectManagerFactory) {
            names.add(moduleSpecification.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public String getType() {
        return objectManagerFactory.getType();
    }

    @Override
    public String toString() {
        return ToString.of(this, objectManagerFactory);
    }
}