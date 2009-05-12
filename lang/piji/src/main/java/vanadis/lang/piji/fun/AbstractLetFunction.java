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

import vanadis.lang.piji.AbstractFunction;
import vanadis.lang.piji.Context;
import vanadis.lang.piji.Expression;

public abstract class AbstractLetFunction extends AbstractFunction {

    private final boolean evalProgressively;

    protected AbstractLetFunction(String docString, Context ctx,
                                  boolean evalProgressively) {
        super(docString, true, 2, ctx);
        this.evalProgressively = evalProgressively;
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Context letContext = new Context(context);
        this.fillContext(context, letContext,
                         checkList(args[1]), evalProgressively);
        Object value = null;
        for (int i = 2; i < args.length; i++) {
            value = args[i].evaluate(letContext);
        }
        return value;
    }

}

