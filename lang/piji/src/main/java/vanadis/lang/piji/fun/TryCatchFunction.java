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

package net.sf.vanadis.lang.piji.fun;

import net.sf.vanadis.core.lang.EntryPoint;
import net.sf.vanadis.lang.piji.*;

@EntryPoint("Reflection")
public final class TryCatchFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public TryCatchFunction(Context ctx) {
        super("(<expression>*)\n" +
                "  ((<exception type> <exception formal> <expression>*)*)",
              2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args) throws Throwable {
        checkArgumentCount(args);
        Throwable throwable;
        try {
            ListNode body = checkList(args[1]);
            Object value = null;
            int bodySize = body.size();
            for (int i = 0; i < bodySize; i++) {
                value = body.get(i).evaluate(context);
            }
            return value;
        } catch (Throwable e) {
            Throwable t = e;
            while (t instanceof EvaluationException && t.getCause() != null) {
                t = t.getCause();
            }
            throwable = t;
        }

        ListNode clauses = checkList(args[2]);
        int clausesSize = clauses.size();

        for (int i = 0; i < clausesSize; i++) {
            ListNode clause = checkList(clauses.get(i));
            Class<?> exceptionClass =
                    Reflector.getClass(clause.get(0), context);

            if (exceptionClass.isAssignableFrom(throwable.getClass())) {

                Symbol formal = checkSymbol(clause.get(1));
                Context exceptionContext = new Context(context);
                exceptionContext.bind(formal, throwable);

                int clauseSize = clause.size();
                Object value = null;
                for (int j = 2; j < clauseSize; j++) {
                    value = clause.get(j).evaluate(exceptionContext);
                }
                return value;
            }
        }
        throw throwable;
    }

}

