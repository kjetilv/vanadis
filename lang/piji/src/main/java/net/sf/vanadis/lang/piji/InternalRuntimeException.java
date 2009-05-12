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

package net.sf.vanadis.lang.piji;

/**
 * A runtime exception subclass for signalling <EM>to the developer</EM>
 * that an error must be corrected.  Should not be used to signal runtime
 * errors attributable to user behaviour, but rather system discrepancies.
 *
 * @author Kjetil Valstadsve
 */
public class InternalRuntimeException extends PijiRuntimeException {

    private static final long serialVersionUID = -700250451036239124L;

    public InternalRuntimeException(String message) {
        this(message, null);
    }

    public InternalRuntimeException(String message, Throwable t) {
        super(message, t);
    }

    public InternalRuntimeException(Throwable t) {
        this(t.getMessage(), t);
    }

}

