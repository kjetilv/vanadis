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

import java.io.Reader;
import java.io.StreamTokenizer;

final class ParseTokenizer extends StreamTokenizer {

    static final int QUOTE_CHAR = (int) '"';

    private static final int COMMENT_CHAR = (int) ';';

    private void wordChar(char c) {
        this.wordChars((int) c, (int) c);
    }

    private void ordChar(char c) {
        this.ordinaryChar((int) c);
    }

    private void wsChar(char c) {
        this.whitespaceChars((int) c, (int) c);
    }

    ParseTokenizer(Reader reader) {
        super(reader);
        this.slashSlashComments(false);
        this.eolIsSignificant(false);
        this.parseNumbers();

        this.commentChar(COMMENT_CHAR);
        this.quoteChar(QUOTE_CHAR);

        this.ordChar('(');
        this.ordChar(')');
        this.ordChar('{');
        this.ordChar('}');

        this.ordChar('#');
        this.ordChar(':');
        this.ordChar('.');
        this.ordChar('*');
        this.ordChar('+');
        this.ordChar('/');
        this.ordChar('-');

        this.wordChar('&');
        this.wordChar('$');
        this.wordChar('@');
        this.wordChar('^');
        this.wordChar('%');
        this.wordChar(':');

        this.wordChar('*');
        this.wordChar('+');
        this.wordChar('/');
        this.wordChar('-');

        this.wordChar('[');
        this.wordChar(']');
        this.wordChar('<');
        this.wordChar('>');
        this.wordChar('=');
        this.wordChar('!');
        this.wordChar('.');
        this.wordChar('_');

        this.wsChar(' ');
        this.wsChar('\t');
        this.wsChar('\n');
    }

}

