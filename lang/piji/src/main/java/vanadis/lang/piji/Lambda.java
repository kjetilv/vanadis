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

import vanadis.core.collections.Generic;

import java.util.Arrays;
import java.util.List;

public final class Lambda extends AbstractFunction {

    private static final Symbol POINT = Symbol.get(".");

    public static Lambda create(Context context, Expression[] expr,
                                int offset, int formalsOffset)
        throws BadArgumentException {
        Symbol[] formalNames = formalNames(expr[offset], formalsOffset);

        int nameCount = formalNames.length;
        boolean vararg = nameCount > 1 &&
            formalNames[nameCount - 2].equals(POINT);
        int formalCount = vararg ? nameCount - 1 : nameCount;

        String docString;
        int bodyOffset;

        if (isString(expr[offset + 1])) {
            bodyOffset = offset + 2;
            docString = ExpressionCheck.checkString
                (Lambda.class, expr[offset + 1]);
        } else {
            bodyOffset = offset + 1;
            docString = "<lambda>";
        }

        return new Lambda(formalNames, vararg, formalCount,
                          docString, expr, bodyOffset,
                          context);
    }

    private static Symbol[] formalNames(Expression expr, int offset)
        throws BadArgumentException {
        ListNode formals = ExpressionCheck.checkList
            (Lambda.class, expr, "Formals must be list of symbols");
        Symbol[] formalNames = new Symbol[formals.size() - offset];
        int length = formalNames.length;
        for (int i = 0; i < length; i++) {
            formalNames[i] = ExpressionCheck.checkSymbol
                (Lambda.class,
                 formals.get(i + offset), "Non-symbol value");
        }
        return formalNames;
    }

    private final Symbol[] formals;

    private final Expression[] body;

    private final int bodyOffset;

    private Lambda(Symbol[] formals, boolean vararg, int formalCount,
                   String docString, Expression[] body, int bodyOffset,
                   Context context) {
        super(docString, vararg, formalCount, context);
        this.body = body;
        this.bodyOffset = bodyOffset;
        this.formals = formals;
    }

    @Override
    public int getArgumentCount() {
        return this.isVararg()
            ? super.getArgumentCount() - 1
            : super.getArgumentCount();
    }

    @Override
    public Object apply(Context context, Expression[] args)
        throws Throwable {
        checkArgumentCount(args);

        Context applyContext = new Context(getContext());

        for (int i = 0; i < getArgumentCount(); i++) {
            Object value = args[i + 1].evaluate(context);
            applyContext.bind(this.formals[i], value);
        }

        if (this.isVararg()) {
            int lastPosition = getArgumentCount() + 1;
            List<Object> list = Generic.list();
            for (int i = lastPosition; i < args.length; i++) {
                list.add(args[i].evaluate(context));
            }
            applyContext.bind(formals[lastPosition], list);
        }
        Object value = null;
        for (int i = this.bodyOffset; i < this.body.length; i++) {
            value = this.body[i].evaluate(applyContext);
        }
        return value;
    }

    @Override
    public String toString() {
        return "Lambda[" + Arrays.toString(this.formals) + "]";
    }

}
