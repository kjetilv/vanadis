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

import vanadis.ext.ObjectManagerExposePointMBean;

class ObjectManagerExposePointMBeanImpl implements ObjectManagerExposePointMBean {

    private final Exposer<?> exposer;

    ObjectManagerExposePointMBeanImpl(Exposer<?> exposer) {
        this.exposer = exposer;
    }

    @Override
    public boolean isMulti() {
        return exposer.isMulti();
    }

    @Override
    public long[] getServiceIds() {
        return exposer.getServiceIds();
    }

    @Override
    public String getServiceInterface() {
        return exposer.getServiceInterface().getName();
    }

    @Override
    public String getName() {
        return exposer.getFeatureName();
    }

    @Override
    public boolean isActive() {
        return exposer.isActive();
    }
}
