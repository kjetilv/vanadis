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

import java.lang.annotation.Annotation;
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

    Iterable<AnnotationDatum<Class<?>>> classData();

    Iterable<AnnotationDatum<Method>> methodData();

    Iterable<AnnotationDatum<Field>> fieldData();

    <A extends Annotation> AnnotationDatum<Class<?>> getClassDatum(Class<A> type);

    List<AnnotationDatum<Class<?>>> getClassData(Class<?>... types);

    List<AnnotationDatum<?>> getAccessibleData(Class<? extends Annotation> type);

    Map<String, AnnotationDatum<?>> getAccessibleDataIndex(Class<? extends Annotation> type);

    List<AnnotationDatum<Field>> getFieldData(Class<? extends Annotation> type);

    Map<String, AnnotationDatum<Field>> getFieldDataIndex(Class<? extends Annotation> type);

    List<AnnotationDatum<Method>> getMethodData(Class<? extends Annotation> type);

    Map<String, AnnotationDatum<Method>> getMethodDataIndex(Class<? extends Annotation> type);

    AnnotationDatum<Class<?>> getClassDatum(String type);

    List<AnnotationDatum<Class<?>>> getClassData(String... type);

    List<AnnotationDatum<Field>> getFieldData(String type);

    List<AnnotationDatum<Method>> getMethodData(String type);

    boolean hasClassData(Class<? extends Annotation>... classes);

    boolean hasMethodData(Class<? extends Annotation>... classes);

    boolean hasFieldData(Class<? extends Annotation>... classes);
}
