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

package net.sf.vanadis.lang.piji.hold;

import net.sf.vanadis.core.collections.Generic;

import java.util.Map;

public final class DataHolderFactory {

    private static final Map<Class<?>, Wrapper> prims = Generic.map();

    static {
        prims.put(Integer.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Integer) obj).intValue());
            }
        });
        prims.put(Long.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Long) obj).longValue());
            }
        });
        prims.put(Float.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Float) obj).floatValue());
            }
        });
        prims.put(Double.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder((Double) obj);
            }
        });
        prims.put(Boolean.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder((Boolean) obj);
            }
        });
        prims.put(Short.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Short) obj).shortValue());
            }
        });
        prims.put(Byte.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Byte) obj).byteValue());
            }
        });
        prims.put(Character.TYPE, new Wrapper() {
            @Override
            public AbstractDataHolder wrap(Object obj) {
                return holder(((Character) obj).charValue());
            }
        });
    }

    private interface Wrapper {

        AbstractDataHolder wrap(Object obj);

    }

    private DataHolderFactory() {
        // Hee hee hee
    }

    public static PrimitiveBooleanHolder holder(boolean truthValue) {
        return truthValue
                ? PrimitiveBooleanHolder.TRUE
                : PrimitiveBooleanHolder.FALSE;
    }

    public static Object drop(Object object) {
        Object walker = object;
        while (walker instanceof Holder) {
            walker = ((Holder) walker).getObject();
        }
        return walker;
    }

    public static PrimitiveIntegerHolder holder(int x) {
        return new PrimitiveIntegerHolder(x);
    }

    public static PrimitiveLongHolder holder(long x) {
        return new PrimitiveLongHolder(x);
    }

    public static PrimitiveByteHolder holder(byte x) {
        return new PrimitiveByteHolder(x);
    }

    public static PrimitiveCharacterHolder holder(char x) {
        return new PrimitiveCharacterHolder(x);
    }

    public static PrimitiveNumberHolder holder(short x) {
        return new PrimitiveShortHolder(x);
    }

    public static PrimitiveFloatHolder holder(float x) {
        return new PrimitiveFloatHolder(x);
    }

    public static PrimitiveDoubleHolder holder(double x) {
        return new PrimitiveDoubleHolder(x);
    }

    public static AbstractDataHolder primitiveHolder(Object object,
                                                     Class<?> type) {
        if (!type.isPrimitive()) {
            throw new IllegalStateException
                    ("Not a primitive type: " + type +
                            ", tried to wrap " + object);
        }
        Wrapper wrapper = prims.get(type);
        return wrapper.wrap(object);
    }

    public static ReferenceHolder holder(Object object, Class<?> type) {
        return object == null
                ? null
                : new ReferenceHolder(object, type);
    }

}
