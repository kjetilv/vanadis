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

import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.ObjectManagerInjectPointMBean;

class ObjectManagerInjectPointMBeanImpl implements ObjectManagerInjectPointMBean {

    private final Injector<?> injector;

    ObjectManagerInjectPointMBeanImpl(Injector<?> injector) {
        this.injector = injector;
    }

    @Override
    public boolean isMulti() {
        return injector.isMulti();
    }

    @Override
    public long[] getServiceIds() {
        return injector.getServiceIds();
    }

    @Override
    public String getServiceInterface() {
        return injector.getServiceInterface().getName();
    }

    @Override
    public String getName() {
        return injector.getFeatureName();
    }

    @Override
    public boolean isActive() {
        return injector.isActive();
    }

    @Override
    public String toString() {
        return ToString.of(this, injector);
    }
}
