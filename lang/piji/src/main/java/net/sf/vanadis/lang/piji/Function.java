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

/**
 * A common interface for scheme functions
 *
 * @author Kjetil Valstadsve
 */
public interface Function {

    /**
     * Returns true iff this function is vararg.
     *
     * @return True iff this function is vararg
     */
    boolean isVararg();

    /**
     * Gets the minimum number of required arguments
     *
     * @return The number of required arguments
     */
    int getArgumentCount();

    /**
     * Returns a documentation string
     *
     * @return A documentation string
     */
    String getDocumentationString();

    /**
     * Gets the definition context
     *
     * @return The definition conetxt
     */
    Context getContext();

    /**
     * Applies the function to a set of values in a given context
     * and returns the result
     *
     * @param context The context to apply this function in
     * @param nodes   Expressions
     * @return The result of the function applied to the arguments in
     *         the context
     * @throws Throwable Anything
     */
    Object apply(Context context, Expression[] nodes)
            throws Throwable;
}


