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

package vanadis.lang.piji.loading;

import vanadis.core.collections.Generic;

import java.lang.reflect.Method;
import java.util.List;

public class MethodFinder extends AccessibleFinder {

    public MethodFinder() {
        this(false);
    }

    public MethodFinder(boolean privates) {
        super(privates);
    }

    public final Method getMethod(Object object,
                                  String methodName,
                                  Class<?>[] signature,
                                  boolean allowFail)
            throws Throwable {
        Class<?> type = resolveType(object);
        return getMethod(type, methodName, signature,
                         allowFail);
    }

    public final Method getMethod(Class<?> type,
                                  String methodName,
                                  Class<?>[] signature,
                                  boolean allowFail) throws LoadingException {
        Method foundMethod;
        try {
            foundMethod = findByMatching(type, methodName, signature);
        } catch (SecurityException e) {
            throw new LoadingException("Got " + e + " looking for method " +
                    signatureString(signature) + " in class " + type, e);
        }
        if (foundMethod == null && !type.equals(Class.class)) {
            foundMethod = this.getMethod(Class.class, methodName, signature, true);
        }

        if (foundMethod != null) {
            return (Method) checkMember(type, foundMethod);
        }

        if (allowFail) {
            return null;
        }
        throw new LoadingException("Found no method \"" + methodName +
                "\" w/ args " + signatureString(signature) +
                " in " + type);
    }

    public Method[] getMethods(Class<?> type) {
        return findCandidates(type);
    }

    private Method[] findCandidates(Class<?> type) {
        if (isPrivates()) {
            List<Method[]> methodses = Generic.list();
            int methodCount = 0;
            for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
                Method[] declared = clazz.getDeclaredMethods();
                if (declared != null && declared.length > 0) {
                    methodses.add(declared);
                    methodCount += declared.length;
                }
            }
            Method[] methods = new Method[methodCount];
            int methodsIndex = 0;
            for (Method[] declared : methodses) {
                System.arraycopy(declared, 0, methods, methodsIndex, declared.length);
                methodsIndex += declared.length;
            }
            return methods;
        } else {
            return type.getMethods();
        }
    }

    private Method findByMatching(Class<?> type,
                                  String methodName,
                                  Class<?>[] signature) {
        Method foundMethod = null;
        Class<?>[] foundTypes = null;
        Method[] methods = findCandidates(type);
        for (Method candidate : methods) {
            if (candidate.getName().equals(methodName)) {
                Class<?>[] candidateTypes = candidate.getParameterTypes();
                if (candidateTypes.length == signature.length
                        && fitsBetween(foundTypes, candidateTypes, signature)) {

                    foundMethod = candidate;
                    foundTypes = candidateTypes;
                }
            }
        }
        if (foundMethod != null && isPrivates()) {
            foundMethod.setAccessible(true);
        }
        return foundMethod;
    }

    public static Method getMethod(Class<?> type,
                                   String methodName,
                                   int argCount,
                                   boolean allowFail) throws Throwable {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)
                    && method.getParameterTypes().length == argCount) {

                return method;
            }
        }
        if (allowFail) {
            return null;
        } else {
            throw new LoadingException("Found no " + methodName + "/" + argCount + " in " + type);
        }
    }

    @Override
    public String toString() {
        return "MethodFinder";
    }

}
