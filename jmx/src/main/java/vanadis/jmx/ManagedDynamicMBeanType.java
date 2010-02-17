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
package vanadis.jmx;

import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static vanadis.jmx.JmxFiddly.*;

public class ManagedDynamicMBeanType {

    private final Class<?> type;

    private final ObjectName objectName;

    public static ManagedDynamicMBeanType create(Class<?> type) {
        return create(type, false);
    }

    public static ManagedDynamicMBeanType createFull(Class<?> type) {
        return create(type, true);
    }

    public static ManagedDynamicMBeanType create(Class<?> type, boolean full) {
        AnnotationsDigest digest;
        Integer key = System.identityHashCode(type);
        synchronized (LOCK) {
            if (undigested.contains(key)) {
                return null;
            }
            Map<Integer, ManagedDynamicMBeanType> digs = full ? fullDigests : digests;
            ManagedDynamicMBeanType existing = digs.get(key);
            if (existing == null) {
                digest = newDigest(type, full);
                if (digest == null) {
                    undigested.add(key);
                    return null;
                }
                ManagedDynamicMBeanType beanType = new ManagedDynamicMBeanType(digest, type);
                digs.put(key, beanType);
                return beanType;
            }
            return existing;
        }
    }

    static final Object LOCK = new Object();

    static final Set<Integer> undigested = Generic.set();

    static final Map<Integer, ManagedDynamicMBeanType> digests = new TypeMap();

    static final Map<Integer, ManagedDynamicMBeanType> fullDigests = new TypeMap();

    static AnnotationsDigest newDigest(Class<?> type, boolean full) {
        AnnotationsDigest digest = full ? AnnotationsDigests.createFullFromType(type)
            : AnnotationsDigests.createFromType(type);
        if (digest.hasClassData(Managed.class) ||
            digest.hasMethodData(Attr.class, Operation.class) ||
            digest.hasFieldData(Attr.class)) {
            return digest;
        }
        return null;
    }

    private static class TypeMap extends LinkedHashMap<Integer, ManagedDynamicMBeanType> {
        private static final long serialVersionUID = -1042710807356175911L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, ManagedDynamicMBeanType> eldest) {
            return size() > 256;
        }
    }

    private final Map<String,MBeanAttributeInfo> attributeInfos;

    private final Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributeMethods;

    private final Map<String, AnnotationDatum<Method>> operations;

    private final Map<String, AnnotationDatum<Field>> attributeFields;

    private final MBeanInfo mBeanInfo;

    private ManagedDynamicMBeanType(AnnotationsDigest digest, Class<?> type) {
        this.type = type;
        Not.nil(digest, "digest");
        Managed managed = managed(digest);
        objectName = objectName(managed, type);
        attributeMethods = organizeAttributes(digest.getMethodData(Attr.class));
        attributeFields = digest.getFieldDataIndex(Attr.class);
        operations = digest.getMethodDataIndex(Operation.class);
        MBeanAttributeInfo[] attributeInfoArray = attributeInfos();
        MBeanOperationInfo[] operationInfoArray = operationInfos();
        this.mBeanInfo = info(type, null, managed, attributeInfoArray, operationInfoArray);
        this.attributeInfos = map(attributeInfoArray);
    }

    public Class<?> getType() {
        return type;
    }

    public ManagedDynamicMBean bean(Object target) {
        return bean(target, null);
    }

    public ManagedDynamicMBean bean(ObjectName objectName, Object target) {
        return bean(objectName, target, null);
    }

    public ManagedDynamicMBean bean(Object target, String description) {
        return bean(null, target, description);
    }

    public ManagedDynamicMBean bean(ObjectName objectName, Object target, String description) {
        return new ManagedDynamicMBean(info(this.mBeanInfo, description),
                                       objectName == null ? this.objectName : objectName,
                                       this,
                                       target);
    }

    private MBeanOperationInfo[] operationInfos() {
        List<MBeanOperationInfo> infos = Generic.list(operations.size());
        for (Map.Entry<String, AnnotationDatum<Method>> entry : operations.entrySet()) {
            AnnotationDatum<Method> datum = entry.getValue();
            infos.add(beanOperationInfo(datum));
        }
        return infos.toArray(new MBeanOperationInfo[infos.size()]);
    }

    private MBeanAttributeInfo[] attributeInfos() {
        List<MBeanAttributeInfo> infos = Generic.list(attributeMethods.size() + attributeFields.size());
        for (Map.Entry<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> entry : attributeMethods.entrySet()) {
            String name = entry.getKey();
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair = entry.getValue();
            infos.add(beanAttributeInfo(name, pair));
        }
        for (Map.Entry<String, AnnotationDatum<Field>> entry : attributeFields.entrySet()) {
            String name = entry.getKey();
            Field field = entry.getValue().getElement();
            infos.add(beanAttributeInfo(name, field));
        }
        return infos.toArray(new MBeanAttributeInfo[infos.size()]);
    }
    
    private static ObjectName objectName(Managed managedAnnotation, Class<?> type) {
        if (managedAnnotation == null) {
            return null;
        }
        String objectName = managedAnnotation.objectName();
        if (Strings.isEmpty(objectName)) {
            return null;
        }
        try {
            return new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name on " + type, e);
        }
    }

    private static <T extends MBeanFeatureInfo> Map<String,T> map(T[] infos) {
        Map<String,T> map = Generic.map();
        for (T attributeInfo : infos) {
            map.put(attributeInfo.getName(), attributeInfo);
        }
        return map;
    }

    private static Managed mng(AnnotationDatum<Class<?>> datum) {
        return datum.createProxy(ManagedDynamicMBeanType.class.getClassLoader(), Managed.class);
    }

    private static Managed managed(AnnotationsDigest digest) {
        AnnotationDatum<Class<?>> datum = digest.getClassDatum(Managed.class);
        return datum == null ? null : mng(datum);
    }

    @Override
    public String toString() {
        return ToString.of(this, "type", type);
    }

    public Map<String, AnnotationDatum<Method>> getOperations() {
        return operations;
    }

    public Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> getAttributeMethods() {
        return attributeMethods;
    }

    public Map<String, AnnotationDatum<Field>> getAttributeFields() {
        return attributeFields;
    }

    public Map<String, MBeanAttributeInfo> getAttributeInfos() {
        return attributeInfos;
    }
}