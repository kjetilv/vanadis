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
package vanadis.osgi;

import vanadis.core.lang.Not;

import java.io.IOException;

public abstract class AbstractReference<T> implements Reference<T> {

    private final Class<T> serviceInterface;

    protected AbstractReference(Class<T> serviceInterface) {
        this.serviceInterface = Not.nil(serviceInterface, "service interface");
    }

    public final Class<T> getServiceInterface() {
        return serviceInterface;
    }

    @Override
    public final T getService() {
        return getServiceInterface().cast(get());
    }

    @Override
    public String getServicePid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getServiceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unget() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceProperties<T> getServiceProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object getRawService() {
        return get();
    }

    protected abstract Object get();

    @Override
    public void close()
            throws IOException {
    }

}
