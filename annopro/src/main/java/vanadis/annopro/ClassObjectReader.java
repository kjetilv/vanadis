/*
 * Copyright 2008 Kjetil Valstadsve
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
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.Invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

final class ClassObjectReader implements AnnotationReader {

    private final List<Class<?>> typeChain;

    ClassObjectReader(List<Class<?>> typeChain) {
        this.typeChain = typeChain;
    }

    @Override
    public Map<String, AnnotationDatum<Class<?>>> annotations() {
        Map<String, AnnotationDatum<Class<?>>> map = Generic.map();
        for (Class<?> type : typeChain) {
            for (Annotation annotation : type.getDeclaredAnnotations()) {
                loadInto(map, annotation, type);
            }
        }
        return map;
    }

    @Override
    public Map<Constructor, List<List<AnnotationDatum<Integer>>>> readAllConstructorParameters() {
        Map<Constructor, List<List<AnnotationDatum<Integer>>>> map = Generic.linkedHashMap();
        Constructor<?>[] constructors = typeChain.get(0).getConstructors();
        for (Constructor<?> constructor : constructors) {
            map.put(constructor, listParameterAnnotations(constructor.getParameterAnnotations()));
        }
        return map;
    }

    @Override
    public Map<Method, List<List<AnnotationDatum<Integer>>>> readAllMethodParameters() {
        Map<Method, List<List<AnnotationDatum<Integer>>>> map = Generic.linkedHashMap();
        Set<Method> methods = allMethods();
        for (Method method : methods) {
            map.put(method, listParameterAnnotations(method.getParameterAnnotations()));
        }
        return map;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Map<Constructor, List<AnnotationDatum<Constructor>>> readAllConstructors() {
        Map<Constructor, List<AnnotationDatum<Constructor>>> constructors = Generic.linkedHashMap();
        for (Constructor<?> constructor : typeChain.get(0).getConstructors()) {
            constructors.put(constructor, readAllFromConstructor(constructor));
        }
        return constructors;
    }

    @Override
    public Map<Method, List<AnnotationDatum<Method>>> readAllMethods() {
        Set<Method> methods = allMethods();
        Map<Method, List<AnnotationDatum<Method>>> map = Generic.linkedHashMap();
        for (Method method : methods) {
            map.put(method, readAllFromMethod(method));
        }
        return map;
    }

    private Set<Method> allMethods() {
        Set<Method> methods = Generic.linkedHashSet();
        for (Class<?> clazz : typeChain) {
            addLeafMethods(methods, clazz);
        }
        return methods;
    }

    @Override
    public Map<Field, List<AnnotationDatum<Field>>> readAllFields() {
        Set<Field> fields = Generic.linkedHashSet();
        for (Class<?> clazz : typeChain) {
            addFields(fields, clazz);
        }
        Map<Field, List<AnnotationDatum<Field>>> map = Generic.linkedHashMap();
        for (Field field : fields) {
            map.put(field, readAllFromField(field));
        }
        return map;
    }

    private List<List<AnnotationDatum<Integer>>> listParameterAnnotations(Annotation[][] parametersAnnotationsArray) {
        List<List<AnnotationDatum<Integer>>> parametersAnnotations = Generic.list();
        for (Annotation[] parameterAnnotationsArray : parametersAnnotationsArray) {
            List<AnnotationDatum<Integer>> parameterAnnotations = Generic.list();
            for (int i = 0; i < parameterAnnotationsArray.length; i++) {
                Annotation annotation = parameterAnnotationsArray[i];
                parameterAnnotations.add(datum(i, annotation));
            }
            parametersAnnotations.add(parameterAnnotations);
        }
        return parametersAnnotations;
    }

    private static <E> void loadInto(Map<String, AnnotationDatum<E>> map, Annotation annotation, E element) {
        String key = annotation.annotationType().getName();
        AnnotationDatum<E> existingData = map.get(key);
        if (existingData == null) {
            map.put(key, datum(element, annotation));
        }
    }

    private static <E> AnnotationDatum<E> datum(E element, Annotation annotation) {
        return AnnotationDatum.create(element,
                                      annotation.annotationType().getName(),
                                      properties(element, annotation));
    }

    private static <E> PropertySet properties(E element, Annotation annotation) {
        PropertySet propertySet = PropertySets.create();
        Class<? extends Annotation> annoClass = annotation.annotationType();
        Method[] declaredMethods = annoClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            Object value = createValue(element, annotation, method);
            propertySet.set(method.getName(), value);
        }
        return propertySet;
    }

    private static <E> Object createValue(E element, Annotation annotation, Method method) {
        boolean array = method.getReturnType().isArray();
        Method annotationMethod = methodOnAnnotation(annotation, method);
        Object value = Invoker.invoke(AnnotationsDigestsImpl.class,
                                      annotation,
                                      annotationMethod);
        if (array) {
            Class<?> returnType = method.getReturnType().getComponentType();
            Object valueArray = value.getClass().isArray() ? value : wrap(method, value);
            return possiblyNestedArray(element, returnType, valueArray);
        } else {
            return possiblyNested(element, method.getReturnType(), value);
        }
    }

    private static Object wrap(Method method, Object value) {
        Object newArray = Array.newInstance(method.getReturnType(), 1);
        Array.set(newArray, 0, value);
        return newArray;
    }

    private static <E> Object possiblyNestedArray(E element, Class<?> type, Object array) {
        if (!isAnnotation(type)) {
            return array;
        }
        int length = Array.getLength(array);
        List<AnnotationDatum<?>> data = Generic.list(length);
        for (int i = 0; i < length; i++) {
            data.add(datum(element, (Annotation) Array.get(array, i)));
        }
        return data;
    }

    private static boolean isAnnotation(Class<?> type) {
        return Annotation.class.isAssignableFrom(type);
    }

    private static <E> Object possiblyNested(E element, Class<?> type, Object value) {
        if (!isAnnotation(type)) {
            return value;
        }
        return datum(element, (Annotation)value);
    }

    private static Method methodOnAnnotation(Annotation annotation, Method method) {
        try {
            return annotation.getClass().getMethod(method.getName());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(annotation + " did not have method " + method, e);
        }
    }

    private static void addLeafMethods(Set<Method> foundMethods, Class<?> clazz) {
        Method[] classMethods = clazz.getDeclaredMethods();
        if (foundMethods.isEmpty()) {
            addAllAnnotated(foundMethods, classMethods);
        } else {
            resolveOverrides(foundMethods, classMethods);
        }
    }

    private static void addAllAnnotated(Set<Method> foundMethods, Method[] classMethods) {
        for (Method classMethod : classMethods) {
            if (isAnnotated(classMethod)) {
                foundMethods.add(classMethod);
            }
        }
    }

    private static void resolveOverrides(Set<Method> foundMethods, Method[] classMethods) {
        Set<Method> toAdd = Generic.set();
        Set<Method> toRemove = Generic.set();
        for (Method classMethod : classMethods) {
            if (isAnnotated(classMethod)) {
                resolveOverrides(foundMethods, toAdd, toRemove, classMethod);
            }
        }
        foundMethods.removeAll(toRemove);
        foundMethods.addAll(toAdd);
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

    private static void resolveOverrides(Set<Method> foundMethods,
                                         Set<Method> toAdd,
                                         Set<Method> toRemove,
                                         Method classMethod) {
        for (Method foundMethod : foundMethods) {
            if (overrides(foundMethod, classMethod, false)) {
                return;
            }
            if (overrides(classMethod, foundMethod, false)) {
                // Class method shadows found method;
                toRemove.add(foundMethod);
                toAdd.add(classMethod);
                return;
            }
        }
        toAdd.add(classMethod);
    }

    private static boolean isAnnotated(Method classMethod) {
        return classMethod.getAnnotations().length > 0;
    }

    private static List<AnnotationDatum<Field>> readAllFromField(Field field) {
        List<AnnotationDatum<Field>> list = Generic.list();
        for (Annotation annotation : field.getAnnotations()) {
            list.add(datum(field, annotation));
        }
        return list;
    }

    private static List<AnnotationDatum<Constructor>> readAllFromConstructor(Constructor constructor) {
        List<AnnotationDatum<Constructor>> list = Generic.list();
        for (Annotation annotation : constructor.getAnnotations()) {
            list.add(datum(constructor, annotation));
        }
        return list;
    }

    private List<AnnotationDatum<Method>> readAllFromMethod(Method method) {
        Map<String, AnnotationDatum<Method>> annotations = Generic.linkedHashMap();
        for (Class<?> type : typeChain) {
            for (Method potentialOverride : type.getDeclaredMethods()) {
                extractOverrides(method, annotations, potentialOverride);
            }
        }
        return Generic.list(annotations.values());
    }

    private static void extractOverrides(Method anchorMethod,
                                         Map<String, AnnotationDatum<Method>> map,
                                         Method potentialOverrider) {
        Annotation[] declaredAnnotations = potentialOverrider.getDeclaredAnnotations();
        if (declaredAnnotations.length > 0 && overrides(anchorMethod, potentialOverrider, true)) {
            for (Annotation declaredAnnotation : declaredAnnotations) {
                loadInto(map, declaredAnnotation, anchorMethod);
            }
        }
    }

    private static void addFields(Set<Field> fields, Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotations().length > 0) {
                fields.add(field);
            }
        }
    }
}
