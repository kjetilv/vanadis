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

    static final Map<Integer, AnnotationsDigest> digests = new LinkedHashMap<Integer, AnnotationsDigest>() {
        private static final long serialVersionUID = -1042710807356175911L;
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, AnnotationsDigest> eldest) {
            return size() > 256;
        }
    };

    static AnnotationsDigest get(Object target) {
        Integer key = System.identityHashCode(target.getClass());
        synchronized (LOCK) {
            if (undigested.contains(key)) {
                return null;
            }
            AnnotationsDigest existing = digests.get(key);
            if (existing == null) {
                AnnotationsDigest digest = newDigest(target);
                if (digest == null) {
                    undigested.add(key);
                    return null;
                }
                digests.put(key, digest);
                return digest;
            }
            return existing;
        }
    }

    static AnnotationsDigest newDigest(Object target) {
        AnnotationsDigest digest = AnnotationsDigests.createFromInstance(target);
        if (digest.hasClassData(Managed.class) ||
            digest.hasMethodData(Attr.class, Operation.class) ||
            digest.hasFieldData(Attr.class)) {
            return digest;
        }
        return null;
    }
}
