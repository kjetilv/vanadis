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

@EntryPoint("Reflection")
public final class LessThanEqualFunction extends AbstractCompareFunction {

    @EntryPoint("Reflection")
    public LessThanEqualFunction(Context ctx) {
        super("Comparably less than or equal to", ctx);
    }

    @Override
    protected boolean compareFailed(Object last, Object value) {
        return cmp(last, value) > 0;
    }

}
