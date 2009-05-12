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
 * An implementation of the Expression interface, using a tree
 * structure, hence the name.  This abstract superclass spans leaf and
 * tree node subclasses
 *
 * @author Kjetil Valstadsve.
 */
public abstract class AbstractNode implements Expression {

    private static final long serialVersionUID = 2627420415294469460L;

    /**
     * Gets a scheme string representation of this node.  Simply calls
     * asSchemeString with a zero offset, should not be redefined by
     * subclasses.
     */
    @Override
    public final String toSchemeString() {
        StringBuffer buffer = new StringBuffer();
        writeToBuffer(buffer, 0, false);
        return buffer.toString();
    }

    @Override
    public final String toString() {
        return this.toSchemeString();
    }

}

