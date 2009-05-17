package vanadis.util.mvn;

import vanadis.core.collections.Pair;
import vanadis.core.test.ForTestingPurposes;

import java.io.File;

class VersionResolver {

    @ForTestingPurposes
    static FileData fileData(String name, long time) {
        return new FileData(name, time);
    }

    static FileData[] fileData(File[] files) {
        FileData[] fileData = new FileData[files.length];
        for (int i = 0; i < files.length; i++) {
            fileData[i] = fileData(files[i].getName(), files[i].lastModified());
        }
        return fileData;
    }

    static String resolve(File... candidates) {
        return resolve(fileData(candidates));
    }

    static String resolve(FileData... candidates) {
        FileData latest = null;
        FileData snapshot = null;
        for (FileData file : candidates) {
            if (isSnapshotJar(file)) {
                snapshot = file;
            } else {
                latest = latest(latest, file);
            }
        }
        if (snapshot == null) {
            return latest.getName();
        }
        if (latest == null) {
            return snapshot.getName();
        }
        return snapshot.lastModified() > latest.lastModified()
                ? snapshot.getName()
                : latest.getName();
    }

    private static boolean isSnapshotJar(FileData file) {
        return file.getName().contains("SNAPSHOT");
    }

    private static FileData latest(FileData latest, FileData snapshot) {
        if (latest == null) {
            return snapshot;
        }
        int compare = latest.getName().compareTo(snapshot.getName());
        return compare > 0 ? latest : snapshot;
    }

    static class FileData extends Pair<String,Long> {

        private FileData(String one, Long two) {
            super(one, two);
        }

        String getName() {
            return getOne();
        }

        long lastModified() {
            return getTwo();
        }
    }
}
