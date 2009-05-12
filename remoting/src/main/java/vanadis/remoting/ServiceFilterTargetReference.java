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
package vanadis.remoting;

import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.Reference;

import java.util.Collection;

public class ServiceFilterTargetReference<T>
        extends AbstractTargetReference<T, Context> {

    private static final long serialVersionUID = -6053968078913412878L;

    private final Filter filter;

    private Reference<?> reference;

    public static <T> ServiceFilterTargetReference<T> create(Class<T> type) {
        return new ServiceFilterTargetReference<T>(type);
    }

    public ServiceFilterTargetReference(Class<T> serviceInterface) {
        this(serviceInterface, null);
    }

    public ServiceFilterTargetReference(String serviceInterfaceName) {
        this(null, serviceInterfaceName, null);
    }

    public ServiceFilterTargetReference(Class<T> serviceInterface, Filter filter) {
        this(serviceInterface, null, filter);
    }

    public ServiceFilterTargetReference(String serviceInterfaceName, Filter filter) {
        this(null, serviceInterfaceName, filter);
    }

    private ServiceFilterTargetReference(Class<T> serviceInterface,
                                         String serviceInterfaceName,
                                         Filter filter) {
        super(serviceInterface, serviceInterfaceName, Context.class);
        this.filter = filter;
    }

    @Override
    protected Object retrieve(Context context) {
        Collection<Reference<?>> references =
                context.getReferences(getTargetInterfaceName(), filter);
        if (references == null || references.isEmpty()) {
            return null;
        }
        if (references.size() > 1) {
            throw new IllegalStateException
                    (this + " mapped to " + references.size() + " instances: " + references);
        }
        reference = references.iterator().next();
        return reference.getService();
    }

    @Override
    protected void wasDisposed() {
        reference.unget();
    }
}
