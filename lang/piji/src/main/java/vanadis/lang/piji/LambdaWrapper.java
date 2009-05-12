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

public abstract class LambdaWrapper implements Function {

    public Object apply(Context context, Expression[] nodes, int off)
        throws Throwable {
        if (off == 1) {
            return apply(context, nodes);
        }
        throw new RuntimeException("Not implemented");
    }

    private final Lambda lambda;

    LambdaWrapper(Lambda lambda) {
        this.lambda = lambda;
    }

    protected Lambda getLambda() {
        return this.lambda;
    }

    @Override
    public boolean isVararg() {
        return this.lambda.isVararg();
    }

    @Override
    public int getArgumentCount() {
        return this.lambda.getArgumentCount();
    }

    @Override
    public String getDocumentationString() {
        return this.lambda.getDocumentationString();
    }

    @Override
    public Context getContext() {
        return this.lambda.getContext();
    }

}
