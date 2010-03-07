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
package vanadis.annopro;

import vanadis.core.collections.Generic;
import vanadis.core.collections.Iterables;
import vanadis.core.collections.Pair;
import vanadis.core.collections.SuperclassIterable;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

final class AnnotationsDigestsImpl implements AnnotationsDigest {

    private final boolean inherits;

    private final Map<String, AnnotationDatum<Class<?>>> classAnnotations;

    private final Map<String, List<AnnotationDatum<Method>>> methodsByType;

    private final Map<String, List<AnnotationDatum<Constructor>>> constructorsByType;

    private final Map<String, List<AnnotationDatum<Field>>> fieldsByType;

    private final Map<Constructor, List<AnnotationDatum<Constructor>>> constructorAnnotations;

    private final Map<Method, List<AnnotationDatum<Method>>> methodAnnotations;

    private final Map<Field, List<AnnotationDatum<Field>>> fieldAnnotations;

    private final Iterable<AnnotationDatum<Constructor>> constructors;

    private final Iterable<AnnotationDatum<Method>> methods;

    private final Iterable<AnnotationDatum<Field>> fields;

    private final List<Class<?>> typeChain;

    private final Map<Constructor, List<List<AnnotationDatum<Integer>>>> constructorParametersAnnotations;

    private final Map<Method, List<List<AnnotationDatum<Integer>>>> methodParametersAnnotations;

    AnnotationsDigestsImpl(Class<?> type, boolean inherits) {
        this(Not.nil(type, "type"), null, inherits, null);
    }

    AnnotationsDigestsImpl(InputStream bytecode) {
        this(null, Not.nil(bytecode, "byte code"), false, null);
    }

    AnnotationsDigestsImpl(InputStream bytecode, String targetAnnotation) {
        this(null, bytecode, false, targetAnnotation);
    }

    private AnnotationsDigestsImpl(Class<?> type, InputStream bytecode, boolean inherits,
                                   String targetAnnotation) {
        if (type == null && bytecode == null) {
            throw new IllegalArgumentException("Expected non-null class object or bytecode");
        }
        this.inherits = inherits;
        AnnotationReader reader;
        if (type == null) {
            this.typeChain = null;
            reader = new BytecodesReader(bytecode, targetAnnotation);
        } else {
            this.typeChain = typeChain(type);
            reader = new ClassObjectReader(typeChain);
        }
        this.classAnnotations = reader.annotations();
        this.fieldAnnotations = reader.readAllFields();
        this.constructorAnnotations = reader.readAllConstructors();
        this.constructorParametersAnnotations = reader.readAllConstructorParameters();
        this.methodAnnotations = reader.readAllMethods();
        this.methodParametersAnnotations = reader.readAllMethodParameters();
        this.fieldsByType = annotatedFields();
        this.methodsByType = annotatedMetods();
        this.constructorsByType = annotatedConstructors();
        this.fields = Iterables.chain(fieldAnnotations.values());
        this.methods = Iterables.chain(methodAnnotations.values());
        this.constructors = Iterables.chain(constructorAnnotations.values());
        if (inherits) {
            indexMethodOverrides();
        }
    }

    @Override
    public Iterable<AnnotationDatum<Method>> methodData(Method method) {
        return methodAnnotations.get(method);
    }

    @Override
    public Iterable<AnnotationDatum<Field>> fieldData(Field field) {
        return fieldAnnotations.get(field);
    }

    @Override
    public List<List<AnnotationDatum<Integer>>> parameterData(Constructor constructor) {
        return constructorParametersAnnotations.get(constructor);
    }

    @Override
    public List<List<AnnotationDatum<Integer>>> parameterData(Method method) {
        return methodParametersAnnotations.get(method);
    }

    @Override
    public Iterable<AnnotationDatum<Class<?>>> classData() {
        return classAnnotations.values();
    }

    @Override
    public Iterable<AnnotationDatum<Method>> methodData() {
        return methods;
    }

    @Override
    public Iterable<AnnotationDatum<Field>> fieldData() {
        return fields;
    }

    @Override
    public Map<String, AnnotationDatum<?>> getAccessibleDataIndex(Class<?> type) {
        Map<String, AnnotationDatum<?>> data = Generic.map();
        Map<String, AnnotationDatum<Field>> fieldIndex = getFieldDataIndex(type);
        Map<String, AnnotationDatum<Method>> methodIndex = getMethodDataIndex(type);
        Set<String> intersectingKeys = intersectingKeys(fieldIndex, methodIndex);
        if (intersectingKeys.isEmpty()) {
            data.putAll(fieldIndex);
            data.putAll(methodIndex);
            return data;
        }
        throw new IllegalStateException
                (this + " has intersecting field/method keys: " + intersectingKeys + ", refusing to give out index!");
    }

