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

package vanadis.concurrent;

import java.util.concurrent.TimeoutException;

public class AsynchException extends RuntimeException {

    private static final long serialVersionUID = -7262865962978528745L;

    public AsynchException(String msg, InterruptedException timeout) {
        super(msg, timeout);

    }

    public AsynchException(String msg, TimeoutException timeout) {
        super(msg, timeout);
    }

}