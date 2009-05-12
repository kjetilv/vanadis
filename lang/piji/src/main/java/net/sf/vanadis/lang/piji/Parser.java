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

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.IORuntimeException;
import net.sf.vanadis.core.lang.ToString;

import java.io.*;
import java.util.List;

/**
 * <P>A parser.  Works fully functionally, i.e. has no mutable state,
 * and may as such be used thread-safely.</P>
 *
 * @author Kjetil Valstadsve
 */
final class Parser implements Parse {

    private static final class Branch {

        private final Branch parent;

        private final ListNodeType type;

        private final List<Expression> branches;

        Branch(ListNodeType type) {
            this(null, type);
        }

        private Branch(Branch parent, ListNodeType type) {
            this.parent = parent;
            this.type = type;
            this.branches = Generic.list();
        }

        public void leaf(LeafNode node) {
            this.branches.add(node);
        }

        public Branch descend(ListNodeType startType) {
            return new Branch(this, startType);
        }

        private ListNode collapsed() {
            return new ListNode(this.type, this.branches);
        }

        public Branch ascend(ListNodeType endType)
                throws ParseException {
            if (this.type != endType) {
                throw new ParseParanthesisException
                        ("Wrong par " + type.getRight() + ", expected " +
                                this.type.getRight());
            }
            if (this.parent != null) {
                this.parent.branches.add(collapsed());
                return this.parent;
            }
            return null;
        }

        public boolean isDone() {
            return this.parent == null;
        }

        public AbstractNode complete()
                throws ParseException {
            if (this.isDone()) {
                return collapsed();
            } else {
                StringBuffer sb = new StringBuffer("Incomplete, expected ");
                for (Branch branch = this.parent;
                     branch != null;
                     branch = branch.parent) {
                    sb.append(branch.type.getRight());
                }
                throw new ParseException(sb.toString());
            }
        }

        @Override
        public String toString() {
            return "Branch[" + this.type + ":" + this.branches + "]";
        }

    }

    /**
     * <P>An inner class for managing state during a parse.
     * Builds and returns a tree.</P>
     *
     * <P>Using this class to model state allows a parse to proceed
     * multi-threaded without need for synchronization.</P>
     *
     * @author Kjetil Valstadsve
     */
    private static final class ParseState {

        private final List<Expression> results = Generic.list();

        private Branch currentTop;

        private Branch current;

        public void descend(char leftPar) {
            ListNodeType type = ListNodeType.get(leftPar);
            if (this.currentTop == null) {
                this.current = new Branch(type);
                this.currentTop = this.current;
            } else {
                this.current = this.current.descend(type);
            }
        }

        public void leaf(LeafNode ln) {
            if (this.currentTop == null) {
                this.results.add(ln);
            } else {
                this.current.leaf(ln);
            }
        }

        public void ascend(char rightPar) {
            if (this.current == null) {
                throw new ParseParanthesisException("Unexpected right par " + rightPar);
            }

            this.current = this.current.ascend(ListNodeType.get(rightPar));
            if (this.current == null) {
                this.results.add(this.currentTop.complete());
                this.currentTop = null;
            }
        }

        public boolean isDone() {
            return this.current == null;
        }

        public List<Expression> harvest() {
            return this.results;
        }

        @Override
        public String toString() {
            return ToString.of(this, this.results, "current" + this.current);
        }
    }

    private static LeafNode resolveNumber(double nval) {
        return nval == (int) nval ? new LeafNode((int) nval)
                : nval == (long) nval ? new LeafNode((long) nval)
                        : nval == (float) nval ? new LeafNode((float) nval)
                                : (new LeafNode(nval));
    }

    @Override
    public List<Expression> parseAll(String string) {
        return parseAll(new StringReader(string));
    }

    @Override
    public List<Expression> parseAll(InputStream stream) {
        return parseAll(new InputStreamReader(stream));
    }

    @Override
    public List<Expression> parseAll(Reader reader) {
        return parse(reader, false);
    }

    @Override
    public Expression parse(String string) {
        return parse(new StringReader(string));
    }

