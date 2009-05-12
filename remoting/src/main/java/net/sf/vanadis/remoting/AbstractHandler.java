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

import net.sf.vanadis.core.lang.EqHc;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.services.remoting.RemotingException;
import net.sf.vanadis.services.remoting.TargetHandle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

public abstract class AbstractHandler extends AbstractSessionable implements InvocationHandler {

    private final TargetHandle<?> handle;

    protected AbstractHandler(TargetHandle<?> handle) {
        this(null, handle);
    }

    protected AbstractHandler(Session session, TargetHandle<?> handle) {
        super(session);
        this.handle = Not.nil(handle, "handle");
    }

    @Override
    public final Object invoke(Object o, Method method, Object[] args)
            throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }
        MethodCall methodCall = new MethodCall(getSession(), getHandle().getReference(), method, args);
        MethodCallResult result = invoke(methodCall);
        adoptOrVerifySession(result);
        return result.isReturnedNormally() ? result.getValue()
                : handleAbnormalReturn(methodCall, result);
    }

    private static Object handleAbnormalReturn(MethodCall call, MethodCallResult result)
            throws Throwable {
        if (!result.isTargetFound()) {
            throw new RemotingException(call + " failed, did not hit a target!");
        }
        Method method = call.getMethod();
        Throwable exception = result.getException();
        if (exception instanceof RuntimeException || exception instanceof Error) {
            throw exception;
        }
        for (Class<?> allowedClass : method.getExceptionTypes()) {
            Class<? extends Throwable> exceptionClass = exception.getClass();
            if (allowedClass.isAssignableFrom(exceptionClass)) {
                throw exception;
            }
        }
        throw new UndeclaredThrowableException(exception, method + " threw invalid exception");
    }

    @Override
    public final int hashCode() {
        return EqHc.hc(getHandle());
    }

    @Override
    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object == null) {
            return false;
        } else {
            AbstractHandler handle = EqHc.retyped(this, object);
            return handle != null && EqHc.eq(handle.getHandle(), handle);
        }
    }

    @Override
    public final String toString() {
        return ToString.of(this, getHandle());
    }

    protected abstract MethodCallResult invoke(MethodCall methodCall);

    public TargetHandle<?> getHandle() {
        return handle;
    }
}
