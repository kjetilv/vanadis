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
import vanadis.annopro.AnnotationMapper;
import vanadis.annopro.AnnotationsDigest;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static vanadis.jmx.JmxFiddly.*;
import static vanadis.jmx.JmxFiddly.beanAttributeInfo;

public class ManagedDynamicMBeanType {

    private final Class<?> type;

    private final ObjectName objectName;

    private final Map<String,MBeanAttributeInfo> attributeInfos;

    private final Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributeMethods;

    private final Map<String, AnnotationDatum<Method>> operations;

    private final Map<Method,List<List<AnnotationDatum<Integer>>>> operationParams;

    private final Map<String, AnnotationDatum<Field>> attributeFields;

    private final MBeanInfo mBeanInfo;

    ManagedDynamicMBeanType(AnnotationsDigest digest, Class<?> type, AnnotationMapper annotationMapper) {
        this.type = Not.nil(type, "type");

        AnnotationMapper mapper = annotationMapper == null ? JmxMapping.DEFAULT : annotationMapper;
        Managed managed = toManaged(Not.nil(digest, "digest").getClassDatum(mapper.getClientCodeType(Managed.class)));
        this.objectName = objectName(managed, type);
        this.attributeMethods = organizeMethodAttributes(digest.getMethodData(mapper.getClientCodeType(Attr.class)));
        this.attributeFields = digest.getFieldDataIndex(mapper.getClientCodeType(Attr.class));
        this.operations = digest.getMethodDataIndex(mapper.getClientCodeType(Operation.class));
        this.operationParams = digest.getMethodParameterDataIndex(mapper.getClientCodeType(Operation.class));
        MBeanAttributeInfo[] attributeInfoArray = attributeInfos();
        MBeanOperationInfo[] operationInfoArray = operationInfos(mapper.getClientCodeType(Param.class));
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

    private MBeanOperationInfo[] operationInfos(Class<?> paramType) {
        List<MBeanOperationInfo> infos = Generic.list(operations.size());
        for (Map.Entry<String, AnnotationDatum<Method>> entry : operations.entrySet()) {
            AnnotationDatum<Method> datum = entry.getValue();
            List<List<AnnotationDatum<Integer>>> params = operationParams.get(datum.getElement());
            infos.add(beanOperationInfo(datum, paramType, params));
        }
        return infos.toArray(new MBeanOperationInfo[infos.size()]);
    }

    private MBeanAttributeInfo[] attributeInfos() {
        Map<String,MBeanAttributeInfo> methodInfos = Generic.map();
        for (Map.Entry<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> entry : attributeMethods.entrySet()) {
            String name = entry.getKey();
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair = entry.getValue();
            MBeanAttributeInfo beanAttributeInfo = beanAttributeInfo(name, pair);
            methodInfos.put(beanAttributeInfo.getName(), beanAttributeInfo);
        }
        Map<String,MBeanAttributeInfo> fieldInfos = Generic.map();
        for (Map.Entry<String, AnnotationDatum<Field>> entry : attributeFields.entrySet()) {
            MBeanAttributeInfo beanAttributeInfo = beanAttributeInfo(entry.getKey(), entry.getValue());
            fieldInfos.put(beanAttributeInfo.getName(), beanAttributeInfo);
        }
        List<MBeanAttributeInfo> infos = Generic.list(methodInfos.size() + fieldInfos.size());
        Set<String> overlapping = overlappingNames(methodInfos, fieldInfos);
        for (String name : overlapping) {
            MBeanAttributeInfo method = methodInfos.remove(name);
            MBeanAttributeInfo field = fieldInfos.remove(name);
            if (method.getType().equals(field.getType())) {
                infos.add(new MBeanAttributeInfo(name, method.getType(),
                                                 better(method.getDescription(), field.getDescription()),
                                                 method.isReadable() || field.isReadable(),
                                                 method.isWritable() || field.isReadable(),
                                                 method.isIs()));
            } else {
                throw new IllegalArgumentException
                        ("Unable to resolve two attributes with the same name '" + name + "' " +
                                "and different types " + method.getType() + " and " + field.getType());
            }
        }
        infos.addAll(methodInfos.values());
        infos.addAll(fieldInfos.values());
        return infos.toArray(new MBeanAttributeInfo[infos.size()]);
    }

    private String better(String desc1, String desc2) {
        return desc1 == null && desc2 == null ? null
                : desc1 == null ? desc2
                : desc2 == null ? desc1
                : desc1.length() > desc2.length() ? desc1
                : desc2;
    }

    private Set<String> overlappingNames(Map<String, MBeanAttributeInfo> methodInfos, Map<String, MBeanAttributeInfo> fieldInfos) {
        Set<String> overlapping  = Generic.set(methodInfos.keySet());
        overlapping.retainAll(fieldInfos.keySet());
        return overlapping;
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

    private static Managed toManaged(AnnotationDatum<Class<?>> datum) {
        return datum == null ? null : datum.createProxy(ManagedDynamicMBeanType.class.getClassLoader(), Managed.class);
    }

    @Override
    public String toString() {
        return ToString.of(this, "type", type);
    }

    Map<String, AnnotationDatum<Method>> getOperations() {
        return operations;
    }

    Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> getAttributeMethods() {
        return attributeMethods;
    }

    Map<String, AnnotationDatum<Field>> getAttributeFields() {
        return attributeFields;
    }

    Map<String, MBeanAttributeInfo> getAttributeInfos() {
        return attributeInfos;
    }
}
