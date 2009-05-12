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

package net.sf.vanadis.core.io;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.collections.Pair;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.VarArgs;
import net.sf.vanadis.core.system.VM;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

public class IO {

    static final String ENC = "utf-8";

    static final Charset UTF_8 = Charset.forName(ENC);

    public static int copy(InputStream source, OutputStream sink) {
        return copy(source, sink, false);
    }

    public static int copy(InputStream source, OutputStream sink, boolean close) {
        Not.nil(source, "source");
        Not.nil(sink, "sink");
        byte[] buffer = new byte[8192];
        int total = 0;
        try {
            int read;
            while (true) {
                read = source.read(buffer);
                if (read > 0) {
                    total += read;
                    sink.write(buffer, 0, read);
                } else if (read < 0) {
                    return total;
                }
            }
        } catch (IOException e) {
            throw new IORuntimeException
                    ("Failed to copy data from " + source + " to " + sink, e);
        } finally {
            if (close) {
                Closeables.close(source, sink);
            }
        }
    }


    public static String toString(InputStream stream, Pair<String, Pattern>... lookouts) {
        try {
            return toString(new InputStreamReader(stream, ENC), lookouts);
        } catch (UnsupportedEncodingException e) {
            return failEncoding(ENC, e);
        }
    }

    public static String toString(Reader reader, Pair<String, Pattern>... lookouts) {
        return toString(new LineIterable(reader), new StringBuilder(), lookouts);
    }

    public static String toString(Iterable<String> iterable, StringBuilder builder, Pair<String, Pattern>... lookouts) {
        return toString(iterable, new BuilderSink(builder), lookouts);
    }

    public static String toString(Iterable<String> iterable, PrintStream stream, Pair<String, Pattern>... lookouts) {
        return toString(iterable, new StreamSink(stream), lookouts);
    }

    private static String toString(Iterable<String> iterable, StringSink stringSink, Pair<String, Pattern>[] lookouts) {
        Map<String, Integer> matches = VarArgs.present(lookouts) ? null : Generic.<String, Integer>map();
        for (String line : iterable) {
            stringSink.print(line).print(VM.LN);
            for (Pair<String, Pattern> lookout : lookouts) {
                String name = lookout.getOne();
                Pattern pattern = lookout.getTwo();
                if (pattern.matcher(line).matches()) {
                    int count = matches.containsKey(name) ? matches.get(name) + 1 : 1;
                    matches.put(name, count);
                }
            }
        }
        if (matches != null && !matches.isEmpty()) {
            stringSink.print(VM.LN).print("Matches:").print(VM.LN);
            for (Map.Entry<String, Integer> entry : matches.entrySet()) {
                stringSink.print("  ").print(entry.getKey()).print(": ").print(entry.getValue()).print(VM.LN);
            }
        }
        return stringSink.toString();
    }

    static LineNumberReader linesFrom(Reader in) {
        return new LineNumberReader(in);
    }

    static <T> T failEncoding(UnsupportedEncodingException e) {
        throw new IllegalArgumentException("Unknown encoding: " + ENC, e);
    }

    static <T> T failEncoding(String enc, UnsupportedEncodingException e) {
        throw new IllegalArgumentException("Unknown encoding: " + enc, e);
    }
}
