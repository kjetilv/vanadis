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

package vanadis.core.ver;

import java.io.Serializable;
import java.util.Arrays;

public final class Version implements Comparable<Version>, Serializable {

    private static final long[] EMPTY = new long[]{};

    public static final Version INITIAL = new Version();

    public static final Version LATEST = new Version(Long.MAX_VALUE);

    private final long[] parts;

    private final String adHoc;

    private final boolean snapshot;

    private static final long serialVersionUID = -7155655964184414458L;

    public Version(String partsOrAdHoc) {
        this(partsOrAdHoc, isSnapshot(partsOrAdHoc), parts(partsOrAdHoc));
    }

    public Version(boolean snapshot, long... parts) {
        this(null, snapshot, parts);
    }

    public Version(long... parts) {
        this(null, false, parts);
    }

    private Version(String adHoc, boolean snapshot, long... parts) {
        if (parts == null && adHoc == null) {
            throw new NullPointerException("Required adhoc version or version parts.");
        }
        this.adHoc = parts == null ? adHoc : null;
        this.snapshot = snapshot;
        this.parts = this.adHoc == null ? initial(parts) ? EMPTY : parts
                : null;

        assert this.adHoc == null || this.parts == null
                : "adhoc:" + this.adHoc + ", parts:" + Arrays.toString(this.parts);
    }

    public boolean isInitial() {
        return adHoc == null && parts == EMPTY;
    }

    public long getMajor() {
        adHocFailsTo("distinguish major component");
        return isInitial() ? 0 : parts[0];
    }

    public long getMinor(int minority) {
        adHocFailsTo("distinguish minor component");
        if (isInitial()) {
            return 0;
        }
        int index = minority + 1;
        if (index >= parts.length || index < 1) {
            throw new IllegalArgumentException(this + " has no minor #" + minority);
        }
        return parts[index];
    }

    public long getMinor() {
        return getMinor(0);
    }

    public long getMicro() {
        adHocFailsTo("distinguish micro component");
        return isInitial() ? 0 : parts[parts.length - 1];
    }

    public Version bump() {
        adHocFailsTo("bump to next version");
        if (isInitial()) {
            return new Version(1);
        } else {
            long[] copy = parts.clone();
            copy[copy.length - 1]++;
            return new Version(copy);
        }
    }

    public String toVersionString() {
        if (isAdHoc()) {
            return adHoc;
        }
        if (isInitial()) {
            return snapshotOf("0");
        }
        StringBuilder builder = new StringBuilder();
        int len = parts.length;
        int lastDotted = len - 1;
        for (int i = 0; i < lastDotted; i++) {
            builder.append(parts[i]);
            builder.append(".");
        }
        builder.append(parts[lastDotted]);
        return snapshotOf(builder.toString());
    }

    private void adHocFailsTo(String desire) {
        if (isAdHoc()) {
            throw new IllegalStateException(this + " is ad-hoc, cannot " + desire);
        }
    }

    public boolean isAdHoc() {
        return adHoc != null;
    }

    private String snapshotOf(String versionString) {
        return snapshot ? versionString + DASH_SNAPSHOT : versionString;
    }

    public boolean isBefore(Version version) {
        return compareTo(version) == -1;
    }

    public boolean isAfter(Version version) {
        return compareTo(version) == 1;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    private static final String DASH_SNAPSHOT = "-SNAPSHOT";

    private static boolean initial(long... parts) {
        if (gotVarArgs(parts)) {
            for (long l : parts) {
                if (l != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean gotVarArgs(long... parts) {
        return parts != null && parts.length > 0;
    }

    private static long[] parts(String version) {
        String[] split = removeSnapshotSuffix(version).split("\\.");
        long[] parts = new long[split.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                parts[i] = Long.parseLong(split[i]);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return parts;
    }

    private static boolean isSnapshot(String version) {
        return version.endsWith(DASH_SNAPSHOT);
    }

    private static String removeSnapshotSuffix(String version) {
        return isSnapshot(version) ? version.substring(0, version.length() - DASH_SNAPSHOT.length())
                : version;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + toVersionString() + "]";
    }

    @Override
    public int hashCode() {
        return adHoc == null ? Arrays.hashCode(parts) : adHoc.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Version) {
            Version version = (Version) obj;
            return version != null &&Arrays.equals(parts, version.parts);
        }
        return false;
    }

    @Override
    public int compareTo(Version version) {
        adHocFailsTo("compare myself to " + version);
        version.adHocFailsTo("compare itself to " + this);
        if (this.isInitial()) {
            return version.isInitial() ? 0 : -1;
        }
        if (version.isInitial()) {
            return 1;
        }
        long[] other = version.parts;
        int otherLen = other.length;
        int thisLen = parts.length;
        int len = Math.min(thisLen, otherLen);
        for (int i = 0; i < len; i++) {
            if (this.parts[i] > other[i]) {
                return 1;
            }
            if (this.parts[i] < other[i]) {
                return -1;
            }
        }
        return thisLen < otherLen ? -1
                : thisLen > otherLen ? 1
                        : this.snapshot && !version.snapshot ? 1
                                : !this.snapshot && version.snapshot ? -1
                                        : 0;
    }
}
