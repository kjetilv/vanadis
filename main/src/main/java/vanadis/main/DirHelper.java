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
package vanadis.main;

import vanadis.core.io.Files;
import vanadis.core.system.VM;
import vanadis.core.time.Time;
import vanadis.launcher.StartupException;
import vanadis.util.mvn.Repo;

import java.io.File;
import java.io.IOException;
import java.net.URI;

class DirHelper {

    private static final Time GO_VM = new Time();

    private static long nameCounter;

    static URI resolveHome(File home) {
        return home != null ? validateDirectory(home, "home", false) : tmpDir();
    }

    static URI validRepo(URI home, URI repo) {
        if (repo != null) {
            if (repo.toASCIIString().startsWith("http")) {
                return repo;
            }
            File repoDirectory = Files.create(repo);
            if (repoDirectory.isAbsolute() && repoDirectory.isDirectory() && repoDirectory.canRead()) {
                return repo;
            }
            File installRepo = home == null ? canonicalize(repoDirectory) : canonicalize(
                    new File(Files.create(home), repo.toASCIIString()));
            if (installRepo.canRead() && installRepo.isDirectory()) {
                return installRepo.toURI();
            }
            throw new IllegalStateException("Invalid repository: " + installRepo);
        }
        return Repo.DEFAULT_URI;
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new StartupException("Unable to resolve canonical form of " + file, e);
        }
    }

    static URI validateDirectory(File file, String type, boolean mustExist) {
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new StartupException(type + " is not a directory: " + file);
            }
        } else {
            if (mustExist) {
                throw new StartupException(type + " directory does not exist: " + file);
            }
        }
        return canonicalize(file).toURI();
    }

    static URI tmpDir() {
        File subDir = workDir();
        if (!subDir.mkdirs()) {
            System.err.println("Failed to establish temporary work directory " + subDir);
            return VM.HOME_URI;
        }
        return subDir.toURI();
    }

    static File workDir() {
        return new File(new File(VM.TMP, "vanadis-main-tmp"), uniqueName()).getAbsoluteFile();
    }

    static String uniqueName() {
        return GO_VM.getEpoch() + "-" + VM.pid() + "-" + nameCounter++;
    }

    static URI resolveRepo(URI home, URI repo) {
        return repo == null ? Repo.DEFAULT_URI : validRepo(home, repo);
    }
}
