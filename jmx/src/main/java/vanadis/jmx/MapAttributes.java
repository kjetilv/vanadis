package vanadis.jmx;

import vanadis.core.collections.Generic;

import javax.management.MBeanAttributeInfo;
import java.util.List;
import java.util.Map;

class MapAttributes {

    private static final String STRING_TYPE = String.class.getName();

    static MBeanAttributeInfo[] attributes(Map<String,Object> map, boolean writable, boolean toString) {
        List<MBeanAttributeInfo> infos = Generic.list();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Class<?> type = entry.getValue().getClass();
            String name = entry.getKey();
            String typeName = toString ? STRING_TYPE : type.getName();
            boolean bool = !toString && type == Boolean.class;
            String desc = "Key:" + name;
            infos.add(new MBeanAttributeInfo(name, typeName, desc, true, writable, bool));
        }
        return infos.toArray(new MBeanAttributeInfo[infos.size()]);
    }
}
