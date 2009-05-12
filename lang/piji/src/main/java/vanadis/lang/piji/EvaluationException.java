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
 * An umbrella exception for all the bad things that may happen
 * during interpretation.  May contain a wrapped exception.
 *
 * @author Kjetil Valstadsve
 */
public class EvaluationException extends PijiException {

    private static final long serialVersionUID = -5845587246985627230L;

    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable t) {
        super(message, t);
    }

    public EvaluationException(Throwable t) {
        super(t.getMessage(), t);
    }

}

