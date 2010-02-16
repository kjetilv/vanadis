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

import vanadis.core.lang.ToString;

import java.io.PrintStream;

final class StreamSink implements StringSink {

    private final PrintStream stream;

    StreamSink(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public StringSink print(Object object) {
        stream.print(object);
        return this;
    }

    @Override
    public String toString() {
        return ToString.of(this, stream);
    }
}
