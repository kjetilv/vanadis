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
package vanadis.deployer;

import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.time.Time;

import java.io.File;
import java.net.URI;

final class Discovery {

    private final URI uri;

    private final Time time;

    private final File file;

    private final Time modified;

    private final long length;

    Discovery(File file, URI uri, Time time) {
        this.uri = uri;
        this.time = time;
        this.file = Not.nil(file, "file");
        this.modified = Time.modified(file);
        this.length = file.length();
    }

    public boolean isUpdated(File file) {
        return Time.modified(file).isAfter(this.modified) || file.length() != this.length;
    }

    public URI getURL() {
        return uri;
    }

    @Override
    public String toString() {
        return ToString.of(this, file, "time", time, "length", length);
    }
}
