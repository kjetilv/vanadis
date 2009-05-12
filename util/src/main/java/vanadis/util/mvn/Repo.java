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

package net.sf.vanadis.util.mvn;

import net.sf.vanadis.core.system.VM;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Repo {

    private static final String M2 = ".m2";

    private static final String REPOSITORY = "repository";

    public static final File DEFAULT = valid(new File(new File(VM.HOME, M2), REPOSITORY));

    public static final URI DEFAULT_URI = DEFAULT.toURI();

    private static File valid(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            try {
                return directory.getCanonicalFile();
            } catch (IOException ignored) {
                return directory.getAbsoluteFile();
            }
        }
        return null;
    }
}
