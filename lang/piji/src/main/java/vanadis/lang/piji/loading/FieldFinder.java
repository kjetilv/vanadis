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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldFinder extends AccessibleFinder {

    public FieldFinder() {
        this(false);
    }

    public FieldFinder(boolean privates) {
        super(privates);
    }

    public final Field getField(Object target, String name)
            throws LoadingException {
        return getField(target, name, false);
    }

    public static Field[] getFields(Class<?> type) {
        List<Field> fields = Generic.list();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public final Field getField(Object target, String name, boolean allowFail)
            throws LoadingException {
        Field field = null;
        Class<?> type = resolveType(target);
        if (isPrivates()) {
            for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                int length = fields.length;
                for (int i = 0; i < length; i++) {
                    if (fields[i].getName().equals(name)) {
                        return (Field) checkMember(clazz, fields[i]);
                    }
                }
            }
        } else {
            try {
                field = type.getField(name);
            } catch (NoSuchFieldException e) {
                if (allowFail) {
                    return null;
                } else {
                    throw new LoadingException("Found no field " + name + " in " + type +
                            ", got " + e, e);
                }
            } catch (Exception e) {
                throw new LoadingException
                        ("Got " + e + " when getting field " + name +
                                " in " + target + "(" + type + ")", e);
            }
        }
        if (field != null) {
            return (Field) checkMember(type, field);
        }
        if (allowFail) {
            return null;
        }
        throw new LoadingException
                ("Found no field " + name + " in " + target + "(" + type + ")");
    }

}
