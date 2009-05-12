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
public final class PrimitiveDoubleHolder extends PrimitiveNumberHolder {

    private static final long serialVersionUID = 3L;

    private final double doubleWrap;

    @Override
    public boolean isDouble() {
        return true;
    }

    private final Double doubleObject;

    @Override
    public Object getObject() {
        return this.doubleObject;
    }

    @Override
    public Class<?> getType() {
        return Double.TYPE;
    }

    @Override
    public double getDouble() {
        return this.doubleWrap;
    }

    public PrimitiveDoubleHolder() {
        this(0.0d);
    }

    public PrimitiveDoubleHolder(double d) {
        this.doubleWrap = d;
        this.doubleObject = d;
    }

    @Override
    public String toValueString() {
        return String.valueOf(this.doubleWrap);
    }

    @Override
    public int compareTo(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
                return 1;
            }
            return 0;
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            if (this.doubleWrap < hd) {
                return -1;
            }
            if (this.doubleWrap > hd) {
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
            return DataHolderFactory.holder(this.doubleWrap + hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.doubleWrap + hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.doubleWrap + hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.doubleWrap + hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.doubleWrap + hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.doubleWrap + hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder sub(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.doubleWrap - hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder div(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.doubleWrap / hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder mul(PrimitiveNumberHolder object) {
        if (object instanceof PrimitiveIntegerHolder) {
            PrimitiveIntegerHolder holder = (PrimitiveIntegerHolder) object;
            int hd = holder.getInt();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        } else if (object instanceof PrimitiveLongHolder) {
            PrimitiveLongHolder holder = (PrimitiveLongHolder) object;
            long hd = holder.getLong();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        } else if (object instanceof PrimitiveFloatHolder) {
            PrimitiveFloatHolder holder = (PrimitiveFloatHolder) object;
            float hd = holder.getFloat();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        } else if (object instanceof PrimitiveDoubleHolder) {
            PrimitiveDoubleHolder holder = (PrimitiveDoubleHolder) object;
            double hd = holder.getDouble();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        } else if (object instanceof PrimitiveByteHolder) {
            PrimitiveByteHolder holder = (PrimitiveByteHolder) object;
            byte hd = holder.getByte();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        } else if (object instanceof PrimitiveShortHolder) {
            PrimitiveShortHolder holder = (PrimitiveShortHolder) object;
            short hd = holder.getShort();
            return DataHolderFactory.holder(this.doubleWrap * hd);
        }
        throw new IllegalArgumentException
                (this + " incompatible with " + object);
    }

    @Override
    public PrimitiveNumberHolder mod(PrimitiveNumberHolder holder) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int toBits() {
        return (int) Double.doubleToLongBits(this.doubleWrap);
    }

}
