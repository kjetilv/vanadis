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
package vanadis.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.*;

import static org.junit.Assert.*;

public class ManagedDynamicMBeanTest {

    private static final Object[] NO_ARGS = new Object[]{};

    private static final String[] EMPTY_SIG = new String[]{};

    @Managed(desc = "one")
    class Managed1 {
    }

    @Managed(desc = "one", objectName = "foo.bar.zot:type=name")
    class Managed2 {
    }

    class Managed1Attr {

        @SuppressWarnings({"UnusedDeclaration"})
        @Attr(desc = "f1")
        private final String field = "value1";
    }

    class ManagedNonAttr {

        @Attr(desc = "fi", asString = true)
        public Integer fi() {
            return 0;
        }
    }

    class Managed1AttrMeth {

        @Attr(desc = "f1")
        public String getField() {
            return "value1";
        }
    }

    class Managed1RWAttrMeth {
        private String field;

        @Attr(desc = "f1")
        public String getField() {
            return field;
        }

        @Attr
        public void setField(String string) {
            this.field = string;
        }
    }

    class Managed1RWAttrMethAndField {

        @Attr(desc = "f1")
        private String field;

        @Attr(desc = "f")
        public void setField(String string) {
            this.field = string;
        }
    }

    class Managed1RWAttrMethAndFieldTakeTwo {

        @Attr(desc = "f1", writable = true)
        private String field;

        private boolean getterCalled;

        @Attr(desc = "f")
        public String getField() {
            this.getterCalled = true;
            return this.field;
        }

        public boolean isGetterCalled() {
            return getterCalled;
        }
    }

    class Managed1AttrIsMeth {

        @Attr(desc = "f1")
        boolean isField() {
            return true;
        }
    }

    class Managed1AttrIsMethToString {

        @Attr(desc = "f1", asString = true)
        boolean isField() {
            return true;
        }
    }

    class Managed1Oper {

        private boolean done;

        @Operation(desc = "f1", impact = MBeanOperationInfo.ACTION_INFO)
        boolean doIt() {
            done = true;
            return true;
        }

        boolean isDone() {
            return done;
        }
    }

    class Managed1OperPar {

        private boolean troo;

        @Operation(desc = "f1",
                   impact = MBeanOperationInfo.ACTION_INFO)
        boolean doIt(@Param(name = "truth", desc = "And nothing but") boolean truth) {
            troo = truth;
            return truth;
        }

        boolean isTroo() {
            return troo;
        }
    }

    class Managed1OperPar2 {

        private boolean troo;

        @Operation(desc = "f1", impact = MBeanOperationInfo.ACTION_INFO)
        boolean doIt(@Param(name = "truth", desc = "And nothing but") boolean truth) {
            troo = truth;
            return truth;
        }

        boolean isTroo() {
            return troo;
        }
    }

    @Test
    public void managedNonAttr() throws ReflectionException, AttributeNotFoundException, MBeanException {
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans();
        DynamicMBean bean = beans.create(new ManagedNonAttr());
        assertEquals("0", bean.getAttribute("fi"));
    }

    @Test
    public void useDescription() {
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans();
        Managed1 target = new Managed1();
        assertEquals("foobar", beans.create(target, "foobar").getMBeanInfo().getDescription());
        assertEquals("one", beans.create(target).getMBeanInfo().getDescription());
    }

    @Test
    public void manageObjectName() throws Exception {
        DynamicMBean bean = new ManagedDynamicMBeans().create(new Managed2());
        ObjectName objectName = ((MBeanRegistration) bean).preRegister(null, null);
        assertNotNull(objectName);
        assertEquals(new ObjectName("foo.bar.zot:type=name"), objectName);
    }

    @Test
    public void dontManage() {
        Assert.assertNull(new ManagedDynamicMBeans().create(new Object()));
    }

    @Test
    public void manageOperPar() throws ReflectionException, MBeanException {
        Managed1OperPar oper = new Managed1OperPar();
        ManagedDynamicMBeanType beanType = new ManagedDynamicMBeans().mbeanType(oper.getClass(), false);
        doManageOperPar(oper, beanType.bean(oper));
    }

