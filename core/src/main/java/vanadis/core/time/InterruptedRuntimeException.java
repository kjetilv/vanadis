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

package vanadis.core.time;

public class InterruptedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 2900157738144087434L;

    public InterruptedRuntimeException(InterruptedException cause) {
        this(null, cause);
    }

    public InterruptedRuntimeException(String s) {
        this(s, null);
    }

    public InterruptedRuntimeException(String s, InterruptedException cause) {
        super(s, cause);
        Thread.currentThread().interrupt();
    }
}
