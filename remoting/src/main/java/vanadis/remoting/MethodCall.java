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

import net.sf.vanadis.core.lang.ContextClassLoaderSwitch;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.lang.VarArgs;
import net.sf.vanadis.services.remoting.TargetReference;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public final class MethodCall extends AbstractWireSessionable {

    private static final long serialVersionUID = -4745617322415339187L;

    private static final Object[] NO_ARGS = new Object[]{};

    private static Object[] varArgsOf(Collection<Object> args) {
        return args.toArray(new Object[args.size()]);
    }

    private Object target;

    private Object[] args;

    private TargetReference<?, ?> reference;

    private int methodNo;

    private transient Method method;

    public MethodCall() {
    } // Required for Externalizable classes

    public MethodCall(TargetReference<?, ?> reference, Method method,
                      Collection<Object> args) {
        this(null, reference, method, args);
    }

    public MethodCall(Session session, TargetReference<?, ?> reference, Method method,
                      Collection<Object> args) {
        this(session, reference, method, varArgsOf(args));
    }

    public MethodCall(TargetReference<?, ?> reference, Method method,
                      Object... args) {
        this(null, reference, method, args);
    }

    public MethodCall(Session session, TargetReference<?, ?> reference, Method method,
                      Object... args) {
        super(session);
        this.reference = Not.nil(reference, "reference");
        this.method = Not.nil(method, "method");
        this.methodNo = Accessor.indexOf(reference.getTargetInterface(), method);
        this.args = VarArgs.present(args) ? args : NO_ARGS;
    }

    public MethodCallResult invoke() {
        if (!isOnTarget()) {
            throw new IllegalArgumentException(this + " is not on target");
        }
        Method method = this.method;
        if (!method.getDeclaringClass().isInstance(target)) {
            throw new IllegalStateException(this + " resolved to target " + method + " of " + target.getClass() +
                    ", which is not compatible with method " + method);
        }
        Object result;
        try {
            result = method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(this.method + " should be an accessible interface method", e);
        } catch (InvocationTargetException e) {
            return new MethodExceptionThrown(getSession(), e.getCause());
        }
        return new MethodReturned(getSession(), result);
    }

    public TargetReference<?, ?> getTargetIdentifier() {
        return reference;
    }

    @Override
    public void writeExternal(ObjectOutput stream)
            throws IOException {
        super.writeExternal(stream);
        stream.writeObject(reference);
        stream.writeInt(methodNo);
        stream.writeObject(args);
    }

    @Override
    public void readExternal(ObjectInput stream)
            throws IOException, ClassNotFoundException {
        super.readExternal(stream);
        this.reference = readReference(stream);
        this.methodNo = stream.readInt();
        this.target = resolveTarget(reference);
        if (target != null) {
            this.reference.restoreTargetInterface(this.target.getClass().getClassLoader());
            this.args = readArgs(stream);
            this.method = Accessor.methodNo(this.reference.getTargetInterface(), this.methodNo);
        }
    }

    private static TargetReference<?, ?> readReference(ObjectInput stream)
            throws IOException, ClassNotFoundException {
        return (TargetReference<?, ?>) stream.readObject();
    }

    private Object resolveTarget(TargetReference<?, ?> reference) {
        TargetMatch<?> targetMatch = Accessor.getSingleton().get(getSession(), reference);
        if (targetMatch == null) {
            return null;
        }
        Session identifiedSession = targetMatch.getSession();
        if (identifiedSession == null) {
            return null;
        }
        if (getSession() == null) {
            setSession(identifiedSession);
        } else if (!getSession().equals(identifiedSession)) {
            throw new IllegalStateException(this + " had session " + getSession() + ", different from session of " +
                    target + ": " + identifiedSession);
        }
        return targetMatch.getTarget();
    }

    private Object[] readArgs(ObjectInput ois)
            throws IOException, ClassNotFoundException {
        ClassLoader classLoader = target.getClass().getClassLoader();
        ContextClassLoaderSwitch contextSwitch = new ContextClassLoaderSwitch(classLoader);
        try {
            return (Object[]) ois.readObject();
        } finally {
            contextSwitch.revert();
        }
    }

    private void readObjectNoData() {
    }

    public Method getMethod() {
        return method;
    }

    public boolean isOnTarget() {
        return this.target != null;
    }

    /**
     * For testing purposes only!
     *
     * @param target Faux target!
     */
    public void setTarget(Object target) {
        if (this.target == null) {
            this.target = target;
        } else {
            throw new IllegalStateException(this + " already has a target");
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "target", target, "method", method, "args", Arrays.toString(args));
    }

}