    @Test
    public void manageOperPar2() throws ReflectionException, MBeanException {
        Managed1OperPar2 oper = new Managed1OperPar2();
        ManagedDynamicMBeanType beanType = new ManagedDynamicMBeans().mbeanType(oper.getClass(), false);
        doManageOperPar(oper, beanType.bean(oper));
    }

    private void doManageOperPar(Managed1OperPar oper, DynamicMBean bean) throws MBeanException, ReflectionException {
        MBeanInfo mBeanInfo = bean.getMBeanInfo();
        MBeanOperationInfo[] operationInfos = mBeanInfo.getOperations();
        assertEquals(1, operationInfos.length);
        MBeanOperationInfo operationInfo = operationInfos[0];
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        assertEquals(1, parameterInfos.length);
        MBeanParameterInfo parameterInfo = parameterInfos[0];
        assertEquals("truth", parameterInfo.getName());
        assertEquals("And nothing but", parameterInfo.getDescription());

        assertFalse(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        assertTrue(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.FALSE}, new String[]{"boolean"});
        assertFalse(oper.isTroo());
    }

    private void doManageOperPar(Managed1OperPar2 oper, DynamicMBean bean) throws MBeanException, ReflectionException {
        MBeanInfo mBeanInfo = bean.getMBeanInfo();
        MBeanOperationInfo[] operationInfos = mBeanInfo.getOperations();
        assertEquals(1, operationInfos.length);
        MBeanOperationInfo operationInfo = operationInfos[0];
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        assertEquals(1, parameterInfos.length);
        MBeanParameterInfo parameterInfo = parameterInfos[0];
        assertEquals("truth", parameterInfo.getName());
        assertEquals("And nothing but", parameterInfo.getDescription());

        assertFalse(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        assertTrue(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.FALSE}, new String[]{"boolean"});
        assertFalse(oper.isTroo());
    }

    @Test
    public void manageOper() throws ReflectionException, MBeanException {
        Managed1Oper oper = new Managed1Oper();
        DynamicMBean op = new ManagedDynamicMBeans().create(oper);
        MBeanInfo info = op.getMBeanInfo();
        MBeanOperationInfo[] infos = info.getOperations();
        assertNotNull(infos);
        assertEquals(1, infos.length);
        MBeanOperationInfo operationInfo = infos[0];
        assertNotNull(operationInfo);
        assertEquals("f1", operationInfo.getDescription());
        assertEquals("doIt", operationInfo.getName());
        assertEquals(MBeanOperationInfo.ACTION_INFO, operationInfo.getImpact());

        assertFalse(oper.isDone());
        assertEquals(Boolean.TRUE, op.invoke("doIt", NO_ARGS, EMPTY_SIG));
        assertTrue(oper.isDone());
    }

    @Test
    public void manageSimple() {
        DynamicMBean m1 = new ManagedDynamicMBeans().create(new Managed1());
        assertNotNull(m1);
        MBeanInfo info = m1.getMBeanInfo();
        assertNotNull(info.getAttributes());
        assertEquals(0, info.getAttributes().length);
        assertEquals(0, info.getOperations().length);
        assertEquals(0, info.getConstructors().length);
        assertEquals("one", info.getDescription());
    }

    @Test
    public void manageOneField() throws ReflectionException, MBeanException, AttributeNotFoundException {
        assertOneFieldAttribute(Managed1Attr.class, new ManagedDynamicMBeans().create(new Managed1Attr()));
    }

    @Test
    public void manageMixedRWFieldMethod() throws InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException {
        Managed1RWAttrMethAndField attr = new Managed1RWAttrMethAndField();
        DynamicMBean bean = new ManagedDynamicMBeans().create(attr);
        MBeanAttributeInfo info = assertOneField(Managed1RWAttrMethAndField.class, bean);
        assertTrue(info.isReadable());
        assertTrue(info.isWritable());
        assertFalse(info.isIs());
        assertEquals("f1", info.getDescription());

        bean.setAttribute(new Attribute("field", "foo"));
        assertEquals("foo", bean.getAttribute("field"));
    }

