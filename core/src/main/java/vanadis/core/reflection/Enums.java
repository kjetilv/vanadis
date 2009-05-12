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

package net.sf.vanadis.core.reflection;

import net.sf.vanadis.core.lang.Not;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Enums {

    public static <E extends Enum<?>> E getEnum(Class<E> type, Object fieldRef) {
        return type.cast(get(type, fieldRef));
    }

    public static Object get(Class<?> type, Object fieldRef) {
        Not.nil(fieldRef, "field name");
        String fieldName = String.valueOf(fieldRef);
        if (!Modifier.isPublic(type.getModifiers())) {
            throw new IllegalArgumentException("Class " + type + " was not public, cannot access " + fieldRef);
        }
        Field field;
        try {
            field = type.getField(fieldName);
        } catch (NoSuchFieldException e) {
            field = lookAgain(type, fieldName);
            if (field == null) {
                throw new IllegalArgumentException("No field " + fieldRef + " in " + type, e);
            }
        }
        int mod = field.getModifiers();
        if (!(Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod))) {
            throw new IllegalArgumentException("Field " + field + " must be public static final");
        }
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(field + " was not accessible!", e);
        }
    }

    private static Field lookAgain(Class<?> type, String fieldName) {
        for (Class<?> walker = type; walker != Object.class; walker = walker.getSuperclass()) {
            for (Field field : walker.getFields()) {
                if (field.getName().equalsIgnoreCase(fieldName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private Enums() { }
}
