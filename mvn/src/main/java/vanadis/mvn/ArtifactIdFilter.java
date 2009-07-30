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

package vanadis.mvn;

import java.io.File;
import java.io.FileFilter;

class ArtifactIdFilter implements FileFilter {

    private final String prefix;

    private final String suffix;

    private final String[] negativeSuffixes;

    ArtifactIdFilter(Coordinate coordinate, String suffix, String... negativeSuffixes) {
        this.suffix = suffix;
        this.negativeSuffixes = negativeSuffixes;
        this.prefix = coordinate.getArtifactId() + "-";
    }

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        if (name.startsWith(prefix) && name.endsWith(suffix)) {
            for (String negative : negativeSuffixes) {
                if (name.endsWith(negative)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
