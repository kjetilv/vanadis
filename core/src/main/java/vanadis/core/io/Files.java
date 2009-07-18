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

import vanadis.core.collections.Pair;
import vanadis.core.lang.Not;
import vanadis.core.lang.VarArgs;
import vanadis.core.system.VM;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public final class Files {

    public static File createFromURI(String uriString) {
        URI uri;
        try {
            uri = URI.create(uriString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI string: " + uriString, e);
        }
        return create(uri);
    }

    public static File create(URI uri) {
        try {
            return new File(uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid file URI: " + uri.toASCIIString(), e);
        }
    }

    public static File createDirectory(File root, String... subdirs) {
        return existingOrNew(directoryPath(root, subdirs));
    }

    public static File getDirectory(File root, String... strings) {
        return directoryPath(root, strings);
    }

    public static File getExistingDirectory(File root, String... strings) {
        return existing(directoryPath(root, strings));
    }

    public static LineNumberReader lines(File file) {
        return IO.linesFrom(readFile(file));
    }

    public static LineNumberReader lines(File file, String enc) {
        return IO.linesFrom(readFile(file, enc));
    }

    public static Reader readFile(File file) {
        return readFile(file, IO.ENC);
    }

    public static Reader readFile(File file, String enc) {
        InputStream stream = streamFile(file);
        try {
            return new InputStreamReader(stream, enc);
        } catch (UnsupportedEncodingException e) {
            return IO.failEncoding(enc, e);
        }
    }

    public static FileInputStream streamFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Unknown file: " + file, e);
        }
    }

    public static File copy(File source, File target) {
        verifyReadableSource(source);
        verifyWritableTarget(target);
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        FileInputStream in = fileInputStream(source);
        FileChannel inChannel = in.getChannel();
        FileOutputStream out = fileOutputStream(target);
        FileChannel outChannel = out.getChannel();
        try {
            int read;
            while (true) {
                read = inChannel.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    outChannel.write(buffer);
                    buffer.clear();
                } else if (read < 0) {
                    return target;
                }
            }
        } catch (IOException e) {
            throw new IORuntimeException
                    ("Failed to copy data from " + source + " to " + target, e);
        } finally {
            Closeables.close(in, out);
        }
    }

    public static File writeFile(File path, String... contents) {
        return writeFile(path, IO.UTF_8, contents);
    }

    public static File writeFile(File path, Charset charset, String... contents) {
        verifyWritableTarget(path);
        Writer writer = openWriter(path, charset);
        try {
            writeLines(path, writer, contents);
        } finally {
            Closeables.close(writer);
        }
        return path;
    }

    public static String toString(File logFile, Pair<String, Pattern>... lookouts) {
        long len = logFile.length();
        if (len > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("File too big! " + logFile + " is " + len + " bytes");
        }
        return IO.toString(new LineIterable(logFile), new StringBuilder((int) len), lookouts);
    }

    public static void toString(File logFile, PrintStream printStream, Pair<String, Pattern>... lookouts) {
        IO.toString(new LineIterable(logFile), printStream, lookouts);
    }

    private static void verifyReadableSource(File path) {
        if (!path.exists()) {
            throw new IllegalArgumentException("No such file: " + path);
        }
        if (path.isDirectory()) {
            throw new IllegalArgumentException("Not a file: " + path);
        }
    }

    private static void verifyWritableTarget(File path) {
        if (!path.getParentFile().exists()) {
            throw new IllegalArgumentException("No such directory: " + path);
        }
        if (path.isDirectory()) {
            throw new IllegalArgumentException("Not a file: " + path);
        }
        if (path.exists() && !path.delete()) {
            throw new IllegalArgumentException
                    ("Existing file could not be deleted: " + path);
        }
    }

    private static Writer openWriter(File path, Charset charset) {
        FileOutputStream fileOutputStream = fileOutputStream(path);
        return new PrintWriter(new OutputStreamWriter(fileOutputStream, charset));
    }

    private static void writeLines(File path, Writer writer, String... contents) {
        for (String line : contents) {
            try {
                writer.write(line.trim() + VM.LN);
            } catch (IOException e) {
                throw new IORuntimeException
                        ("Failed to write line to " + path + ": '" + line + "'", e);
            }
        }
    }

    private static FileOutputStream fileOutputStream(File path) {
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            throw new IORuntimeException("Failed to write to " + path, e);
        }
    }

    private static FileInputStream fileInputStream(File path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new IORuntimeException("Failed to open " + path, e);
        }
    }

    private static File existingOrNew(File directoryPath) {
        if (directoryPath.exists()) {
            return directoryPath;
        }
        if (directoryPath.mkdirs()) {
            return directoryPath;
        }
        throw new IllegalArgumentException("Failed to create " + directoryPath);
    }

    private static File existing(File directoryPath) {
        return directoryPath.exists() ? directoryPath : null;
    }

    private static File directoryPath(File root, String... subdirs) {
        Not.nil(root, "root");
        verifyNotAnExistingFile(root);
        if (!VarArgs.present(subdirs)) {
            return root;
        }
        File pathSpelunker = root;
        for (String subdir : subdirs) {
            pathSpelunker = verifyNotAnExistingFile(new File(pathSpelunker, subdir));
        }
        return pathSpelunker;
    }

    private static File verifyNotAnExistingFile(File path) {
        if (path.isFile()) { // which also checks its existence
            throw new IllegalArgumentException("Path exists and is not a directory: " + path);
        }
        return path;
    }

    private Files() {}
}