    @Test
    public void manageMixedRWFieldMethodTakeTwo() throws InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException {
        Managed1RWAttrMethAndFieldTakeTwo attr = new Managed1RWAttrMethAndFieldTakeTwo();
        DynamicMBean bean = new ManagedDynamicMBeans().create(attr);

        bean.setAttribute(new Attribute("field", "foo"));
        assertFalse(attr.isGetterCalled());
        assertEquals("foo", bean.getAttribute("field"));
        assertTrue(attr.isGetterCalled());
    }

    @Test
    public void manageRWOneField() throws ReflectionException, MBeanException, AttributeNotFoundException, InvalidAttributeValueException {
        Managed1RWAttrMeth attr = new Managed1RWAttrMeth();
        DynamicMBean bean = new ManagedDynamicMBeans().create(attr);
        MBeanAttributeInfo info = assertOneField(Managed1RWAttrMeth.class, bean);
        assertTrue(info.isReadable());
        assertTrue(info.isWritable());
        assertFalse(info.isIs());

        assertEquals(attr.getField(), bean.getAttribute("field"));
        bean.setAttribute(new Attribute("field", "newValue"));
        assertEquals("newValue", attr.getField());
        assertEquals(attr.getField(), bean.getAttribute("field"));
    }

    @Test
    public void manageOneMethodAttr() throws ReflectionException, MBeanException, AttributeNotFoundException {
        assertOneFieldAttribute(Managed1AttrMeth.class, new ManagedDynamicMBeans().create(new Managed1AttrMeth()));
    }

    @Test
    public void manageOneMethodIsAttr() throws ReflectionException, MBeanException, AttributeNotFoundException {
        DynamicMBean mBean = new ManagedDynamicMBeans().create(new Managed1AttrIsMeth());
        MBeanAttributeInfo info = assertOneField(Managed1AttrIsMeth.class, mBean);
        assertEquals("field", info.getName());
        assertTrue(info.isIs());
        assertFalse(info.isWritable());
        assertTrue(info.isReadable());
        assertEquals(Boolean.TRUE, mBean.getAttribute(info.getName()));
    }

    @Test
    public void manageOneMethodIsAttrAsString() throws ReflectionException, MBeanException, AttributeNotFoundException {
        DynamicMBean mBean = new ManagedDynamicMBeans().create(new Managed1AttrIsMethToString());
        MBeanAttributeInfo info = assertOneField(Managed1AttrIsMethToString.class, mBean);
        assertEquals("field", info.getName());
        assertFalse(info.isIs());
        assertFalse(info.isWritable());
        assertTrue(info.isReadable());
        assertEquals("true", mBean.getAttribute(info.getName()));
    }

    private static void assertOneFieldAttribute(Class<?> clazz, DynamicMBean m1f)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        assertNotNull(m1f);
        MBeanAttributeInfo mBeanAttributeInfo = assertOneField(clazz, m1f);
        assertEquals("f1", mBeanAttributeInfo.getDescription());
        assertEquals("field", mBeanAttributeInfo.getName());
        assertEquals("java.lang.String", mBeanAttributeInfo.getType());
        assertFalse(mBeanAttributeInfo.isWritable());
        assertFalse(mBeanAttributeInfo.isIs());
        assertTrue(mBeanAttributeInfo.isReadable());
        assertEquals("value1", m1f.getAttribute("field"));
    }

    private static MBeanAttributeInfo assertOneField(Class<?> clazz, DynamicMBean m1f) {
        MBeanInfo info = m1f.getMBeanInfo();
        assertNotNull(info);
        assertEquals(clazz.getName(), info.getDescription());
        assertEquals(0, info.getOperations().length);
        assertEquals(0, info.getConstructors().length);
        assertEquals(1, info.getAttributes().length);
        MBeanAttributeInfo mBeanAttributeInfo = info.getAttributes()[0];
        assertNotNull(mBeanAttributeInfo);
        return mBeanAttributeInfo;
    }
}
