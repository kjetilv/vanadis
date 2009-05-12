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
 * @author Kjetil Valstadsve
 */
public final class PrimitiveCharacterHolder extends PrimitiveDataHolder
        implements Comparable<AbstractDataHolder> {

    private static final long serialVersionUID = 3L;

    private final char charWrap;

    @Override
    public boolean isCharacter() {
        return true;
    }

    @Override
    public boolean isChar() {
        return true;
    }

    @Override
    public Object getObject() {
        return this.charWrap;
    }

    @Override
    public Class<?> getType() {
        return Character.TYPE;
    }

    @Override
    public int compareTo(AbstractDataHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            if (this.charWrap < hd) {
                return -1;
            }
            if (this.charWrap > hd) {
                return 1;
            }
            return 0;
        }
        throw new ClassCastException(this + " incomparable to " + object +
                " (" + object.getClass() + ")");
    }

    @Override
    public char getChar() {
        return this.charWrap;
    }

    public PrimitiveCharacterHolder() {
        this('\0');
    }

    public PrimitiveCharacterHolder(char c) {
        this.charWrap = c;
    }

    @Override
    public String toValueString() {
        return String.valueOf(this.charWrap);
    }

}
