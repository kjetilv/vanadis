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

import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.hold.Holder;

/**
 * A Scheme tree leaf node
 *
 * @author Kjetil Valstadsve
 */
public final class LeafNode extends AbstractNode {

    /**
     * The content of the leaf
     */
    private final Object content;

    private static final long serialVersionUID = 181071817937142320L;

    boolean isContentExplicitlyTyped() {
        return this.content instanceof Holder;
    }

    boolean isContentSymbol() {
        return this.content instanceof Symbol;
    }

    boolean isContentString() {
        return this.content instanceof String;
    }

    boolean isContentHolder() {
        return this.content instanceof Holder;
    }

    Symbol getSymbolContent() {
        return (Symbol) this.content;
    }

    String getStringContent() {
        return (String) this.content;
    }

    /**
     * Get the content of the leaf
     *
     * @return The content of the leaf
     */
    Object getContent() {
        return content;
    }

    /**
     * A constructor for a leaf with a general object content
     *
     * @param content The content
     */
    LeafNode(Object content) {
        this.content = content;
    }

    /**
     * A constructor for a leaf with a boolean content
     *
     * @param bool The content
     */
    LeafNode(boolean bool) {
        this(DataHolderFactory.holder(bool));
    }

    /**
     * A constructor for a leaf with a char content
     *
     * @param c The content
     */
    LeafNode(char c) {
        this(DataHolderFactory.holder(c));
    }

    /**
     * A constructor for a leaf with an integer content
     *
     * @param i The content
     */
    LeafNode(int i) {
        this(DataHolderFactory.holder(i));
    }

    /**
     * A constructor for a leaf with a long content
     *
     * @param l The content
     */
    LeafNode(long l) {
        this(DataHolderFactory.holder(l));
    }

    /**
     * A constructor for a leaf with a float content
     *
     * @param f The content
     */
    LeafNode(float f) {
        this(DataHolderFactory.holder(f));
    }

    /**
     * A constructor for a leaf with a double content
     *
     * @param d The content
     */
    LeafNode(double d) {
        this(DataHolderFactory.holder(d));
    }

    /**
     * A constructor for a leaf with a byte content
     *
     * @param b The content
     */
    LeafNode(byte b) {
        this(DataHolderFactory.holder(b));
    }

    /**
     * A constructor for a leaf with a short content
     *
     * @param sh The content
     */
    LeafNode(short sh) {
        this(DataHolderFactory.holder(sh));
    }

    /**
     * Returns the string value of the content
     */
    @Override
    public void writeToBuffer(StringBuffer buffer, int x, boolean bool) {
        String string = this.isContentString()
                ? "\"" + this.getContent() + "\""
                : (this.isContentHolder()
                        ? ((Holder) this.getContent()).toValueString()
                        : String.valueOf(this.getContent()));
        buffer.append(string);
    }

    @Override
    public boolean equals(Object object) {
        return this == object ||
                (object instanceof LeafNode &&
                        ((LeafNode) object).content.equals(this.content));
    }

    @Override
    public int hashCode() {
        return 19 + this.content.hashCode() * 13;
    }

    /**
     * Evaluates the content of this leaf in the given environment.  Iff
     * content is a symbol, look it up in the context
     *
     * @param context The context to evaluate in
     * @return The value of the content
     * @throws EvaluationException Thrown iff something bad happens
     */
    @Override
    public Object evaluate(Context context)
            throws Throwable {
        if (this.isContentSymbol()) {
            Object value = context.lookup(this.getSymbolContent());
            if (value == null) {
                value = Reflector.get().resolveLeafNode(context, this);
                if (value == null) {
                    throw new EvaluationException
                            ("Unknown symbol or type " + this.getSymbolContent());
                }
            }
            return value;
        } else {
            return this.getContent();
        }
    }

}

