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

import vanadis.core.io.Closeables;
import vanadis.core.io.IORuntimeException;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

final class DirectoryBundleExplorer extends AbstractDirectoryUriExplorer {

    private static final String META_INF = "META-INF";

    private static final String MANIFEST_MF = "MANIFEST.MF";

    private static File manifestFileInDir(File file) {
        return new File(new File(file, META_INF), MANIFEST_MF);
    }

    private final File tmp;

    DirectoryBundleExplorer(File dir, File tmp, String... suffixes) {
        this(dir, tmp, true, suffixes);
    }

    private DirectoryBundleExplorer(File dir, File tmp, boolean recursive, String... suffixes) {
        super(dir, suffixes);
        this.tmp = validate(tmp);
    }

    @Override
    protected boolean isMatch(File file) {
        if (isDirectory(file)) {
            File manifest = manifestFileInDir(file);
            return manifest.exists() && manifest.isFile();
        } else {
            return isSuffixedFile(file);
        }
    }

    @Override
    protected File resolveFile(File file) {
        return file.isDirectory() ? toTemporaryJar(file) : file;
    }

    private File toTemporaryJar(File dir) {
        Manifest manifest = getManifest(dir);
        File tempFile = getTempFile(dir);
        JarOutputStream jarOutputStream = openJarOutput(manifest, tempFile);
        try {
            addEntries(jarOutputStream, dir, tempFile);
        } finally {
            Closeables.close(jarOutputStream);
        }
        return tempFile;
    }

    private void addEntries(JarOutputStream jos, File directory, File tempFile) {
        for (File file : directory.listFiles()) {
            if (!file.getName().equals(META_INF)) {
                addFile(jos, tempFile, file);
                FileInputStream inputStream = stream(file);
                try {
                    transfer(inputStream, jos);
                } finally {
                    Closeables.close(inputStream);
                }
            }
        }
    }

    private void addFile(JarOutputStream jos, File tempFile, File file) {
        JarEntry entry = new JarEntry(file.getName());
        try {
            jos.putNextEntry(entry);
        } catch (IOException e) {
            throw new IORuntimeException
                    (this + " failed to write entry " + file + " to " + tempFile, e);
        }
    }

    private FileInputStream stream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IORuntimeException
                    (this + " could not find file " + file, e);
        }
    }

    private void transfer(FileInputStream inputStream, OutputStream outputStream) {
        byte[] buffer = new byte[8192];
        while (true) {
            int read = readInto(inputStream, buffer);
            if (read == -1) {
                return;
            }
            writeInto(outputStream, buffer, read);
        }
    }

    private void writeInto(OutputStream outputStream, byte[] buffer, int bytes) {
        try {
            outputStream.write(buffer, 0, bytes);
        } catch (IOException e) {
            throw new IORuntimeException
                    (this + " failed to write to " + outputStream, e);
        }
    }

    private int readInto(FileInputStream inputStream, byte[] buffer) {
        try {
            return inputStream.read(buffer);
        } catch (IOException e) {
            throw new IORuntimeException
                    (this + " failed to read from " + inputStream, e);
        }
    }

    private JarOutputStream openJarOutput(Manifest manifest, File tempFile) {
        try {
            return new JarOutputStream(new FileOutputStream(tempFile), manifest);
        } catch (IOException e) {
            throw new IORuntimeException
                    (this + " failed to create jar file " + tempFile, e);
        }
    }

    private File getTempFile(File dir) {
        try {
            File tempFile = File.createTempFile(dir.getName(), ".jar", tmp);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new IORuntimeException
                    (this + " failed to create temp file in " + tmp, e);
        }
    }

    private Manifest getManifest(File dir) {
        File manifestFile = manifestFileInDir(dir);
        FileInputStream manifestInputStream = streamFileInDir(manifestFile, dir);
        return createManifest(manifestFile, manifestInputStream);
    }

    private Manifest createManifest(File manifestFile, FileInputStream manifestInputStream) {
        try {
            return new Manifest(manifestInputStream);
        } catch (IOException e) {
            throw new IORuntimeException(this + " failed to read manifest from " + manifestFile, e);
        } finally {
            Closeables.close(manifestInputStream);
        }
    }

    private FileInputStream streamFileInDir(File manifestFile, File dir) {
        try {
            return new FileInputStream(manifestFile);
        } catch (FileNotFoundException e) {
            throw new IORuntimeException
                    (this + " failed to read manifest from exploded bundle " + dir, e);
        }
    }
}
