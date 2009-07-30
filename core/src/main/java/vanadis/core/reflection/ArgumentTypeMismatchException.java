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
package vanadis.core.reflection;

public class ArgumentTypeMismatchException extends InvokeException {

    private static final long serialVersionUID = -7891226002388323103L;

    public ArgumentTypeMismatchException(String msg) {
        super(msg);
    }

    public ArgumentTypeMismatchException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
