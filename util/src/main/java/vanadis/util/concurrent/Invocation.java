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

package vanadis.util.concurrent;

import vanadis.core.lang.AccessibleHelper;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

class Invocation implements Runnable, Callable<Object> {

    private static final Log log = Logs.get(Invocation.class);

    private final Method method;

    private final Object[] args;

    private final Object target;

    private static final int ARGS_TOSTRING_MAX_LENGTH = 80;

    private static final String ARGS_ELLIPSIS = "...";

    private static final Object[] NO_ARGS = new Object[]{};

    private static String ellipsed(String argsString) {
        return argsString.substring
                (0, ARGS_TOSTRING_MAX_LENGTH - ARGS_ELLIPSIS.length()) + ARGS_ELLIPSIS;
    }

    Invocation(Object target, Method method, Object[] args) {
        this.target = Not.nil(target, "target");
        this.args = args == null ? NO_ARGS : args;
        this.method = Not.nil(method, "method");
        if (!method.isAccessible()) {
            AccessibleHelper.openSesame(method);
        }
    }

    private String argsString() {
        String argsString = Arrays.toString(args);
        return argsString.length() > ARGS_TOSTRING_MAX_LENGTH
                ? ellipsed(argsString)
                : argsString;
    }

    protected Method getMethod() {
        return method;
    }

    protected Object[] getArgs() {
        return args;
    }

    protected Object getTarget() {
        return target;
    }

    protected Object execute() {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access " + getMethod(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(getMethod().getName() + " failed (" + getMethod() + ")", e);
        }
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Throwable e) {
            log.error(this + " failed to execute", e);
        }
    }

    @Override
    public Object call() {
        return execute();
    }

    @Override
    public String toString() {
        String reasonableArgsString = argsString();
        return ToString.of
                (this, target, "method", method, "args", reasonableArgsString);
    }
}
