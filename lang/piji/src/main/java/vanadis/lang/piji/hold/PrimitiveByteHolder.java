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

package vanadis.lang.piji.hold;

/**
 * @author Kjetil Valstadsve
 */
public final class PrimitiveByteHolder extends PrimitiveNumberHolder {

    private static final long serialVersionUID = 3L;

    private final byte byteWrap;

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isShort() {
        return true;
    }

    @Override
    public boolean isByte() {
        return true;
    }

    private final Byte byteObject;

    @Override
    public Object getObject() {
        return this.byteObject;
    }

    @Override
    public Class<?> getType() {
        return Byte.TYPE;
    }

    /*    public void setByte(byte i) {
          this.byteWrap = byteWrap;
          }*/

    @Override
    public byte getByte() {
        return this.byteWrap;
    }

    public PrimitiveByteHolder() {
        this((byte) 0);
    }

    public PrimitiveByteHolder(byte b) {
        this.byteWrap = b;
        this.byteObject = b;
    }

    @Override
    public String toValueString() {
        return String.valueOf(this.byteWrap);
    }

    @Override
    public int compareTo(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            if (this.byteWrap < hd) {
                return -1;
            }
            if (this.byteWrap > hd) {
                return 1;
            }
            return 0;
        }
        throw new ClassCastException(this + " incomparable to " + object +
                " (" + object.getClass() + ")");
    }

    @Override
    public PrimitiveNumberHolder add(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.byteWrap + hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.byteWrap + hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.byteWrap + hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.byteWrap + hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.byteWrap + hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.byteWrap + hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder sub(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.byteWrap - hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.byteWrap - hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.byteWrap - hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.byteWrap - hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.byteWrap - hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.byteWrap - hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder div(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.byteWrap / hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.byteWrap / hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.byteWrap / hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.byteWrap / hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.byteWrap / hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.byteWrap / hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder mul(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.byteWrap * hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.byteWrap * hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.byteWrap * hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.byteWrap * hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.byteWrap * hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.byteWrap * hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder mod(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.byteWrap % hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.byteWrap % hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.byteWrap % hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.byteWrap % hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    protected int toBits() {
        return this.byteWrap;
    }

}
