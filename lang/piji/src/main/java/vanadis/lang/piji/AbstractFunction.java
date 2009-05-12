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

package net.sf.vanadis.lang.piji;

public abstract class AbstractFunction implements Function {

    private final boolean vararg;

    @Override
    public boolean isVararg() {
        return this.vararg;
    }

    private final String documentation;

    @Override
    public String getDocumentationString() {
        return this.documentation;
    }

    private final int argumentCount;

    @Override
    public int getArgumentCount() {
        return this.argumentCount;
    }

    private final Context context;

    @Override
    public Context getContext() {
        return this.context;
    }

    protected AbstractFunction(String docString,
                               int argCount,
                               Context context) {
        this(docString, false, argCount, context);
    }

    protected AbstractFunction(String docString,
                               boolean vararg, int argCount,
                               Context context) {
        this.vararg = vararg;
        this.argumentCount = argCount;
        this.context = context;
        this.documentation = docString;
    }

    protected final void checkArgumentCount(Expression[] args)
            throws WrongArgumentCountException {
        int argl = args.length - 1;
        int argc = getArgumentCount();
        boolean vararg = this.isVararg();
        if ((vararg && argl < argc) || (!vararg && argl != argc)) {
            throw new WrongArgumentCountException(this, argl);
        }
    }

    protected static boolean isLeaf(Expression expr) {
        return ExpressionCheck.isLeaf(expr);
    }

    protected static boolean isSymbol(Expression expr) {
        return ExpressionCheck.isSymbol(expr);
    }

    static boolean isString(Expression expr) {
        return ExpressionCheck.isString(expr);
    }

    protected final String checkString(Expression expr, String msg)
            throws BadArgumentException {
        return ExpressionCheck.checkString(this, expr, msg);
    }

    protected final Symbol checkSymbol(Expression expr)
            throws BadArgumentException {
        return ExpressionCheck.checkSymbol(this, expr);
    }

    protected final Symbol checkSymbol(Expression expr, boolean fail)
            throws BadArgumentException {
        return ExpressionCheck.checkSymbol(this, expr, null, fail);
    }

    protected final ListNode checkList(Expression expr)
            throws BadArgumentException {
        return ExpressionCheck.checkList(this, expr);
    }

    protected final void fillContext(Context ctx, ListNode list)
            throws Throwable {
        this.fillContext(null, ctx, list, true);
    }

    protected final void fillContext(Context ctx, Context letCtx,
                                     ListNode list, boolean prog)
            throws Throwable {
        int pairCount = list.size();
        for (int i = 0; i < pairCount; i++) {
            ListNode pair = checkList(list.get(i));
            if (pair.size() != 2) {
                throw new BadArgumentException
                        (this + " got invalid let pair " + pair);
            }
            Symbol name = checkSymbol(pair.get(0));
            Object value = pair.get(1).evaluate(prog ? letCtx : ctx);
            letCtx.bind(name, value);
        }
    }

}
