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

import java.io.Serializable;

/**
 * <P>A super-interface for a Scheme expression.</P>
 *
 * @author Kjetil Valstadsve
 */
public interface Expression extends Serializable {

    /**
     * Evaluates this expression in a given scheme context
     *
     * @param context The context to evaluate in
     * @return The result of the evaluation
     * @throws Throwable iff the evaluation goes awry
     */
    Object evaluate(Context context)
            throws Throwable;

    /**
     * Returns this expression in a string form
     *
     * @return This expression in a string form
     */
    String toSchemeString();

    /**
     * Used to get a scheme string representation of this node with
     * proper indentation.
     *
     * @param buffer The buffer
     * @param xcoord Used to set the indentation for printing, enabling proper
     *               tree structuring of the output
     * @param indent Whether to indent
     */
    void writeToBuffer(StringBuffer buffer,
                       int xcoord, boolean indent);
}
