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
package vanadis.osgi;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;

import java.util.Arrays;
import java.util.Collection;

final class ObjectClassesFilter extends AbstractFilter {

    private static final long serialVersionUID = 757833146280705986L;

    private static String[] toArray(Collection<String> objectClasses) {
        Not.nil(objectClasses, "objectClasses");
        if (objectClasses.isEmpty()) {
            throw new IllegalArgumentException("No object classes!");
        }
        return objectClasses.toArray(new String[objectClasses.size()]);
    }

    private final String[] objectClasses;

    private final String[] sortedObjectClasses;

    private final boolean multi;

    ObjectClassesFilter(Collection<String> objectClasses) {
        this(toArray(objectClasses));
    }

    ObjectClassesFilter(String[] objectClasses) {
        this.objectClasses = Not.emptyVarArgs(objectClasses, "object classes");
        int length = objectClasses.length;
        this.multi = length > 1;
        this.sortedObjectClasses = this.objectClasses.clone();
        Arrays.sort(this.sortedObjectClasses);
    }

    ObjectClassesFilter(Class<?>[] objectClasses) {
        this(classNames(Not.emptyVarArgs(objectClasses, "object classes")));
    }

    private static String[] classNames(Class<?>[] objectClasses) {
        int length = objectClasses.length;
        String[] classNames = new String[length];
        for (int i = 0; i < length; i++) {
            Class<?> objectClass = objectClasses[i];
            Not.nil(objectClass, "objectclass " + i + " was null");
            classNames[i] = objectClass.getName();
        }
        return classNames;
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        builder.append("objectclass=");
        if (multi) {
            builder.append("[");
        }
        for (int i = 0; i < objectClasses.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(objectClasses[i]);
        }
        if (multi) {
            builder.append("]");
        }
        return builder;
    }

    @Override
    protected Object[] hashBase() {
        return objectClasses;
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        ObjectClassesFilter oce = (ObjectClassesFilter) expr;
        return EqHc.eq(oce.objectClasses, objectClasses);
    }

    @Override
    public boolean matches(ServiceProperties<?> properties) {
        for (String objectClass : properties.getObjectClasses()) {
            int index = Arrays.binarySearch(this.sortedObjectClasses, objectClass);
            if (index < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTyped() {
        return true;
    }
}
