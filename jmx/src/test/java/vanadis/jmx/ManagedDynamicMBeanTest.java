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
                   params = @Param(name = "truth", desc = "And nothing but"),
                   impact = MBeanOperationInfo.ACTION_INFO)
        boolean doIt(boolean truth) {
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
        Assert.assertEquals("0", bean.getAttribute("fi"));
    }

    @Test
    public void useDescription() {
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans();
        Assert.assertEquals("foobar", beans.create(new Managed1(), "foobar").getMBeanInfo().getDescription());
        Assert.assertEquals("one", beans.create(new Managed1()).getMBeanInfo().getDescription());
    }

    @Test
    public void manageObjectName() throws Exception {
        DynamicMBean bean = new ManagedDynamicMBeans().create(new Managed2());
        ObjectName objectName = ((MBeanRegistration) bean).preRegister(null, null);
        Assert.assertNotNull(objectName);
        Assert.assertEquals(new ObjectName("foo.bar.zot:type=name"), objectName);
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
        Assert.assertEquals(1, operationInfos.length);
        MBeanOperationInfo operationInfo = operationInfos[0];
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        Assert.assertEquals(1, parameterInfos.length);
        MBeanParameterInfo parameterInfo = parameterInfos[0];
        Assert.assertEquals("truth", parameterInfo.getName());
        Assert.assertEquals("And nothing but", parameterInfo.getDescription());

        Assert.assertFalse(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        Assert.assertTrue(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.FALSE}, new String[]{"boolean"});
        Assert.assertFalse(oper.isTroo());
    }

    private void doManageOperPar(Managed1OperPar2 oper, DynamicMBean bean) throws MBeanException, ReflectionException {
        MBeanInfo mBeanInfo = bean.getMBeanInfo();
        MBeanOperationInfo[] operationInfos = mBeanInfo.getOperations();
        Assert.assertEquals(1, operationInfos.length);
        MBeanOperationInfo operationInfo = operationInfos[0];
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        Assert.assertEquals(1, parameterInfos.length);
        MBeanParameterInfo parameterInfo = parameterInfos[0];
        Assert.assertEquals("truth", parameterInfo.getName());
        Assert.assertEquals("And nothing but", parameterInfo.getDescription());

        Assert.assertFalse(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        Assert.assertTrue(oper.isTroo());
        bean.invoke("doIt", new Object[]{Boolean.FALSE}, new String[]{"boolean"});
        Assert.assertFalse(oper.isTroo());
    }

    @Test
    public void manageOper() throws ReflectionException, MBeanException {
        Managed1Oper oper = new Managed1Oper();
        DynamicMBean op = new ManagedDynamicMBeans().create(oper);
        MBeanInfo info = op.getMBeanInfo();
        MBeanOperationInfo[] infos = info.getOperations();
        Assert.assertNotNull(infos);
        Assert.assertEquals(1, infos.length);
        MBeanOperationInfo operationInfo = infos[0];
        Assert.assertNotNull(operationInfo);
        Assert.assertEquals("f1", operationInfo.getDescription());
        Assert.assertEquals("doIt", operationInfo.getName());
        Assert.assertEquals(MBeanOperationInfo.ACTION_INFO, operationInfo.getImpact());

        Assert.assertFalse(oper.isDone());
        Assert.assertEquals(Boolean.TRUE, op.invoke("doIt", NO_ARGS, EMPTY_SIG));
        Assert.assertTrue(oper.isDone());
    }

    @Test
    public void manageSimple() {
        DynamicMBean m1 = new ManagedDynamicMBeans().create(new Managed1());
        Assert.assertNotNull(m1);
        MBeanInfo info = m1.getMBeanInfo();
        Assert.assertNotNull(info.getAttributes());
        Assert.assertEquals(0, info.getAttributes().length);
        Assert.assertEquals(0, info.getOperations().length);
        Assert.assertEquals(0, info.getConstructors().length);
        Assert.assertEquals("one", info.getDescription());
    }

    @Test
    public void manageOneField() throws ReflectionException, MBeanException, AttributeNotFoundException {
        assertOneFieldAttribute(Managed1Attr.class, new ManagedDynamicMBeans().create(new Managed1Attr()));
    }

    @Test
    public void manageRWOneField() throws ReflectionException, MBeanException, AttributeNotFoundException, InvalidAttributeValueException {
        Managed1RWAttrMeth attr = new Managed1RWAttrMeth();
        DynamicMBean bean = new ManagedDynamicMBeans().create(attr);
        MBeanAttributeInfo info = assertOneField(Managed1RWAttrMeth.class, bean);
        Assert.assertTrue(info.isReadable());
        Assert.assertTrue(info.isWritable());
        Assert.assertFalse(info.isIs());

        Assert.assertEquals(attr.getField(), bean.getAttribute("field"));
        bean.setAttribute(new Attribute("field", "newValue"));
        Assert.assertEquals("newValue", attr.getField());
        Assert.assertEquals(attr.getField(), bean.getAttribute("field"));
    }

    @Test
    public void manageOneMethodAttr() throws ReflectionException, MBeanException, AttributeNotFoundException {
        assertOneFieldAttribute(Managed1AttrMeth.class, new ManagedDynamicMBeans().create(new Managed1AttrMeth()));
    }

    @Test
    public void manageOneMethodIsAttr() throws ReflectionException, MBeanException, AttributeNotFoundException {
        DynamicMBean mBean = new ManagedDynamicMBeans().create(new Managed1AttrIsMeth());
        MBeanAttributeInfo info = assertOneField(Managed1AttrIsMeth.class, mBean);
        Assert.assertEquals("field", info.getName());
        Assert.assertTrue(info.isIs());
        Assert.assertFalse(info.isWritable());
        Assert.assertTrue(info.isReadable());
        Assert.assertEquals(Boolean.TRUE, mBean.getAttribute(info.getName()));
    }

    @Test
    public void manageOneMethodIsAttrAsString() throws ReflectionException, MBeanException, AttributeNotFoundException {
        DynamicMBean mBean = new ManagedDynamicMBeans().create(new Managed1AttrIsMethToString());
        MBeanAttributeInfo info = assertOneField(Managed1AttrIsMethToString.class, mBean);
        Assert.assertEquals("field", info.getName());
        Assert.assertFalse(info.isIs());
        Assert.assertFalse(info.isWritable());
        Assert.assertTrue(info.isReadable());
        Assert.assertEquals("true", mBean.getAttribute(info.getName()));
    }

    private static void assertOneFieldAttribute(Class<?> clazz, DynamicMBean m1f)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        Assert.assertNotNull(m1f);
        MBeanAttributeInfo mBeanAttributeInfo = assertOneField(clazz, m1f);
        Assert.assertEquals("f1", mBeanAttributeInfo.getDescription());
        Assert.assertEquals("field", mBeanAttributeInfo.getName());
        Assert.assertEquals("java.lang.String", mBeanAttributeInfo.getType());
        Assert.assertFalse(mBeanAttributeInfo.isWritable());
        Assert.assertFalse(mBeanAttributeInfo.isIs());
        Assert.assertTrue(mBeanAttributeInfo.isReadable());
        Assert.assertEquals("value1", m1f.getAttribute("field"));
    }

    private static MBeanAttributeInfo assertOneField(Class<?> clazz, DynamicMBean m1f) {
        MBeanInfo info = m1f.getMBeanInfo();
        Assert.assertNotNull(info);
        Assert.assertEquals(clazz.getName(), info.getDescription());
        Assert.assertEquals(0, info.getOperations().length);
        Assert.assertEquals(0, info.getConstructors().length);
        Assert.assertEquals(1, info.getAttributes().length);
        MBeanAttributeInfo mBeanAttributeInfo = info.getAttributes()[0];
        Assert.assertNotNull(mBeanAttributeInfo);
        return mBeanAttributeInfo;
    }
}
