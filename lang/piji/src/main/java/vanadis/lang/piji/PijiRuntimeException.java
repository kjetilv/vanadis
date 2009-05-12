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

package vanadis.lang.piji;

public class PijiRuntimeException extends RuntimeException {

    private final Throwable cause;

    private static final long serialVersionUID = -7091198069625887741L;

    public PijiRuntimeException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    public PijiRuntimeException(String msg) {
        this(msg, null);
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

}
