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
 * Exception for signalling error conditions during a parse
 *
 * @author Kjetil Valstadsve
 */
class ParseUnfinishedException extends ParseException {

    private static final long serialVersionUID = -2889982274046132641L;

    /**
     * Error info with line number
     *
     * @param line    The line number
     * @param message The info
     */
    ParseUnfinishedException(int line, String message) {
        this(message + " @ line " + line);
    }

    /**
     * Error info
     *
     * @param message The info
     */
    ParseUnfinishedException(String message) {
        super(message);
    }

}
