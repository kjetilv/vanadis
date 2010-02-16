package vanadis.jmx;

import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.AccessibleHelper;
import vanadis.core.reflection.GetNSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JmxFiddly {

    static final String STRING = String.class.getName();

    static MBeanAttributeInfo beanAttributeInfo(String key, Field field) {
        Attr annotation = field.getAnnotation(Attr.class);
        return new MBeanAttributeInfo(key,
                                      annotation.asString() ? STRING : namedType(field.getType()),
                                      annotation.desc(),
                                      true,
                                      false,
                                      false);
    }

    static MBeanAttributeInfo beanAttributeInfo(String key,
                                                        Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair) {
        AnnotationDatum<Method> getDatum = pair.getOne();
        AnnotationDatum<Method> setDatum = pair.getTwo();
        Attr annotation = getDatum != null ? attr(getDatum) : attr(setDatum);
        return new MBeanAttributeInfo(key,
                                      annotation.asString() ? STRING : namedType(attributeType(getDatum, setDatum)),
                                      annotation.desc(),
                                      getDatum != null,
                                      setDatum != null,
                                      isIs(getDatum));
    }

    static MBeanOperationInfo beanOperationInfo(AnnotationDatum<Method> datum) {
        Method method = datum.getElement();
        Operation annotation = oper(datum);
        return new MBeanOperationInfo
        (method.getName(),
         annotation.desc(),
         parameters(annotation, method),
         annotation.string() ? STRING : JmxFiddly.namedType(method.getReturnType()),
         annotation.impact());
    }

    static Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> organizeAttributes(List<AnnotationDatum<Method>> data) {
        Map<String, AnnotationDatum<Method>> getters = Generic.map();
        Map<String, AnnotationDatum<Method>> setters = Generic.map();
        Set<String> attributeNames = Generic.set();
        for (AnnotationDatum<Method> datum : data) {
            Method method = datum.getElement();
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            String get = GetNSet.getProperty(methodName);
            int pars = method.getParameterTypes().length;
            if (get != null && pars == 0 && returnsValue(returnType)) {
                getters.put(get, datum);
                attributeNames.add(get);
            } else {
                String set = GetNSet.setProperty(methodName);
                if (set != null && pars == 1) {
                    setters.put(set, datum);
                    attributeNames.add(set);
                } else {
                    throw new IllegalArgumentException
                        ("Managed attribute method " + method + " is not a valid setter or getter");
                }
            }
        }
        Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributes = Generic.map();
        for (String name : attributeNames) {
            AnnotationDatum<Method> getDatum = getters.get(name);
            AnnotationDatum<Method> setDatum = setters.get(name);
            if (nonMatching(getDatum, setDatum)) {
                throw new IllegalArgumentException
                    ("Managed read/write property " + name + " has non-matching getter/setter: " +
                        getDatum.getElement() + "/" + setDatum.getElement());
            }
            attributes.put(name, Pair.of(getDatum, setDatum));
        }
        return attributes;
    }

    private static boolean isIs(AnnotationDatum<Method> getDatum) {
        return getDatum != null && getDatum.getElement().getName().startsWith("is");
    }

    private static Class<?> attributeType(AnnotationDatum<Method> getDatum, AnnotationDatum<Method> setDatum) {
        if (getDatum != null) {
            return getDatum.getElement().getReturnType();
        }
        if (setDatum != null) {
            Class<?>[] types = setDatum.getElement().getParameterTypes();
            if (types != null && types.length == 1) {
                return types[0];
            }
        }
        throw new IllegalArgumentException("Unable to derive namedType of " + getDatum + "/" + setDatum);
    }

    private static String namedType(Class<?> returnType) {
        Class<?> type = returnType.isArray() ? returnType.getComponentType() : returnType;
        String name = type.getName();
        return returnType.isArray() ? "[L" + name + ";" : name;
    }

    private static MBeanParameterInfo[] parameters(Operation operation, Method method) {
        List<MBeanParameterInfo> infos = Generic.list();
        Param[] params = operation.params();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Param param = i < params.length ? params[i] : null;
            String name = param == null ? "param" + i : param.name();
            String desc = param == null ? "param" + i : param.desc();
            Class<?> parameter = method.getParameterTypes()[i];
            infos.add(new MBeanParameterInfo(name, namedType(parameter), desc));
        }
        return infos.toArray(new MBeanParameterInfo[infos.size()]);
    }

    private static boolean nonMatching(AnnotationDatum<Method> getDatum, AnnotationDatum<Method> setDatum) {
        return getDatum != null &&
            setDatum != null &&
            !getDatum.getElement().getReturnType().equals(setDatum.getElement().getParameterTypes()[0]);
    }

    private static boolean returnsValue(Class<?> returnType) {
        return returnType != null &&
            returnType != void.class &&
            returnType != Void.class;
    }

    private static Attr attr(AnnotationDatum<?> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(), Attr.class);
    }

    static Operation oper(AnnotationDatum<Method> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(), Operation.class);
    }
}
