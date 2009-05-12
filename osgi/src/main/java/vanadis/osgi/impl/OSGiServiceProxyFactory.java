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

package vanadis.osgi.impl;

import vanadis.core.lang.Not;
import vanadis.core.lang.Proxies;
import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.ServiceProxyFactory;

import java.lang.reflect.InvocationHandler;

final class OSGiServiceProxyFactory implements ServiceProxyFactory {

    private final Context context;

    OSGiServiceProxyFactory(Context context) {
        this.context = context;
    }

    @Override
    public <T> T get(Class<T> serviceInterface, Filter filter) {
        return get(loader(serviceInterface),
                   serviceInterface,
                   filter);
    }

    @Override
    public <T> T getPersistent(Class<T> serviceInterface, Filter filter) {
        return getPersistent(loader(serviceInterface), serviceInterface, filter);
    }

    @Override
    public <T> T get(ClassLoader classLoader, Class<T> serviceInterface, Filter filter) {
        return proxy(classLoader, serviceInterface,
                     new ServiceProxyHandler<T>(context, serviceInterface, filter, false));
    }

    @Override
    public <T> T getPersistent(ClassLoader classLoader, Class<T> serviceInterface, Filter filter) {
        return proxy(classLoader, serviceInterface,
                     new ServiceProxyHandler<T>(context, serviceInterface, filter, true));
    }

    @Override
    public void closePersistent(Object service) {
        ServiceProxyHandler<?> handler = Proxies.handler(ServiceProxyHandler.class, service);
        if (handler != null) {
            handler.close();
        }
    }

    private static <T> T proxy(ClassLoader classLoader, Class<T> serviceInterface, InvocationHandler handler) {
        return Proxies.genericProxy(classLoader, serviceInterface, handler);
    }

    private static ClassLoader loader(Class<?> serviceInterface) {
        return Not.nil(serviceInterface, "service interface").getClassLoader();
    }
}
