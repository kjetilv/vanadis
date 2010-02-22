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

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.Proxies;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public final class AnnotationDatum<E> {

    private final String annotationType;

    private final PropertySet propertySet;

    private final E element;

    private static final String DEFAULT_PROPERTY = "value";

    static <E> AnnotationDatum<E> create(String annotationType, PropertySet propertySet) {
        return create(null, annotationType, propertySet);
    }

    static <E> AnnotationDatum<E> create(E element, String annotationType, PropertySet propertySet) {
        return new AnnotationDatum<E>(element, annotationType, propertySet, false);
    }

    private AnnotationDatum(E element, String annotationType, PropertySet propertySet, boolean shallow) {
        this.element = element;
        this.annotationType = Not.nil(annotationType, "annotationType");
        Not.nil(propertySet, "propertySet");
        this.propertySet = shallow ? propertySet.orphan() : propertySet;
    }

    public String getTypeName() {
        if (element instanceof Field) {
            return ((Field)element).getType().getName();
        }
        if (element instanceof Method) {
            return ((Method)element).getReturnType().getName();
        }
        return null;
    }

    public E getElement() {
        if (element == null) {
            throw new IllegalStateException(this + " was not initialized with an actual element");
        }
        return element;
    }

    public PropertySet getPropertySet() {
        return propertySet;
    }

    public boolean isType(Class<? extends Annotation> type) {
        return type != null && type.getName().equals(annotationType);
    }

    public String annotationType() {
        return annotationType;
    }

    public <A extends Annotation> A createProxy(ClassLoader classLoader, Class<A> type) {
        return createProxy(classLoader, type, true, null);
    }

    public <A extends Annotation> A createProxy(ClassLoader classLoader, Class<A> type, PropertySet propertySet) {
        return createProxy(classLoader, type, true, propertySet);
    }

    public <A extends Annotation> A createShallowProxy(ClassLoader classLoader, Class<A> type) {
        return createProxy(classLoader, type, false, null);
    }

    public <A extends Annotation> A createShallowProxy(ClassLoader classLoader, Class<A> type, PropertySet propertySet) {
        return createProxy(classLoader, type, false, propertySet);
    }

    <A extends Annotation> A createProxy(ClassLoader classLoader, Class<A> type, boolean deep, PropertySet propertySet) {
        AnnotationDatum<E> instrumentedData = deep
            ? this.withShadowingProperties(propertySet)
            : this.shallowWithShadowingProperties(propertySet);
        InvocationHandler handler = new AnnotationHandler(classLoader, instrumentedData);
        return Proxies.genericProxy(classLoader, type, handler);
    }

    public <T> T getValue(Class<T> type) {
        return type.cast(getValue());
    }

    public Object getValue() {
        return hasSingleValue() ? propertySet.get(DEFAULT_PROPERTY) : null;
    }

    private boolean hasSingleValue() {
        return propertySet != null && propertySet.has(DEFAULT_PROPERTY) && propertySet.size() == 1;
    }

    private AnnotationDatum<E> shallow() {
        return derivedData(propertySet.orphan());
    }

    private AnnotationDatum<E> shallowWithShadowingProperties(PropertySet propertySet) {
        AnnotationDatum<E> shallow = this.shallow();
        return propertySet == null ? shallow : shallow.withShadowingProperties(propertySet);
    }

    private AnnotationDatum<E> withShadowingProperties(PropertySet propertySet) {
        return propertySet == null ? this
            : derivedData(this.propertySet == null ? propertySet : propertySet.withParent(this.propertySet, false));
    }

    private AnnotationDatum<E> derivedData(PropertySet shadowed) {
        return new AnnotationDatum<E>(element, annotationType, shadowed, false);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(propertySet, annotationType);
    }

    @Override
    public boolean equals(Object obj) {
        AnnotationDatum<E> annotationData = EqHc.retyped(this, obj);
        return annotationData != null &&
            EqHc.eq(propertySet, annotationData.propertySet,
                    annotationType, annotationData.annotationType);
    }

    @Override
    public String toString() {
        return ToString.of(this, element, "t", annotationType, "p", propertySet);
    }
}
