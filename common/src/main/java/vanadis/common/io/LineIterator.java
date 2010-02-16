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

package vanadis.common.io;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;

final class LineIterator implements Iterator<String> {

    private final LineNumberReader reader;

    private String next;

    LineIterator(File file) {
        this(Files.readFile(file));
    }

    LineIterator(Reader reader) {
        this.reader = (LineNumberReader)
            (reader instanceof LineNumberReader ? reader
                : new LineNumberReader(reader));
        readLine();
    }

    private void readLine() {
        try {
            next = reader.readLine();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String next() {
        try {
            return next;
        } finally {
            readLine();
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }
}