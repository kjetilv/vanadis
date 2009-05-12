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

import vanadis.core.lang.ContextClassLoaderSwitch;
import vanadis.core.lang.ToString;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class MethodCallResult extends AbstractWireSessionable {

    private static final long serialVersionUID = 1106575680086259524L;

    private static final ThreadLocal<ClassLoader> threadLocalClassLoader = new ThreadLocal<ClassLoader>();

    private Object value;

    private Throwable exception;

    protected MethodCallResult() { }

    protected MethodCallResult(Session session, Object value, Throwable exception) {
        super(session);
        this.value = value;
        this.exception = exception;
    }

    public static void setThreadLocalClassLoader(ClassLoader classLoader) {
        threadLocalClassLoader.set(classLoader);
    }

    public boolean isReturnedNormally() {
        return isSessioned() && exception == null;
    }

    public boolean isTargetFound() {
        return isSessioned();
    }

    public final Object getValue() {
        return value;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput)
        throws IOException {
        super.writeExternal(objectOutput);
        objectOutput.writeObject(value);
        objectOutput.writeObject(exception);
    }

    @Override
    public void readExternal(ObjectInput objectInput)
        throws IOException, ClassNotFoundException {
        super.readExternal(objectInput);
        ContextClassLoaderSwitch classLoaderSwitch = new ContextClassLoaderSwitch(threadLocalClassLoader.get());
        try {
            this.value = objectInput.readObject();
            this.exception = (Throwable)objectInput.readObject();
        } finally {
            classLoaderSwitch.revert();
        }
    }

    @Override
    public String toString() {
        return isReturnedNormally() ? ToString.of(this, value)
               : ToString.of(this, exception);
    }

}
