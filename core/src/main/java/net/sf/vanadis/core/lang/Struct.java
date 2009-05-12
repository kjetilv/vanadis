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
package net.sf.vanadis.core.lang;

import java.util.Arrays;

public abstract class Struct {

    private final Object[] array;

    private final Object object;

    protected Struct(Object... array) {
        this(false, array);
    }

    protected Struct(boolean nonNull, Object... array) {
        if (VarArgs.present(array)) {
            boolean justOne = array.length == 1;
            this.array = justOne ? null : array;
            this.object = justOne ? array[0] : null;
        } else {
            throw new IllegalArgumentException("Expected varargs!");
        }
        if (nonNull) {
            for (int i = 0; i < this.array.length; i++) {
                Not.nil(this.array[i], "argument " + i);
            }
        }
    }

    protected final <T> T get(Class<T> type) {
        if (object == null) {
            throw new IllegalStateException(this + " contains " + array.length + " items");
        }
        return type.cast(object);
    }

    protected final <T> T get(Class<T> type, int i) {
        try {
            return type.cast(object == null ? array[i] : object);
        } catch (ClassCastException e) {
            throw new IllegalStateException(this + " did not contain object of " + type + " at #i", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException(this + " did not contain object #" + i + " of " + type, e);
        }
    }

    @Override
    public final boolean equals(Object obj) {
        Struct struct = EqHc.retyped(this, obj);
        return struct != null && EqHc.eqA(array, struct.array) && EqHc.eq(object, struct.object);
    }

    @Override
    public final int hashCode() {
        return EqHc.hcA(array);
    }

    @Override
    public String toString() {
        return ToString.of(this, Arrays.toString(array));
    }
}