    @Override
    public List<AnnotationDatum<?>> getAccessibleData(Class<?> type) {
        List<AnnotationDatum<?>> data = Generic.list();
        data.addAll(getMethodData(type));
        data.addAll(getFieldData(type));
        return data;
    }

    @Override
    public List<AnnotationDatum<Class<?>>> getClassData(Class<?>... types) {
        return getClassData(ToString.array(types));
    }

    @Override
    public List<AnnotationDatum<Class<?>>> getClassData(String... types) {
        List<AnnotationDatum<Class<?>>> data = Generic.list(types.length);
        for (String type : types) {
            data.add(classAnnotations.get(type));
        }
        return data;
    }

    @Override
    public AnnotationDatum<Class<?>> getClassDatum(Class<?> type) {
        return getClassDatum(name(type));
    }

    @Override
    public AnnotationDatum<Class<?>> getClassDatum(String type) {
        return classAnnotations.get(type);
    }

    @Override
    public List<AnnotationDatum<Field>> getFieldData(Class<?> type) {
        return getFieldData(name(type));
    }

    @Override
    public List<AnnotationDatum<Method>> getMethodData(Class<?> type) {
        return getMethodData(name(type));
    }

    @Override
    public List<AnnotationDatum<Constructor>> getConstructorData(Class<?> type) {
        return getConstructorData(name(type));
    }

    @Override
    public List<AnnotationDatum<Field>> getFieldData(String type) {
        List<AnnotationDatum<Field>> data = fieldsByType.get(type);
        return data == null ? Collections.<AnnotationDatum<Field>>emptyList() : data;
    }

    @Override
    public List<AnnotationDatum<Method>> getMethodData(String type) {
        List<AnnotationDatum<Method>> data = methodsByType.get(type);
        return data == null ? Collections.<AnnotationDatum<Method>>emptyList() : data;
    }

    @Override
    public List<AnnotationDatum<Constructor>> getConstructorData(String type) {
        List<AnnotationDatum<Constructor>> data = constructorsByType.get(type);
        return data == null ? Collections.<AnnotationDatum<Constructor>>emptyList() : data;
    }

    @Override
    public Map<String, AnnotationDatum<Field>> getFieldDataIndex(Class<?> type) {
        Map<String, AnnotationDatum<Field>> map = Generic.map();
        for (AnnotationDatum<Field> datum : getFieldData(type)) {
            Field field = datum.getElement();
            map.put(field.getName(), datum);
        }
        return map;
    }

    @Override
    public Map<Constructor, List<List<AnnotationDatum<Integer>>>> getConstructorParameterDataIndex(Class<?> type) {
        Map<Constructor, List<List<AnnotationDatum<Integer>>>> map = Generic.map();
        for (Map.Entry<Constructor,List<List<AnnotationDatum<Integer>>>> entry : constructorParametersAnnotations.entrySet()) {
            List<List<AnnotationDatum<Integer>>> filteredDataLists = Generic.list();
            List<List<AnnotationDatum<Integer>>> storedDataLists = entry.getValue();
            for (List<AnnotationDatum<Integer>> storedData : storedDataLists) {
                List<AnnotationDatum<Integer>> filteredData = Generic.list();
                for (AnnotationDatum<Integer> storedDatum : storedData) {
                    if (storedDatum.isType(type)) { filteredData.add(storedDatum); }
                }
                filteredDataLists.add(filteredData);
            }
            map.put(entry.getKey(), filteredDataLists);
        }
        return map;
    }

    @Override
    public Map<Method, List<List<AnnotationDatum<Integer>>>> getMethodParameterDataIndex(Class<?> type) {
        Map<Method, List<List<AnnotationDatum<Integer>>>> map = Generic.map();
        for (AnnotationDatum<Method> datum : getMethodData(type)) {
            Method element = datum.getElement();
            map.put(element, methodParametersAnnotations.get(datum.getElement()));
        }
        return map;
    }

    @Override
    public Map<String, AnnotationDatum<Method>> getMethodDataIndex(Class<?> type) {
        Map<String, AnnotationDatum<Method>> map = Generic.map();
        for (AnnotationDatum<Method> datum : getMethodData(type)) {
            Method method = datum.getElement();
            map.put(method.getName(), datum);
        }
        return map;
    }

    @Override
    public boolean hasMethodData(Class<?>... classes) {
        return checkMap(methodsByType, classes);
    }

    @Override
    public boolean hasFieldData(Class<?>... classes) {
        return checkMap(fieldsByType, classes);
    }

    @Override
    public boolean hasClassData(Class<?>... classes) {
        return checkMap(classAnnotations, classes);
    }

