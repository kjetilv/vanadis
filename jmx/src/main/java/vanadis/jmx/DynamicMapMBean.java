package vanadis.jmx;

import vanadis.core.collections.Generic;

import javax.management.*;
import java.util.List;
import java.util.Map;

public class DynamicMapMBean implements DynamicMBean {

   private final Map<String, Object> map;

   private final boolean writable;

   private final boolean toString;

   private static final String STRING_TYPE = String.class.getName();

   public DynamicMapMBean(Map<String, Object> map) {
       this(map, true, false);
   }

   public DynamicMapMBean(Map<String, Object> map, boolean writable, boolean toString) {
       this.map = map;
       this.writable = writable;
       this.toString = toString;
   }

   @Override
   public Object getAttribute(String attribute) {
       return val(map.get(attribute));
   }

   @Override
   public void setAttribute(Attribute attribute) {
       if (!writable) {
           throw new IllegalStateException("Received set call: " + attribute);
       }
       map.put(attribute.getName(), attribute.getValue());
   }

   @Override
   public AttributeList getAttributes(String[] attributes) {
       List<Attribute> values = Generic.list();
       for (String attribute : attributes) {
           Object value = map.get(attribute);
           values.add(new Attribute(attribute, val(value)));
       }
       return new AttributeList();
   }

   @Override
   public AttributeList setAttributes(AttributeList attributes) {
       if (!writable) {
           throw new IllegalStateException("Received set call: " + attributes);
       }
       for (Attribute attribute : attributes.asList()) {
           setAttribute(attribute);
       }
       return attributes;
   }

   @Override
   public Object invoke(String actionName, Object[] params, String[] signature) {
       throw new IllegalStateException("No invokations!");
   }

   @Override
   public MBeanInfo getMBeanInfo() {
       return new MBeanInfo(Map.class.getName(), "A map", attributes(), null, null, null, null);
   }

   private Object val(Object value) {
       return toString ? String.valueOf(value) : value;
   }

   private MBeanAttributeInfo[] attributes() {
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
