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

    private final Set<Object> undigestable = Generic.set();

    private final Map<Object, ManagedDynamicMBeanType> digests = new TypeMap();

    private final Map<Object, ManagedDynamicMBeanType> fullDigests = new TypeMap();

    private JmxMapping mapping;

    public ManagedDynamicMBeans(JmxMapping mapping) {
        this.mapping = mapping;
    }

    public ManagedDynamicMBeans() {
        this(JmxMapping.DEFAULT);
    }

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
        return type;
    }

    public ManagedDynamicMBeanType mbeanType(Class<?> type, boolean full) {
        Object key = key(type);
        synchronized (LOCK) {
            return isUndigestable(key) ? null : digest(key, type, full, full ? fullDigests : digests);
        }
    }

    private boolean isUndigestable(Object key) {
        return undigestable.contains(key);
    }

    private ManagedDynamicMBeanType digest(Object key, Class<?> type, boolean full,
                                            Map<Object, ManagedDynamicMBeanType> digests) {
        ManagedDynamicMBeanType existing = digests.get(key);
        if (existing == null) {
                AnnotationsDigest digest = newDigest(type, full);
                if (digest == null) {
                    undigestable.add(key);
                    return null;
                }
                ManagedDynamicMBeanType beanType = new ManagedDynamicMBeanType(digest, type, mapping);
                digests.put(key, beanType);
                return beanType;
            }
        return existing;
    }

    private DynamicMBean create(Object target, String description, boolean full) {
        Class<?> type = target.getClass();
        ManagedDynamicMBeanType beanType = mbeanType(type, full);
        return beanType == null ? null : beanType.bean(target, description);
    }

    private AnnotationsDigest newDigest(Class<?> type, boolean full) {
        AnnotationsDigest digest = full
                ? AnnotationsDigests.createFullFromType(type)
                : AnnotationsDigests.createFromType(type);
        return mapping.managed(digest);
    }

    private static class TypeMap extends LinkedHashMap<Object, ManagedDynamicMBeanType> {
        private static final long serialVersionUID = -1042710807356175911L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, ManagedDynamicMBeanType> eldest) {
            return size() > 256;
        }
    }
}
