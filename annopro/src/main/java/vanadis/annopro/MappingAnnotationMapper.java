package vanadis.annopro;

import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MappingAnnotationMapper implements AnnotationMapper {

    private Map<String, Class<?>> map = Generic.map();

    private Map<String, Map<String, String>> attributeMap = Generic.map();

    public MappingAnnotationMapper(Class<?>... selfMap) {
        this(Arrays.asList(selfMap));
    }

    public MappingAnnotationMapper(List<Class<?>> selfMap) {
        this(map(selfMap));
    }

    public MappingAnnotationMapper(Map<Class<?>, Class<?>> map) {
        this(map, null);
    }

    public MappingAnnotationMapper(Map<Class<?>, Class<?>> map,
                                   Map<Class<?>, Map<String, String>> attributeMap) {
        for (Map.Entry<Class<?>, Class<?>> entry : Not.nil(map, "map").entrySet()) {
            this.map.put(entry.getKey().getName(), entry.getValue());
        }
        if (attributeMap != null) {
            for (Map.Entry<Class<?>, Map<String, String>> entry : attributeMap.entrySet()) {
                this.attributeMap.put(entry.getKey().getName(), Generic.<String, String>map(entry.getValue()));
            }
        }
    }

    @Override
    public Class<?> getClientCodeType(String processingType) {
        return map.get(processingType);
    }

    @Override
    public Class<?> getClientCodeType(Class<?> processingType) {
        return getClientCodeType(Not.nil(processingType, "processing type").getName());
    }

    @Override
    public String getClientCodeAttribute(Class<?> processingType, String processingAttribute) {
        return getClientCodeAttribute(Not.nil(processingType, "processing type").getName(),
                                      processingAttribute);
    }

    @Override
    public String getClientCodeAttribute(String processingType, String processingAttribute) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            return null;
        }
        Map<String, String> map = attributeMap.get(Not.nil(processingType, "processing type"));
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map.get(Not.nil(processingAttribute, "processing attribute"));
    }

    private static Map<Class<?>, Class<?>> map(List<Class<?>> clazzes) {
        Map<Class<?>, Class<?>> selfMap = Generic.identityMap();
        for (Class<?> clazz : clazzes) {
            selfMap.put(clazz, clazz);
        }
        return selfMap;
    }
}
