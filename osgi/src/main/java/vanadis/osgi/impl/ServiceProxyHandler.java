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

package net.sf.vanadis.osgi.impl;

import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.osgi.Context;
import net.sf.vanadis.osgi.Filter;
import net.sf.vanadis.osgi.Reference;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ServiceProxyHandler<T> implements InvocationHandler {

    private final Context context;

    private final Class<T> serviceInterface;

    private final Filter filter;

    private final boolean persistent;

    private Reference<T> reference;

    ServiceProxyHandler(Context context, Class<T> serviceInterface, Filter filter, boolean persistent) {
        this.persistent = persistent;
        this.context = Not.nil(context, "context");
        this.serviceInterface = Not.nil(serviceInterface, "service interface");
        this.filter = filter;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        Reference<T> reference = getReference(false);
        if (reference == null) {
            throw new IllegalStateException(this + ": No target service found");
        }
        return attemptCall(method, args, reference);
    }

    private Object attemptCall(Method method, Object[] args, Reference<T> reference) throws IllegalAccessException, InvocationTargetException {
        T service = reference.getService();
        try {
            if (service == null) {
                throw new IllegalStateException(this + ": No target service found");
            }
            return method.invoke(service, args);
        } finally {
            ungetReference(reference);
        }
    }

    public void close() {
        if (persistent) {
            ungetReference(reference);
        }
    }

    private void ungetReference(Reference<T> reference) {
        if (reference != null) {
            if (!persistent) {
                try {
                    reference.unget();
                } catch (Exception e) {
                    log.warn(this + " failed to unget " + reference, e);
                }
            }
        }
    }

    private Reference<T> getReference(boolean clear) {
        if (persistent) {
            if (reference == null || clear) {
                reference = getFreshReference();
            }
            return reference;
        }
        return getFreshReference();
    }

    private Reference<T> getFreshReference() {
        return context.getReference(serviceInterface, filter);
    }

    private static final Log log = Logs.get(ServiceProxyHandler.class);
}
