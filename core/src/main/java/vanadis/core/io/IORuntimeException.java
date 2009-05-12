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

package vanadis.core.io;

import java.io.IOException;

/**
 * A sensible, unchecked I/O exception.  Wraps the original IOException.
 */
public class IORuntimeException extends RuntimeException {

    private static final long serialVersionUID = -3235960585116007400L;

    public IORuntimeException(IOException e) {
        super(e);
    }

    public IORuntimeException(String msg, IOException e) {
        super(msg, e);
    }

    /**
     * The cause, with type narrowed to IOException.
     *
     * @return Cause
     */
    @Override
    public IOException getCause() {
        return (IOException)super.getCause();
    }
}
