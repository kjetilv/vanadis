package vanadis.jmx;

import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.core.collections.Generic;

import javax.management.DynamicMBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ManagedDynamicMBeans {

    private final Object LOCK = new Object();

    private final Set<Object> undigested = Generic.set();

    private final Map<Object, ManagedDynamicMBeanType> digests = new TypeMap();

    private final Map<Object, ManagedDynamicMBeanType> fullDigests = new TypeMap();

    public final DynamicMBean create(Object target) {
        return create(target, null, false);
    }

    public final DynamicMBean create(Object target, String description) {
        return create(target, description, false);
    }

    public final DynamicMBean createFull(Object target) {
        return create(target, null, true);
    }

    public final DynamicMBean createFull(Object target, String description) {
        return create(target, description, true);
    }

    protected Object key(Class<?> type) {
        return System.identityHashCode(type) + "-" + System.identityHashCode(type.getClassLoader());
    }

    public ManagedDynamicMBeanType mbeanType(Class<?> type, boolean full) {
        Object key = key(type);
        synchronized (LOCK) {
            return undigested.contains(key) ? null : resolve(type, full, key);
        }
    }

    private DynamicMBean create(Object target, String description, boolean full) {
        Class<?> type = target.getClass();
        ManagedDynamicMBeanType beanType = mbeanType(type, full);
        return beanType == null ? null : beanType.bean(target, description);
    }

    private ManagedDynamicMBeanType resolve(Class<?> type, boolean full, Object key) {
        Map<Object, ManagedDynamicMBeanType> digs = full ? fullDigests : digests;
        ManagedDynamicMBeanType existing = digs.get(key);
        if (existing == null) {
            AnnotationsDigest digest = newDigest(type, full);
            if (digest == null) {
                undigested.add(key);
                return null;
            }
            return newType(key, type, digs, digest);
        }
        return existing;
    }

    private static ManagedDynamicMBeanType newType(Object key, Class<?> type,
                                                   Map<Object, ManagedDynamicMBeanType> digs,
                                                   AnnotationsDigest digest) {
        ManagedDynamicMBeanType beanType = new ManagedDynamicMBeanType(digest, type);
        digs.put(key, beanType);
        return beanType;
    }

    private static AnnotationsDigest newDigest(Class<?> type, boolean full) {
        AnnotationsDigest digest = full
            ? AnnotationsDigests.createFullFromType(type)
            : AnnotationsDigests.createFromType(type);
        return isDigestable(digest)
            ? digest
            : null;
    }

    private static boolean isDigestable(AnnotationsDigest digest) {
        return digest.hasClassData(Managed.class) ||
            digest.hasMethodData(Attr.class, Operation.class) ||
            digest.hasFieldData(Attr.class);
    }

    private static class TypeMap extends LinkedHashMap<Object, ManagedDynamicMBeanType> {
        private static final long serialVersionUID = -1042710807356175911L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, ManagedDynamicMBeanType> eldest) {
            return size() > 256;
        }
    }
}