    @Override
    public Expression parse(InputStream stream) {
        return parse(new InputStreamReader(stream));
    }

    @Override
    public Expression parse(Reader reader) {
        List<Expression> exprs = parse(reader, true);
        if (exprs.isEmpty()) {
            return null;
        }
        return exprs.get(0);
    }

    private static List<Expression> parse(Reader reader, boolean justOne) {
        ParseState state = new ParseState();
        StreamTokenizer tokenizer = new ParseTokenizer(reader);

        int nextToken;
        boolean running = true;
        boolean negation = false;
        do {
            nextToken = nextToken(tokenizer);
            if (negation) {
                state.leaf(resolveNumber(tokenizer.nval));
                negation = false;
            } else {
                switch (nextToken) {

                    case (int) '(':
                    case (int) '{':
                        state.descend((char) nextToken);
                        break;

                    case (int) ')':
                    case (int) '}':
                        state.ascend((char) nextToken);
                        break;

                    /*                case (int)'-' :
                    negation = true;
                    break;*/

                    case ParseTokenizer.QUOTE_CHAR:
                        state.leaf(new LeafNode(tokenizer.sval));
                        break;

                    case StreamTokenizer.TT_NUMBER:
                        state.leaf(resolveNumber(tokenizer.nval));
                        break;

                    case StreamTokenizer.TT_WORD:
                        String sval = tokenizer.sval;
                        if (sval.equals("true")) {
                            state.leaf(new LeafNode(true));
                        } else if (sval.equals("false")) {
                            state.leaf(new LeafNode(false));
                        } else if (sval.equals("null")) {
                            state.leaf(new LeafNode(null));
                        } else if (sval.startsWith("-") && sval.length() > 1) {
                            state.leaf(resolveNumber(Double.parseDouble(sval)));
                        } else {
                            state.leaf(new LeafNode(Symbol.get(sval)));
                        }
                        break;

                    case (int) '#':  // Typed number, followed by type and value
                        if (nextToken(tokenizer) == StreamTokenizer.TT_WORD) {
                            String type = tokenizer.sval.substring(0, 1);
                            String v = tokenizer.sval.substring(1);
                            if (type.equals("i")) {
                                state.leaf(new LeafNode(Integer.parseInt(v)));
                            } else if (type.equals("l")) {
                                state.leaf(new LeafNode(Long.parseLong(v)));
                            } else if (type.equals("f")) {
                                state.leaf(new LeafNode(Float.parseFloat(v)));
                            } else if (type.equals("d")) {
                                state.leaf(new LeafNode(Double.parseDouble(v)));
                            } else if (type.equals("b")) {
                                state.leaf(new LeafNode(Byte.parseByte(v)));
                            } else if (type.equals("s")) {
                                state.leaf(new LeafNode(Short.parseShort(v)));
                            } else if (type.equals("c")) {
                                state.leaf(new LeafNode(v.charAt(0)));
                            } else {
                                throw new ParseException
                                        (tokenizer.lineno(),
                                         "Unknown primitive type #" + type +
                                                 tokenizer.nval);
                            }
                        } else {
                            throw new ParseException
                                    (tokenizer.lineno(),
                                     "Bad number " + tokenizer.nval);
                        }
                        break;

                    case StreamTokenizer.TT_EOF:
                        running = false;
                        break;

                    default:
                        throw new ParseException
                                (tokenizer.lineno(),
                                 "'" + ((char) nextToken) + "' unknown " +
                                         (tokenizer.sval == null
                                                 ? "number " + tokenizer.nval
                                                 : "string " + tokenizer.sval) +
                                         ", parse state: " + state);
                }
            }
            if (justOne) {
                running = running && !state.isDone();
            }

        } while (running);

        if (state.isDone()) {
            return state.harvest();
        }

        throw new ParseUnfinishedException("Unfinished parse: " + state);
    }

    private static int nextToken(StreamTokenizer tokenizer) {
        try {
            return tokenizer.nextToken();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Piji's Parser";
    }

}

