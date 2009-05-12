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

/**
 * Exception signalling a mismatch between the desired properties of a
 * formal argument and the properties of an actual argument, eg. "this
 * argument must be a list", or "first argument must be function".
 *
 * @author Kjetil Valstadsve
 */
public class BadArgumentException extends EvaluationException {

    private static final long serialVersionUID = -7523277285836022983L;

    public BadArgumentException(String message) {
        super(message);
    }

    public BadArgumentException
        (Function function, Expression node, String message) {
        super(function + ": Error evaluating argument " + node +
              ": " + message);
    }

}
