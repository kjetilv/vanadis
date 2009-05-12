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

package vanadis.lang.piji;

import vanadis.lang.piji.hold.DataHolderFactory;

import java.lang.reflect.Field;

public class Accessor extends ReflectorHelper {

    public Accessor(Reflector ref) {
        super(ref);
    }

    public final Object access(Object object, Field field)
        throws InternalException {
        if (field == null) {
            throw new NullPointerException
                (this + " got null field for object " + object);
        }
        boolean inaccessible = !field.isAccessible();
        if (inaccessible) {
            field.setAccessible(true);
        }
        try {
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive()) {
                if (fieldType == Integer.TYPE) {
                    return DataHolderFactory.holder(field.getInt(object));
                } else if (fieldType == Boolean.TYPE) {
                    return DataHolderFactory.holder(field.getBoolean(object));
                } else if (fieldType == Double.TYPE) {
                    return DataHolderFactory.holder(field.getDouble(object));
                } else if (fieldType == Short.TYPE) {
                    return DataHolderFactory.holder(field.getShort(object));
                } else if (fieldType == Float.TYPE) {
                    return DataHolderFactory.holder(field.getFloat(object));
                } else if (fieldType == Long.TYPE) {
                    return DataHolderFactory.holder(field.getLong(object));
                } else if (fieldType == Character.TYPE) {
                    return DataHolderFactory.holder(field.getChar(object));
                } else {
                    return DataHolderFactory.holder(field.getByte(object));
                }
            } else {
                return field.get(object);
            }
        } catch (IllegalAccessException e) {
            throw new InternalException
                ("Not allowed to access field " + field +
                 " in object " + object + " :\n" + e, e);
        } catch (IllegalArgumentException e) {
            throw new InternalException
                ("Found no field " + field + " defined for object " +
                 object + " :\n" + e, e);
        } catch (NullPointerException e) {
            throw new InternalException
                ("Got null object, and field " + field +
                 " not static in " + object + " :\n" + e, e);
        } catch (ExceptionInInitializerError e) {
            throw new InternalException
                ("Got initialization failed for field " + field +
                 " in " + object + " :\n" + e, e);
        } finally {
            if (inaccessible) {
                field.setAccessible(false);
            }
        }
    }

    private Object access(Object object, String fieldName)
        throws Throwable {
        try {
            Field field = getRef().getFieldFinder().getField
                (Reflector.resolveType(object), fieldName);
            return access(object, field);
        } catch (SecurityException e) {
            throw new InternalException
                ("Got security violation accessing field " +
                 fieldName + " in " + object, e);
        }
    }

    public Object access(Context context, Expression[] args)
        throws Throwable {
        return access(context, args, 0);
    }

    public Object access(Context context, Expression[] args, int offset)
        throws Throwable {
        Object object = getRef().resolveTarget(context, args[offset]);
        String name = Reflector.getName(args[offset + 1], context);

        try {
            return access(object, name);
        } catch (RuntimeException e) {
            throw new InternalRuntimeException
                ("Got " + e + " when accessing " + name +
                 " on " + object + " in context " + context, e);
        }
    }

}