    @Override
    public boolean hasParamData(Class<?>... classes) {
        for (Map.Entry<Method, List<List<AnnotationDatum<Integer>>>> entry : methodParametersAnnotations.entrySet()) {
            for (List<AnnotationDatum<Integer>> methods : entry.getValue()) {
                for (Class<?> annotationClass : classes) {
                    for (AnnotationDatum<Integer> datum : methods) {
                        if (datum.isType(annotationClass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkMap(Map<String, ?> map, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz != null && map.containsKey(clazz.getName())) {
                return true;
            }
        }
        return false;
    }

    private Map<Pair<String, Integer>, List<Method>> nameCardinalityMethods() {
        Map<Pair<String, Integer>, List<Method>> map = Generic.map();
        for (Class<?> type : typeChain) {
            for (Method method : type.getDeclaredMethods()) {
                Pair<String, Integer> key = key(method);
                List<Method> methods = map.get(key);
                if (methods == null) {
                    List<Method> list = Generic.list();
                    list.add(method);
                    map.put(key, list);
                } else {
                    methods.add(method);
                }
            }
        }
        return map;
    }

    private Map<String, List<AnnotationDatum<Method>>> annotatedMetods() {
        return mapByName(methodAnnotations);
    }

    private Map<String, List<AnnotationDatum<Constructor>>> annotatedConstructors() {
        return mapByName(constructorAnnotations);
    }

    private Map<String, List<AnnotationDatum<Field>>> annotatedFields() {
        return mapByName(fieldAnnotations);
    }

    private <T> Map<String,List<AnnotationDatum<T>>> mapByName(Map<T, List<AnnotationDatum<T>>> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<AnnotationDatum<T>>> byName = Generic.map();
        for (Map.Entry<T, List<AnnotationDatum<T>>> entry : map.entrySet()) {
            for (AnnotationDatum<T> data : entry.getValue()) {
                mappedList(byName, data.annotationType()).add(data);
            }
        }
        return byName;
    }

    private void indexMethodOverrides() {
        if (methodAnnotations == null) {
            return;
        }
        Map<Pair<String, Integer>, List<Method>> nameCardinalityMethods = nameCardinalityMethods();
        for (Method annotatedMethod : Generic.set(methodAnnotations.keySet())) {
            List<Method> otherMethods = nameCardinalityMethods.get(key(annotatedMethod));
            for (Method otherMethod : otherMethods) {
                if (!methodAnnotations.containsKey(otherMethod) && overrides(otherMethod, annotatedMethod, true)) {
                    List<AnnotationDatum<Method>> annotationData = methodAnnotations.get(annotatedMethod);
                    methodAnnotations.put(otherMethod, annotationData);
                }
            }
        }
    }

    private static String name(Class<?> type) {
        return Not.nil(type, "type").getName();
    }

    private static Pair<String, Integer> key(Method method) {
        return Pair.of(method.getName(), method.getParameterTypes().length);
    }

    private static <K, T> List<T> mappedList(Map<K, List<T>> map, K data) {
        List<T> list = map.get(data);
        if (list == null) {
            List<T> newList = Generic.list();
            map.put(data, newList);
            return newList;
        } else {
            return list;
        }
    }

    private static boolean overrides(Method overridee, Method overrider, boolean includeSelf) {
        if (!overridee.getName().equals(overrider.getName())) {
            return false;
        }
        Class<?> overriderType = overrider.getReturnType();
        Class<?> overrideeType = overridee.getReturnType();
        if (overriderType == overrideeType && !includeSelf) {
            return false;
        }
        boolean returnTypeOverride = overriderType.isAssignableFrom(overrideeType);
        if (returnTypeOverride) {
            Class<?>[] methodTypes = overrider.getParameterTypes();
            Class<?>[] referenceTypes = overridee.getParameterTypes();
            return referenceTypes.length == methodTypes.length &&
                    Arrays.equals(referenceTypes, methodTypes);
        }
        return false;
    }

    private List<Class<?>> typeChain(Class<?> type) {
        List<Class<?>> types = Generic.list();
        if (inherits) {
            for (Class<?> sc : new SuperclassIterable(type, true, false)) {
                types.add(sc);
                collectInterfaces(types, sc);
            }
        } else {
            types.add(type);
        }
        return types;
    }

    private static <K, AE1, AE2> Set<K> intersectingKeys
            (Map<K, AnnotationDatum<AE1>> fieldIndex,
             Map<K, AnnotationDatum<AE2>> methodIndex) {
        Set<K> intersectingKeys = Generic.set(fieldIndex.keySet());
        intersectingKeys.retainAll(methodIndex.keySet());
        return intersectingKeys;
    }

    private static void collectInterfaces(List<Class<?>> types, Class<?> sc) {
        Class<?>[] interfaces = sc.getInterfaces();
        if (interfaces != null && interfaces.length != 0) {
            for (Class<?> interphase : interfaces) {
                if (!types.contains(interphase)) {
                    types.add(interphase);
                    collectInterfaces(types, interphase);
                }
            }
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "types:" + typeChain,
                           "annotations", classAnnotations.keySet(),
                           "fields", fieldsByType.size(),
                           "methods", methodsByType.size());
    }
}
