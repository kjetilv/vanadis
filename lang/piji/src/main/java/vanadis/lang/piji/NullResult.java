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

import vanadis.lang.piji.hold.Holder;

public final class NullResult extends Result {

    private static final long serialVersionUID = 3L;

    NullResult(Object expr) {
        super(expr, null, false, null, 0);
    }

    @Override
    public final Holder getWrappedValue() {
        return null;
    }

    @Override
    public final boolean isWrapped() {
        return false;
    }

    @Override
    public final Object getValue() {
        return null;
    }

    @Override
    public final Object getValueString() {
        return null;
    }

    @Override
    public final Object getTypedValue() {
        return null;
    }

    @Override
    public final Class<?> getType() {
        return null;
    }

    @Override
    public final String getTypeName() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public String getThrowableType() {
        return null;
    }

    @Override
    public String getThrowableMessage() {
        return null;
    }

    @Override
    public final long getEvalTime() {
        return 0;
    }

    @Override
    public final boolean isOK() {
        return true;
    }

}
