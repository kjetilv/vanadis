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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * A generic, reflective annotation processor that handles inherited annotations as well.
 * Provides {@link AnnotationDatum annotation data} instances that
 * hold annotation data for the class, its fields and its methods.  The data objects
 * can produce dynamic proxies for the annotation references,
 */
public interface AnnotationsDigest {

    Iterable<AnnotationDatum<Method>> methodData(Method method);

    Iterable<AnnotationDatum<Field>> fieldData(Field field);

    List<List<AnnotationDatum<Integer>>> parameterData(Constructor constructor);

    List<List<AnnotationDatum<Integer>>> parameterData(Method method);

    Iterable<AnnotationDatum<Class<?>>> classData();

    Iterable<AnnotationDatum<Method>> methodData();

    Iterable<AnnotationDatum<Field>> fieldData();

    AnnotationDatum<Class<?>> getClassDatum(Class<?> type);

    List<AnnotationDatum<Class<?>>> getClassData(Class<?>... types);

    List<AnnotationDatum<?>> getAccessibleData(Class<?> type);

    Map<String, AnnotationDatum<?>> getAccessibleDataIndex(Class<?> type);

    List<AnnotationDatum<Field>> getFieldData(Class<?> type);

    Map<String, AnnotationDatum<Field>> getFieldDataIndex(Class<?> type);

    List<AnnotationDatum<Method>> getMethodData(Class<?> type);

    List<AnnotationDatum<Constructor>> getConstructorData(Class<?> type);

    Map<Constructor, List<List<AnnotationDatum<Integer>>>> getConstructorParameterDataIndex(Class<?> type);

    Map<Method, List<List<AnnotationDatum<Integer>>>> getMethodParameterDataIndex(Class<?> type);

    Map<String, AnnotationDatum<Method>> getMethodDataIndex(Class<?> type);

    AnnotationDatum<Class<?>> getClassDatum(String type);

    List<AnnotationDatum<Class<?>>> getClassData(String... type);

    List<AnnotationDatum<Field>> getFieldData(String type);

    List<AnnotationDatum<Method>> getMethodData(String type);

    List<AnnotationDatum<Constructor>> getConstructorData(String type);

    boolean hasClassData(Class<?>... classes);

    boolean hasMethodData(Class<?>... classes);

    boolean hasFieldData(Class<?>... classes);

    boolean hasParamData(Class<?>... classes);
}
