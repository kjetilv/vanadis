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

import java.util.Arrays;
import java.util.List;

/**
 * <P>Static support for implementing the infamous pair of equals and hashCode methods.
 * We want it done cheaply (less typing) and reliably. Some prototypical usage:</P>
 *
 * <P><PRE>
 * public boolean equals(Object o) {
 *     if (o == this) {
 *         return true; // This shortcut is not necessary, but it <b>is</b> a shortcut, FWIW
 *     }
 *     MyClass ok = {@link #retyped(Object, Object) EqHc.retyped}(this, o); // Legal cast or null
 *     return ok != null && {@link #eq(Object[]) EqHc.eq}
 *         (fieldOne, ok.fieldOne,
 *          fieldTwo, ok.fieldTwo);
 * }
 *
 * public int hashCode() {
 *     return {@link #hc(Object[]) EqHc.hc}(fieldOne, fieldTwo);
 * }
 * </PRE></P>
 *
 * <P>The shortcut might be moved to post-cast at the expense of some extra processing:</P>
 *
 * <P><PRE>
 * public boolean equals(Object o) {
 *     MyClass ok = {@link #retyped(Object, Object) EqHc.retyped}(this, o);
 *     return <b>this == ok ||</b> ok != null &&
 *         {@link #eq(Object[]) EqHc.eq}(fieldOne, ok.fieldOne, fieldTwo, ok.fieldTwo);
 * }
 * </PRE></P>
 *
 * <P>Design note: All methods in this class are heavily overloaded.  This reflects the desired usage pattern:
 * <strong>Comparison of (typed) fields</strong>!
 * It makes no sense to pass the <code>null</code> literal to any of these methods, so don't do it.  Don't pass
 * an empty varargs list. Pass field references, <em>nothing else</em>, and ambiguities don't become an issue.
 * The massive overloading by design, since it helps the compiler tell you when you're
 * using it wrong. This class is only ugly so your classes don't have to be.</P>
 *
 * <P>The only exceptions are {@link #eqA(Object[][]) eqA(Object[]...)} and {@link #hcA(Object[][]) hcA(Object[]...)},
 * necessary only because Object[] is a subclass of Object.</P>
 *
 * <P>Note that this class will not be party to dodgy <code>float</code> and <code>double</code> comparison.</P>
 */
public final class EqHc {

    /**
     * <P>Checks if the the first argument has the same type as the second. If so, returns the second with the same type
     * as the first, otherwise null.  This is a support method for equals methods.</P>
     *
     * <P>Use with {@link #eq(Object[])} to shorten the following common occurrence:</P>
     *
     * <P><PRE>
     * if (this == object) {
     *     return true;
     * }
     * if (object != null && object.getClass() == getClass()) {
     *     Foo foo = (Foo)object;
     *     return name.equals(foo.name) && bar.equals(foo.bar) && zot.equals(foo.zot);
     * }
     * return false;
     * </PRE></P>
     *
     * to:
     *
     * <P><PRE>
     * Foo foo = EqHash.typedAs(this, object);
     * return this == foo || foo != null && EqHc.eq(name, foo.name, bar, foo.bar, zot, foo.zot);
     * </PRE></P>
     *
     * <P>Note that the former is also null-safe!  Either or all of the name, bar and zot fields may be null,
     * equality is still checked.</P>
     *
     * @param subject Always pass yourself as this argument
     * @param object  Pass the compared object (the argument to <code>equals</code>) as this argument
     * @return If the other object is the same as this, return this.
     *         If the other object is the same class, return it and cast it to the same class.
     *         Otherwise, return null.
     */
    public static <T> T retyped(T subject, Object object) {
        assert subject != null : "Expected non-null first argument: this";
        return object == null ? null
                : object == subject ? subject
                        : specificCast(subject, object);
    }

    public static boolean eq(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o2 != null && o1.equals(o2);
    }

