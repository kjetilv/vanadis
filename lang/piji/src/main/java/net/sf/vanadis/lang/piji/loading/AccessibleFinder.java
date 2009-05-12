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

package net.sf.vanadis.lang.piji.loading;

import net.sf.vanadis.lang.piji.hold.Holder;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class AccessibleFinder {

    private final boolean privates;

    AccessibleFinder() {
        this(false);
    }

    AccessibleFinder(boolean privates) {
        this.privates = privates;
    }

    static Class<?> resolveType(Object object) {
        return object == null
                ? null
                : (object instanceof Class
                        ? (Class<?>) object
                        : (object instanceof Holder
                                ? ((Holder) object).getType()
                                : object.getClass()));
    }

    static String signatureString(Class<?>[] signature) {
        int argCount = signature.length;
        StringBuffer argTypeString = new StringBuffer("[");
        for (int i = 0; i < argCount; i++) {
            argTypeString.append(String.valueOf(signature[i]));
            if (i < argCount - 1) {
                argTypeString.append(" ");
            }
        }
        argTypeString.append("]");
        return argTypeString.toString();
    }

    static boolean fitsBetween(Class<?>[] championTypes,
                                     Class<?>[] candidateTypes,
                                     Class<?>[] signature) {
        if (championTypes == null) {
            for (int i = 0; i < signature.length; i++) {
                if (!isSuperclass(candidateTypes[i], signature[i])) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < signature.length; i++) {
                if (!(isSuperclass(championTypes[i], candidateTypes[i]) &&
                        isSuperclass(candidateTypes[i], signature[i]))) {
                    return false;
                }
            }
        }
        return true;
    }

    Member checkMember(Class<?> clazz, Member member) throws LoadingException {
        if (Modifier.isPrivate(member.getModifiers())) {
            if (!isPrivates()) {
                throw new PrivateMemberException(clazz, member);
            }
        }
        return member;
    }

    static boolean isSuperclass(Class<?> supr, Class<?> sub) {
        return sub == null || supr.isAssignableFrom(sub);
    }

    boolean isPrivates() {
        return this.privates;
    }
}
