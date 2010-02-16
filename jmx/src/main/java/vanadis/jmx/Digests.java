package vanadis.jmx;

import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.core.collections.Generic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Digests {

    static final Object LOCK = new Object();

    static final Set<Integer> undigested = Generic.set();

    static final Map<Integer, AnnotationsDigest> digests = new DigestMap();

    static final Map<Integer, AnnotationsDigest> fullDigests = new DigestMap();

    static AnnotationsDigest get(Object target) {
        return get(target, false);
    }

    static AnnotationsDigest get(Object target, boolean full) {
        Integer key = System.identityHashCode(target.getClass());
        synchronized (LOCK) {
            if (undigested.contains(key)) {
                return null;
            }
            Map<Integer, AnnotationsDigest> digs = full ? fullDigests : digests;
            AnnotationsDigest existing = digs.get(key);
            if (existing == null) {
                AnnotationsDigest digest = newDigest(target, full);
                if (digest == null) {
                    undigested.add(key);
                    return null;
                }
                digs.put(key, digest);
                return digest;
            }
            return existing;
        }
    }

    static AnnotationsDigest newDigest(Object target, boolean full) {
        AnnotationsDigest digest = full
            ? AnnotationsDigests.createFullFromInstance(target)
            : AnnotationsDigests.createFromInstance(target);
        if (digest.hasClassData(Managed.class) ||
            digest.hasMethodData(Attr.class, Operation.class) ||
            digest.hasFieldData(Attr.class)) {
            return digest;
        }
        return null;
    }

    private static class DigestMap extends LinkedHashMap<Integer, AnnotationsDigest> {
        private static final long serialVersionUID = -1042710807356175911L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, AnnotationsDigest> eldest) {
            return size() > 256;
        }
    }
}