    public static boolean eq(Object[] o1, Object[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(int[] o1, int[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(long[] o1, long[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(boolean[] o1, boolean[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(short[] o1, short[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(byte[] o1, byte[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    public static boolean eq(char[] o1, char[] o2) {
        return o1 == o2 || o1 != null && o2 != null && Arrays.equals(o1, o2);
    }

    /**
     * <P>Null-safe equals comparison of an even number - <b>and at least two!</b> -
     * <b>non-array</b>, arguments. The arguments may be null.</P>
     *
     * <P>For array comparisons, use {@link #eqA(Object[][])}.</P>
     *
     * @param args At least two arguments, or an even number - arrays not allowed
     * @return True if arguments are pair-wise equals.
     * @throws AssertionError If arguments are bogus
     * @see #retyped(Object, Object)
     */
    public static boolean eq(Object... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@link #eq(Object[])} for object arrays.  This method has a different name, to avoid overloading
     * confusion - <code>Object[]</code> is a subclass of <code>Object</code>.
     *
     * @param args Arrays
     * @return True if arrays are pair-wise equal
     */
    public static boolean eqA(Object[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (args[i].length != args[i + 1].length || !eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(int[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(long[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(boolean[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(short[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(byte[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq(char[]... args) {
        int len = verifyEvenVarargs(args);
        for (int i = 0; i < len; i += 2) {
            if (!eq(args[i], args[i + 1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * <P>Compute compound hash code for all argument, <b>non-array</b> objects. Null arguments are allowed.</P>
     *
     * <P>The order of the arguments is significant, i.e. <code>hc(1,2,3)</code> is not the same as
     * <code>hc(3,2,1)</code>. We think this increases uniqueness since objects hashing on the same state, but not in
     * the same order, will get different hashcodes. <code>null</code> arguments will be hashed according to their
     * position.</P>
     *
     * <P>For array objects, use {@link #hcA(Object[][])}.</P>
     *
     * @param args Objects A non-null, non-empty sequence of objects, null values allowed
     * @return A hashcode
     */
    public static int hc(Object... args) {
        int length = verifyActualVarargs(args);
        int hashTotal = 0;
        for (int i = 0; i < length; i++) {
            hashTotal += hash(args[i], i);
        }
        return hashTotal + primeNo(length);
    }

    /**
     * {@link #hc(Object[])} for arrays.
     *
     * @param args Arrays
     * @return A hashcode
     */
    public static int hcA(Object[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    public static int hc(int[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    public static int hc(long[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    public static int hc(boolean[]... args) {
        int len = verifyActualVarargs(args);
        int sum = hashArgs(len, args);
        return sum + primeNo(len);
    }

    public static int hc(short[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    public static int hc(byte[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    public static int hc(char[]... args) {
        int len = verifyActualVarargs(args);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum + primeNo(len);
    }

    private static final int[] PRIME_NUMBERS;

    private static final int PRIME_COUNT;

    @SuppressWarnings({"unchecked"})
    private static <T> T specificCast(T subject, Object object) {
        Class<? extends T> type = (Class<? extends T>) subject.getClass();
        return type != object.getClass() ? null : type.cast(object);
    }

    private static int hashArgs(int len, boolean[]... args) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += args[i] == null ? 0 : contribution(Arrays.hashCode(args[i]));
        }
        return sum;
    }

    private static int contribution(int hash) {
        return hash * primeNo(hash);
    }

    static {
        List<Integer> primes = Sieve.oddPrimes(200);
        PRIME_COUNT = primes.size();
        PRIME_NUMBERS = new int[PRIME_COUNT];
        for (int i = 0; i < PRIME_COUNT; i++) {
            PRIME_NUMBERS[i] = primes.get(i);
        }
    }

    /**
     * Get the nth prime.
     *
     * @param n n
     * @return Prime #n
     */
    private static int primeNo(int n) {
        return PRIME_NUMBERS[(Math.abs(n) % PRIME_COUNT)];
    }

    /**
     * Hash argument, taking position into account.
     *
     * @param arg Argument
     * @param i   position
     * @return Hash
     */
    private static int hash(Object arg, int i) {
        int hash = arg == null ? 0 : arg.hashCode();
        return primeNo(hash + i) * hash;
    }

    /**
     * Check non-null, non-empty varargs.
     *
     * @param args Varargs
     * @return Length of array
     * @throws IllegalAccessError If null or empty
     */
    private static int verifyActualVarargs(Object[] args) {
        assert args != null : "Null varargs passed to vararg method";
        assert args.length > 0 : "No objects passed to vararg method";
        return args.length;
    }

    /**
     * Check non-null, non-empty, even-numbered varargs.
     *
     * @param args Varargs
     * @return Length of array
     * @throws AssertionError If null, empty or odd
     */
    private static int verifyEvenVarargs(Object[] args) {
        int len = verifyActualVarargs(args);
        assert len % 2 == 0 : "Odd number of objects passed to vararg method: " + Arrays.toString(args);
        return len;
    }

    private EqHc() {
    }
}
