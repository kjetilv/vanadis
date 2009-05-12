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

import java.util.Arrays;
import java.util.List;

/**
 * A class describing a tree node with children in a Scheme tree
 *
 * @author Kjetil Valstadsve
 */
public final class ListNode extends AbstractNode {

    private static final long serialVersionUID = -7895911217883595746L;

    private static final AbstractNode[] NO_NODES = new AbstractNode[0];

    private final ListNodeType type;

    /**
     * A list of all children
     */
    private final Expression[] nodes;

    /**
     * Construct a scheme node a list of subnodes
     *
     * @param type Type
     */
    ListNode(ListNodeType type) {
        this(type, (List<Expression>) null);
    }

    ListNode(ListNodeType type, List<Expression> nodes) {
        this(type, nodes == null || nodes.isEmpty()
            ? NO_NODES
            : nodes.toArray(new Expression[nodes.size()]));
    }

    private ListNode(ListNodeType type, Expression[] nodes) {
        this.type = type;
        this.nodes = nodes;
    }

    ListNode sublist(int offset) {
        return sublist(offset, this.nodes.length - 1);
    }

    ListNode sublist(int offset, int length) {
        Expression[] sub = new AbstractNode[length];
        System.arraycopy(this.nodes, offset, sub, 0, length);
        return new ListNode(this.type, sub);
    }

    ListNodeType getType() {
        return this.type;
    }

    public int size() {
        return this.nodes.length;
    }

    public final Expression get(int i) {
        return this.nodes[i];
    }

    /**
     * Get a numbered argument node.  Recall that this refers to
     * the numbering of the child nodes preceding the first.
     *
     * @return Function node
     */
    final Expression getFunNode() {
        return this.nodes[0];
    }

    /**
     * Get a numbered argument node.  Recall that this refers to
     * the numbering of the child nodes preceding the first.
     *
     * @param i The number of the argument node
     * @return Argument node alt. i
     */
    final Expression getArgumentNode(int i) {
        return this.nodes[i + 1];
    }

    /**
     * Returns the string value of this tree node
     */
    @Override
    public void writeToBuffer(StringBuffer buffer, int x, boolean indent) {
        buffer.append(this.getType().getLeft());
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].writeToBuffer(buffer, x + 2, true);
            if (i < (this.nodes.length - 1)) {
                buffer.append(" ");
            }
        }
        buffer.append(this.getType().getRight());
    }

    @Override
    public final boolean equals(Object object) {
        return super.equals(object) ||
            (object instanceof ListNode &&
                Arrays.equals(this.nodes, ((ListNode) object).nodes));
    }

    @Override
    public int hashCode() {
        int hc = 19;
        for (Expression node : this.nodes) {
            hc += node.hashCode() + 17;
        }
        return hc;
    }

    /**
     * Evaluates this tree node in the given context.  Evaluates the
     * first child node to a function, then applies it to the
     * remaining child nodes.
     *
     * @param context The context to evaluate in
     * @return The value of the tree
     * @throws EvaluationException Thrown iff something bad happens
     */
    @Override
    public final Object evaluate(Context context)
        throws Throwable {
        if (this.nodes.length == 0) {
            return null;
        }
        Reflector ref = Reflector.get(this.type == ListNodeType.PRIVATE);
        Object object = ref.resolveTarget(context, this.nodes[0]);
        if (object instanceof Function) {
            return ((Function) object).apply(context, this.nodes);
        } else {
            if (this.nodes.length == 1) {
                throw new EvaluationException
                    ("No-arg expressions need instance of " + Function.class +
                        " to have meaning, got " + this.nodes[0] +
                        " (" + object + ")");
            }
            return ref.getInvoker().invoke(context, object, this.nodes, 1);
        }
    }

}

