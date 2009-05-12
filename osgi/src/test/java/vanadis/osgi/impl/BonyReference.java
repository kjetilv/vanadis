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
package net.sf.vanadis.osgi.impl;

import net.sf.vanadis.osgi.AbstractReference;
import net.sf.vanadis.osgi.ServiceProperties;

public class BonyReference<T> extends AbstractReference<T> {

    private final T service;

    private boolean invalid;

    public <C extends T> BonyReference(Class<T> serviceInterface, C service) {
        super(serviceInterface);
        this.service = service;
    }

    @Override
    protected Object get() {
        if (invalid) {
            throw new IllegalStateException(this + " invalid");
        }
        return service;
    }

    @Override
    public boolean unget() {
        this.invalid = true;
        return true;
    }

    @Override
    public ServiceProperties<T> getServiceProperties() {
        return ServiceProperties.create(getServiceInterface());
    }
}
