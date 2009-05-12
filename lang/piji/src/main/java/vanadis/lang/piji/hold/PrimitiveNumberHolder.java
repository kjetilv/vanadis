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

package net.sf.vanadis.lang.piji.hold;

/**
 * <P>A primitive number holder.</P>
 *
 * @author Kjetil Valstadsve
 */
public abstract class PrimitiveNumberHolder extends AbstractDataHolder
        implements PrimitiveHolder, Comparable<PrimitiveNumberHolder> {

    private final int hashBase;

    private static final long serialVersionUID = -2660411478003960971L;

    PrimitiveNumberHolder() {
        this.hashBase = 17 * 37 * getClass().hashCode();
    }

    public boolean isInteger() {
        return false;
    }

    public boolean isLong() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isByte() {
        return false;
    }

    public boolean isShort() {
        return false;
    }

    private Number getNumber() {
        try {
            return (Number) getObject();
        } catch (ClassCastException e) {
            if (getObject() instanceof Number) {
                throw e;
            } else {
                throw new IllegalStateException
                        (this + " had a non-number object " + getObject() +
                                ", got " + e, e);
            }
        }
    }

    public int getInt() {
        return getNumber().intValue();
    }

    public long getLong() {
        return getNumber().longValue();
    }

    public float getFloat() {
        return getNumber().floatValue();
    }

    public double getDouble() {
        return getNumber().doubleValue();
    }

    public byte getByte() {
        return getNumber().byteValue();
    }

    public short getShort() {
        return getNumber().shortValue();
    }

    protected abstract int toBits();

    @Override
    public final int hashCode() {
        return hashBase + 19 * toBits();
    }

    @Override
    public final boolean equals(Object object) {
        return this == object ||
                (object instanceof PrimitiveNumberHolder &&
                        this.compareTo((PrimitiveNumberHolder) object) == 0);
    }

    public abstract PrimitiveNumberHolder add(PrimitiveNumberHolder holder);

    public abstract PrimitiveNumberHolder sub(PrimitiveNumberHolder holder);

    public abstract PrimitiveNumberHolder div(PrimitiveNumberHolder holder);

    public abstract PrimitiveNumberHolder mul(PrimitiveNumberHolder holder);

    public abstract PrimitiveNumberHolder mod(PrimitiveNumberHolder holder);

}
