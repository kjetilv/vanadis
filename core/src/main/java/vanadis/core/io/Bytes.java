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

package vanadis.core.io;

import vanadis.core.lang.Not;
import vanadis.core.lang.VarArgs;

import java.io.UnsupportedEncodingException;

public final class Bytes {

    private static final String ENC = "utf-8";

    private static final byte[] EMPTY = new byte[0];

    private static final byte[] NONE = new byte[]{};

    public static byte[] suffix(int offset, byte[] bytes) {
        if (offset < Not.nil(bytes, "bytes").length) {
            byte[] suffix = new byte[bytes.length - offset];
            System.arraycopy(bytes, offset, suffix, 0, suffix.length);
            return suffix;
        }
        return NONE;
    }

    public static byte[] prepend(byte[] bytes, byte... prefix) {
        return add(Not.nil(prefix, "prefix"), Not.nil(bytes, "bytes"));
    }

    public static byte[] append(byte[] bytes, byte... suffix) {
        return add(Not.nil(bytes, "bytes"), Not.nil(suffix, "suffix"));
    }

    public static byte[] add(byte[]... parts) {
        if (!VarArgs.present(parts)) {
            return EMPTY;
        }
        int length = 0;
        for (byte[] part : parts) {
            length += part == null ? 0 : part.length;
        }
        byte[] bytes = new byte[length];
        int pos = 0;
        for (byte[] part : parts) {
            if (part != null) {
                System.arraycopy(part, 0, bytes, pos, part.length);
                pos += part.length;
            }
        }
        return bytes;
    }

    public static byte[] bytesFrom(int value) {
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = (byte) (value >> (3 - i) * 8 & 0xFF);
        }
        return bs;
    }

    public static int intFrom(byte[] bytes) {
        return intFrom(bytes, 0);
    }

    public static byte[] bytesFrom(long value) {
        byte[] bs = new byte[8];
        for (int i = 0; i < 8; i++) {
            bs[i] = (byte) (value >> (7 - i) * 8 & 0xFF);
        }
        return bs;
    }

    public static long longFrom(byte[] bytes) {
        return longFrom(bytes, 0);
    }

    public static int intFrom(byte[] bytes, int offset) {
        if (bytes == null) {
            return 0;
        }
        int count = Math.min(4, bytes.length - offset);
        if (count < 1) {
            return 0;
        }
        int value = 0;
        for (int i = 0; i < count; i++) {
            value += (bytes[i + offset] & 0xFF) << (count - 1 - i) * 8;
        }
        return value;
    }

    private static long longFrom(byte[] bytes, int offset) {
        if (bytes == null) {
            return 0;
        }
        int count = Math.min(8, bytes.length - offset);
        if (count < 1) {
            return 0;
        }
        int value = 0;
        for (int i = 0; i < count; i++) {
            value += (bytes[i + offset] & 0xFF) << (count - 1 - i) * 8;
        }
        return value;
    }

    public static String stringFrom(byte[] bytes) {
        return stringFrom(Not.nil(bytes, "bytes"), 0, bytes.length);
    }

    public static String stringFrom(byte[] bytes, int offset) {
        return stringFrom(Not.nil(bytes, "bytes"), offset, bytes.length - offset);
    }

    public static String stringFrom(byte[] bytes, int offset, int length) {
        try {
            return new String(Not.nil(bytes, "bytes"), offset, length, ENC);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Unsupported encoding", e);
        }
    }

    public static byte[] bytesFrom(String string) {
        try {
            return Not.nil(string, "string").getBytes(ENC);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Unsupported encoding", e);
        }
    }

    private Bytes() {}
}