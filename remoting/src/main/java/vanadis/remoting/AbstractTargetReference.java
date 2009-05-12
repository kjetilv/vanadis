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
package net.sf.vanadis.remoting;

import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.services.remoting.RemotingException;
import net.sf.vanadis.services.remoting.TargetReference;

import java.io.Serializable;

public abstract class AbstractTargetReference<T, L> implements TargetReference<T, L>, Serializable {

    private final String targetInterfaceName;

    private final String simpleTargetInterfaceName;

    private final Class<L> lookupClass;

    private transient Class<T> targetInterface;

    private transient T resolved;

    protected AbstractTargetReference(String targetInterfaceName,
                                      Class<L> lookupClass) {
        this(null, Not.nil(targetInterfaceName, "target interface name"), lookupClass);
    }

    protected AbstractTargetReference(Class<T> targetInterface,
                                      Class<L> lookupClass) {
        this(Not.nil(targetInterface, "target interface"), null, lookupClass);
    }

    AbstractTargetReference(Class<T> targetInterface, String targetInterfaceName, Class<L> lookupClass) {
        if (targetInterface == null && targetInterfaceName == null) {
            throw new IllegalArgumentException("Target interface or interface name requried");
        }
        this.lookupClass = Not.nil(lookupClass, "lookup class");
        this.targetInterface = targetInterface;
        this.targetInterfaceName = targetInterfaceName != null ? targetInterfaceName
                : targetInterface.getName();
        this.simpleTargetInterfaceName = targetInterface != null ? targetInterface.getSimpleName()
                : targetInterfaceName;
    }

    @Override
    public final Class<L> getLookupClass() {
        return lookupClass;
    }

    protected final String getTargetInterfaceName() {
        return targetInterfaceName;
    }

    protected abstract Object retrieve(L lookup);

    @Override
    public final T get(L locator) {
        if (resolved != null) {
            return resolved;
        }
        Object object = retrieve(locator);
        if (object == null) {
            return null;
        }
        Class<?> type = resolveClass(object);
        if (type.isInstance(object)) {
            resolved = cast(type, object);
            return resolved;
        }
        return null;
    }

    @Override
    public Class<T> getTargetInterface() {
        return targetInterface;
    }

    @Override
    public void restoreTargetInterface(ClassLoader classLoader) {
        if (targetInterface == null) {
            targetInterface = restoredInterface(classLoader);
        }
    }

    @SuppressWarnings({"unchecked"})
    private Class<T> restoredInterface(ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(targetInterfaceName, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RemotingException(this + " failed to restore interface with " + classLoader, e);
        }
    }

    @Override
    public final void dispose() {
        wasDisposed();
    }

    void wasDisposed() {
    }

    private Class<?> resolveClass(Object target) {
        try {
            return Class.forName(targetInterfaceName, true, target.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RemotingException
                    ("Unable to resolve class " + targetInterfaceName + " for target " +
                            target + " of " + target.getClass(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private T cast(Class<?> targetInterface, Object object) {
        return (T) ((Class<?>) targetInterface).cast(object);
    }

    @Override
    public String toString() {
        return ToString.of(this, simpleTargetInterfaceName, "resolved", resolved != null);
    }

    private static final long serialVersionUID = 4345L;
}
