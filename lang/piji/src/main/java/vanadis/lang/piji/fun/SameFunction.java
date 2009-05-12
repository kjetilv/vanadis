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

package vanadis.lang.piji.fun;

import vanadis.core.lang.EntryPoint;
import vanadis.lang.piji.Context;
import vanadis.lang.piji.hold.PrimitiveHolder;

@EntryPoint("Reflection")
public class SameFunction extends EqualsFunction {

    @EntryPoint("Reflection")
    public SameFunction(Context ctx) {
        super("Object == equality", ctx);
    }

    @Override
    protected boolean compareFailed(Object last, Object value) {
        if (value instanceof PrimitiveHolder) {
            return super.compareFailed(last, value);
        }
        return last != value;
    }

}
