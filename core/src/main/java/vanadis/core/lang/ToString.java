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

package vanadis.core.lang;

import vanadis.core.system.VM;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

/**
 * toString utility methods.
 */
public final class ToString {

    public static String of(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("[ ");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=>").append(ofObjectOrArray(entry.getValue())).append(" ");
        }
        return sb.append("]").toString();
    }

    public static String of(Iterable<?> iter) {
        StringBuilder sb = new StringBuilder("[ ");
        for (Object entry : iter) {
            sb.append(ofObjectOrArray(entry)).append(" ");
        }
        return sb.append("]").toString();
    }

    public static String ofObjectOrArray(Object object) {
        if (object == null) {
            return null;
        }
        if (object.getClass().isArray()) {
            if (object instanceof Object[]) {
                return Arrays.toString((Object[]) object);
            } else if (object instanceof int[]) {
                return Arrays.toString((int[]) object);
            } else if (object instanceof byte[]) {
                return Arrays.toString((byte[]) object);
            } else if (object instanceof long[]) {
                return Arrays.toString((long[]) object);
            } else if (object instanceof short[]) {
                return Arrays.toString((short[]) object);
            } else if (object instanceof float[]) {
                return Arrays.toString((float[]) object);
            } else if (object instanceof double[]) {
                return Arrays.toString((double[]) object);
            } else if (object instanceof boolean[]) {
                return Arrays.toString((boolean[]) object);
            }
            throw new IllegalStateException
                    ("Uh-oh! A new PRIMITIVE type has appeared in Java " + VM.VERSION +
                            ": " + object.getClass() + "!  The fools!");
        }
        return object.toString();
    }

    public static String of(Object yourself, Object body) {
        return of(yourself, body == null ? new StringBuilder() : new StringBuilder(body.toString()));
    }

    public static String of(Object yourself, StringBuilder body) {
        return name(yourself).append("[").append(body == null ? "" : body).append("]").toString();
    }

    public static String of(Object yourself, Object... body) {
        return writeArgs(name(yourself).append("["), body).append("]").toString();
    }

    public static String[] array(Object array) {
        String[] strs = new String[Array.getLength(array)];
        for (int i = 0; i < strs.length; i++) {
            Object o = Array.get(array, i);
            strs[i] = o == null ? null : o.toString();
        }
        return strs;
    }

    /**
     * Creates the toString as it would have been, if the instance didn't
     * implement {@link Object#toString()}!
     *
     * @param object Object
     * @return Default toString
     */
    public static String objectToString(Object object) {
        return (object == null)
                ? String.valueOf(object)
                : "" + object.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(object));
    }

    private static StringBuilder name(Object yourself) {
        return new StringBuilder(yourself.getClass().getSimpleName());
    }

    private static StringBuilder writeArgs(StringBuilder sb, Object[] args) {
        int length = args.length;
        int target = length / 2;
        if (length > 0) {
            int offset = offsetForOddArgs(sb, args, length);
            for (int i = 0; i < target; i++) {
                int pos = offset + i * 2;
                writeItem(sb, args, i + 1 < target, pos);
            }
        }
        return sb;
    }

    private static int offsetForOddArgs(StringBuilder sb, Object[] args, int length) {
        boolean odd = length % 2 == 1;
        if (odd) { // We know the array is longer, otherwise the overloaded create method would have been invoked
            sb.append(args[0]).append(" ");
            return 1;
        } else {
            return 0;
        }
    }

    private static void writeItem(StringBuilder sb, Object[] body,
                                  boolean moreArgs,
                                  int headerIndex) {
        Object headerObject = body[headerIndex];
        String header = headerObject == null ? null
                : String.valueOf(headerObject);
        if (header != null && header.trim().length() > 0) {
            sb.append(header);
            if (!(header.endsWith(":") || header.endsWith("@") || header.endsWith("="))) {
                sb.append(":");
            }
        }
        sb.append(body[headerIndex + 1]);
        if (moreArgs) {
            sb.append(" ");
        }
    }

    public static String throwable(Throwable throwable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        try {
            throwable.printStackTrace(ps);
        } finally {
            ps.close();
        }
        return new String(baos.toByteArray());
    }

    private ToString() {
        // Don't make me
    }
}
