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
package vanadis.common.ver;

import junit.framework.Assert;
import org.junit.Test;

public class VersionTest extends Assert {

    @Test
    public void majorMinorMicro() {
        Version version = ver(1, 2, 3);
        assertMajor(version, 1);
        assertEquals(2, version.getMinor());
        assertEquals(2, version.getMinor(0));
        assertEquals(3, version.getMinor(1));
        assertMicro(version, 3);
        failMinority(version, 2);
        failMinority(version, 3);
        failMinority(version, -1);
        failMinority(version, -2);
    }

    @Test
    public void snapshot() {
        assertFalse(new Version("2.0").isSnapshot());
        assertTrue(new Version("2.0-SNAPSHOT").isSnapshot());
    }

    private void assertMicro(Version version, int micro) {
        assertEquals(micro, version.getMicro());
    }

    private void assertMajor(Version version, int major) {
        assertEquals(major, version.getMajor());
    }

    private void failMinority(Version version, int illegalMinority) {
        try {
            fail("Should not get minor " + version.getMinor(illegalMinority));
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void strings() {
        assertEquals("1.2.3", ver(1, 2, 3).toVersionString());
    }

    @Test
    public void bump() {
        assertEquals(ver(1, 2, 4), ver(1, 2, 3).bump());
        assertEquals(ver(1, 3), ver(1, 2).bump());
        assertEquals(ver(2), ver(1).bump());
    }

    public static Version ver(long... parts) {
        Version version = new Version(parts);
        assertEquals(version, new Version(version.toVersionString()));
        return version;
    }

    @Test
    public void simpleBefore() {
        assertTrue(new Version(1, 2, 3).isBefore(new Version(1, 2, 4)));
    }

    @Test
    public void simpleAfter() {
        assertTrue(new Version(1, 2, 2).isAfter(new Version(1, 2, 1)));
    }

    @Test
    public void shorterAfter() {
        assertTrue(new Version(1, 2, 2, 1).isAfter(new Version(1, 2, 1)));
    }

    @Test
    public void longerAfter() {
        assertTrue(new Version(1, 2, 3).isAfter(new Version(1, 2, 2, 1)));
    }

    @Test
    public void shorterBefore() {
        assertTrue(new Version(1, 2, 2, 1).isBefore(new Version(1, 2, 3)));
    }

    @Test
    public void longerBefore() {
        assertTrue(new Version(1, 2, 2).isBefore(new Version(1, 2, 2, 1)));
    }

    @Test
    public void longerNotAfter() {
        assertFalse(new Version(1, 2, 2, 1).isAfter(new Version(1, 2, 2, 1, 1)));
    }

    @Test
    public void shorterNotAfter() {
        assertFalse(new Version(1, 2, 2, 1).isAfter(new Version(1, 2, 3)));
    }

    @Test
    public void longerNotBefore() {
        assertFalse(new Version(1, 2, 2, 3).isBefore(new Version(1, 2, 2, 1, 1)));
    }

    @Test
    public void shorterNotBefore() {
        assertFalse(new Version(1, 2, 3, 1).isBefore(new Version(1, 2, 3)));
    }

    @Test
    public void testCompareToInitial() {
        assertTrue(new Version(1).isAfter(Version.INITIAL));
        assertTrue(Version.INITIAL.isBefore(new Version(1)));
    }

    @Test
    public void compareSnapshots() {
        assertTrue(new Version("1.0-SNAPSHOT").isAfter(new Version("1.0")));
        assertFalse(new Version("1.0-SNAPSHOT").isBefore(new Version("1.0")));
        assertTrue(new Version("1.1-SNAPSHOT").isAfter(new Version("1.0-SNAPSHOT")));
        assertFalse(new Version("1.1-SNAPSHOT").isBefore(new Version("1.0-SNAPSHOT")));
    }

    @Test
    public void testSnapshottery() {
        String str = "2.1.2-SNAPSHOT";
        Version version = new Version(str);
        assertEquals(str, version.toVersionString());
        assertEquals(version, new Version(version.toVersionString()));

        assertEquals(str, new Version(true, 2, 1, 2).toVersionString());
    }

    @Test
    public void adhocVersionString() {
        Version version = new Version("1.0.0.v20080505");
        assertEquals("1.0.0.v20080505", version.toVersionString());
    }
}
