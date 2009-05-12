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
public final class PrimitiveBooleanHolder extends PrimitiveDataHolder {

    private static final long serialVersionUID = 3L;

    static final PrimitiveBooleanHolder TRUE =
            new PrimitiveBooleanHolder(true);

    static final PrimitiveBooleanHolder FALSE =
            new PrimitiveBooleanHolder(false);

    private final boolean booleanWrap;

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public Object getObject() {
        return this.booleanWrap ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Class<?> getType() {
        return Boolean.TYPE;
    }

    @Override
    public boolean getBoolean() {
        return this.booleanWrap;
    }

    private PrimitiveBooleanHolder(boolean bool) {
        this.booleanWrap = bool;
    }

    @Override
    public String toValueString() {
        return String.valueOf(this.booleanWrap);
    }

}
