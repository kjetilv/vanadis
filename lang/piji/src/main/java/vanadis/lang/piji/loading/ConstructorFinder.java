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

package vanadis.lang.piji.loading;

import java.lang.reflect.Constructor;

public class ConstructorFinder extends AccessibleFinder {

    public ConstructorFinder() {
        this(false);
    }

    public ConstructorFinder(boolean privates) {
        super(privates);
    }

    public final Constructor<?> getConstructor(Class<?> type, Class<?>[] signature)
        throws Throwable {
        return getConstructor(type, signature, false);
    }

    final Constructor<?> getConstructor(Class<?> type, Class<?>[] signature,
                                        boolean allowFail) throws Throwable {
        Constructor<?> foundConstructor;

        try {
            foundConstructor = type.getConstructor(signature);
        } catch (SecurityException e) {
            throw new LoadingException("Got security violation looking for constructor " +
                                       signatureString(signature) + " in class " + type, e);
        } catch (NoSuchMethodException ignore) {
            foundConstructor = findByMatching(type, signature);
        }

        if (foundConstructor != null) {
            return (Constructor<?>) checkMember(type, foundConstructor);
        }

        if (allowFail) {
            return null;
        }
        throw new LoadingException("Could not find constructor " + signatureString(signature) +
                                   " in " + type);
    }

    final Constructor<?>[] getConstructors(Class<?> type) {
        return isPrivates()
            ? type.getDeclaredConstructors()
            : type.getConstructors();
    }

    private Constructor<?> findByMatching(Class<?> type, Class<?>[] signature) {
        Constructor<?> foundConstructor = null;
        Class<?>[] foundTypes = null;
        Constructor<?>[] constructors = getConstructors(type);
        if (constructors == null || constructors.length == 0) {
            return null;
        }
        for (Constructor<?> constructor : constructors) {
            Class<?>[] candidateTypes = constructor.getParameterTypes();
            if (candidateTypes.length == signature.length &&
                fitsBetween(foundTypes, candidateTypes, signature)) {
                foundConstructor = constructor;
                foundTypes = candidateTypes;
            }
        }
        return foundConstructor;
    }

    @Override
    public String toString() {
        return "ConstructorFinder";
    }

}
