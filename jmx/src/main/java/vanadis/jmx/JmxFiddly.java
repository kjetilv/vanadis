package vanadis.jmx;

import vanadis.annopro.AnnotationDatum;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.Strings;
import vanadis.core.reflection.GetNSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JmxFiddly {

    private static final String STRING = String.class.getName();

    static MBeanInfo info(Class<?> type, String description, Managed managed,
                          MBeanAttributeInfo[] attributeInfos,
                          MBeanOperationInfo[] operationInfos) {
        String desc = description != null ? description
            : managed != null ? managed.desc()
                : type.getName();
        return new MBeanInfo(type.getName(), desc,
                             attributeInfos, null, operationInfos, null);
    }

    static MBeanAttributeInfo beanAttributeInfo(String name, AnnotationDatum<Field> datum) {
        Attr annotation = attr(datum);
        return new MBeanAttributeInfo(name,
                                      asString(annotation) ? STRING : namedType(datum.getElement().getType()),
                                      annotation.desc(),
                                      annotation.readable(),
                                      annotation.writable(),
                                      false);
    }

    static MBeanAttributeInfo beanAttributeInfo(String name,
                                                Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair) {
        AnnotationDatum<Method> getDatum = pair.getOne();
        AnnotationDatum<Method> setDatum = pair.getTwo();
        boolean r = getDatum != null;
        boolean w = setDatum != null;
        Attr annotation = validAnnotation(name, getDatum, setDatum, r, w);
        boolean asString = asString(annotation);
        return new MBeanAttributeInfo(name,
                                      asString ? STRING : namedType(attributeType(getDatum, setDatum)),
                                      annotation.desc(), r, w,
                                      !asString && isIs(getDatum));
    }

    private static Attr validAnnotation(String name, AnnotationDatum<Method> getDatum, AnnotationDatum<Method> setDatum,
                                        boolean r, boolean w) {
        Attr annotation = r ? attr(getDatum) : attr(setDatum);
        if (!annotation.readable() && r) {
            throw new IllegalArgumentException("Unexpected get method " + getDatum.getElement() +
                    "for non-readable attribute " + name +
                    ", avoid using readable() and writable() for methods");
        }
        if (annotation.readable() && !r) {
            throw new IllegalArgumentException("No get method matching readable attribute " + name +
                    ", avoid using readable() and writable() for methods");
        }
        if (!annotation.writable() && w) {
            throw new IllegalArgumentException("Unexpected set method " + setDatum.getElement() +
                    "for non-writable attribute " + name +
                    ", avoid using readable() and writable() for methods");
        }
        if (annotation.writable() && !w) {
            throw new IllegalArgumentException("No set method for writable attribute " + name +
                    ", avoid using readable() and writable() for methods");
        }
        return annotation;
    }

    static MBeanOperationInfo beanOperationInfo(AnnotationDatum<Method> datum,
                                                Class<?> paramType, List<List<AnnotationDatum<Integer>>> params) {
        Method method = datum.getElement();
        Operation annotation = oper(datum);
        return new MBeanOperationInfo
            (method.getName(),
             annotation.desc(),
             useParamAnnotations(paramType, params, method),
             asString(annotation) ? STRING : JmxFiddly.namedType(method.getReturnType()),
             impact(annotation));
    }

    static Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> organizeMethodAttributes(List<AnnotationDatum<Method>> data) {
        Map<String, AnnotationDatum<Method>> getters = Generic.map();
        Map<String, AnnotationDatum<Method>> setters = Generic.map();
        Set<String> attributeNames = Generic.set();
        for (AnnotationDatum<Method> datum : data) {
            add(getters, setters, attributeNames, datum);
        }
        Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributes = Generic.map();
        for (String name : attributeNames) {
            add(getters.get(name), setters.get(name), attributes, name);
        }
        return attributes;
    }

    private static boolean asString(Attr annotation) {
        try {
            return annotation.asString();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private static boolean asString(Operation annotation) {
        try {
            return annotation.asString();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private static int impact(Operation annotation) {
        try {
            return annotation.impact();
        } catch (NullPointerException e) {
            return MBeanOperationInfo.ACTION;
        }
    }

    private static void add(AnnotationDatum<Method> getDatum,
                            AnnotationDatum<Method> setDatum,
                            Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributes,
                            String name) {
        if (nonMatching(getDatum, setDatum)) {
            throw new IllegalArgumentException
                ("Managed read/write property " + name + " has non-matching getter/setter: " +
                    getDatum.getElement() + "/" + setDatum.getElement());
        }
        attributes.put(name, Pair.of(getDatum, setDatum));
    }

    private static void add(Map<String, AnnotationDatum<Method>> getters,
                            Map<String, AnnotationDatum<Method>> setters,
                            Set<String> attributeNames,
                            AnnotationDatum<Method> datum) {
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

    private static MBeanParameterInfo[] useParamAnnotations(Class<?> paramType, List<List<AnnotationDatum<Integer>>> params,
                                                            Method method) {
        List<MBeanParameterInfo> infos = Generic.list();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0, paramsSize = params.size(); i < paramsSize; i++) {
            String type = namedType(types[i]);
            List<AnnotationDatum<Integer>> paramAnnotations = params.get(i);
            for (int j = 0, paramAnnotationsSize = paramAnnotations.size(); j < paramAnnotationsSize; j++) {
                Param param = param(paramAnnotations.get(j));
                infos.add(new MBeanParameterInfo(param.name(), type, param.desc()));
            }
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

    static Param param(AnnotationDatum<Integer> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(), Param.class);
    }

    static MBeanInfo info(MBeanInfo info, String description) {
        return description == null || Strings.isEmpty(description)
            ? info
            : new MBeanInfo(info.getClassName(), description,
                            info.getAttributes(), null, info.getOperations(), null);
    }
}
