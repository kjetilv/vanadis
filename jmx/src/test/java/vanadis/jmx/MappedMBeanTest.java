package vanadis.jmx;

import org.junit.Test;

import javax.management.*;
import java.lang.annotation.Retention;
import java.util.Arrays;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MappedMBeanTest {

    @Retention(RUNTIME) @interface O { String desc(); }
    @Retention(RUNTIME) @interface A { String desc(); }
    @Retention(RUNTIME) @interface P { String desc(); }
    @Retention(RUNTIME) @interface M { String desc(); }

    @Test
    public void testMapped() throws ReflectionException, AttributeNotFoundException, MBeanException {
        @M(desc = "Manager of operations") class MyObject {
            private int cost;

            @O(desc = "Simple operation, first fix is free")
            public void operate() {
                cost = 0;
            }

            @O(desc = "Another simple operation, but now we pay")
            public void operateMore(@P(desc = "The cost") int cost) {
                this.cost = cost;
            }

            @A(desc = "Cost so far")
            public int cost() {
                return cost;
            }

        }
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans(new JmxMapping(M.class, O.class, A.class, P.class));
        DynamicMBean bean = digest(new MyObject(), beans);
        assertExpectedMBeanInfo(bean, false, false);
    }

    @Retention(RUNTIME) @interface OV { String value(); }
    @Retention(RUNTIME) @interface AV { String value(); }
    @Retention(RUNTIME) @interface PV { String value(); }
    @Retention(RUNTIME) @interface MV { String value(); }

    @Test
    public void testPropertyMappedValue() throws ReflectionException, AttributeNotFoundException, MBeanException {
        @MV("Manager of operations") class MyObject {
            private int cost;

            @OV("Simple operation, first fix is free")
            public void operate() {
                cost = 0;
            }

            @OV("Another simple operation, but now we pay")
            public void operateMore(@PV("The cost") int cost) {
                this.cost = cost;
            }

            @AV("Cost so far")
            public int cost() {
                return cost;
            }

        }
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans
                (new JmxMapping(MV.class, null, "value",
                                OV.class, null, "value", null,
                                AV.class, null, "value", null, null,
                                PV.class, null, "value"));
        DynamicMBean bean = digest(new MyObject(), beans);
        assertExpectedMBeanInfo(bean, false, false);
    }

    @Retention(RUNTIME) @interface O2 { String d(); int i(); boolean as(); }
    @Retention(RUNTIME) @interface A2 { String d(); boolean r(); boolean w(); boolean as();}
    @Retention(RUNTIME) @interface P2 { String d(); String n(); }
    @Retention(RUNTIME) @interface M2 { String d(); String on(); }

    @Test
    public void testPropertyMappedAll() throws Exception {
        @M2(d = "Manager of operations", on = "on:me=true") class MyObject {
            private int cost;

            @O2(d = "Simple operation, first fix is free", i = MBeanOperationInfo.ACTION_INFO, as = true)
            public void operate() {
                cost = 0;
            }

            @O2(d = "Another simple operation, but now we pay", i = MBeanOperationInfo.ACTION_INFO, as = true)
            public void operateMore(@P2(d = "The cost", n = "cost") int cost) {
                this.cost = cost;
            }

            @A2(d = "Cost so far", r = true, w = false, as = true)
            public int cost() {
                return cost;
            }
        }
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans
                (new JmxMapping(M2.class, "on", "d",
                                O2.class, "as", "d", "i",
                                A2.class, "as", "d", "r", "w",
                                P2.class, "n", "d"));
        DynamicMBean bean = digest(new MyObject(), beans);
        assertExpectedMBeanInfo(bean, true, true);
    }

    private void assertExpectedMBeanInfo(DynamicMBean bean,
                                         boolean checkRW,
                                         boolean checkImpact) {
        MBeanInfo mbeanInfo = assertMBeanInfoWithDescription(bean);
        MBeanOperationInfo[] operationInfos = assertTwoOperations(bean, mbeanInfo);
        assertNoParameterOperation(operationInfos, checkImpact);
        assertSingleParameterOperation(operationInfos, checkImpact);
        MBeanAttributeInfo info = assertSingleAttribute(mbeanInfo);
        if (checkRW) {
            assertRW(info, true, false);
        }
    }

    private static <T> T notNull(String error, T t) {
        assertNotNull(error, t);
        return t;
    }

    private void assertSingleParameterOperation(MBeanOperationInfo[] operationInfos, boolean checkImpact) {
        MBeanOperationInfo oneParameter = get(operationInfos, "operateMore");
        assertEquals("Another simple operation, but now we pay", oneParameter.getDescription());
        assertEquals("The cost", signature(oneParameter)[0].getDescription());
        if (checkImpact) {
            assertEquals(MBeanOperationInfo.ACTION_INFO, oneParameter.getImpact());
        }
    }

    private DynamicMBean digest(Object target, ManagedDynamicMBeans beans) {
        return notNull(target + " should be digestable",
                       beans.create(target));
    }

    private MBeanInfo assertMBeanInfoWithDescription(DynamicMBean bean) {
        MBeanInfo mbeanInfo = notNull(bean + " produced no mbean info",
                                      bean.getMBeanInfo());

        assertEquals("Wrong description of " + bean,
                     "Manager of operations", mbeanInfo.getDescription());
        return mbeanInfo;
    }

    private void assertRW(MBeanAttributeInfo info, boolean r, boolean w) {
        assertEquals(r, info.isReadable());
        assertEquals(w, info.isWritable());
    }

    private MBeanAttributeInfo assertSingleAttribute(MBeanInfo mbeanInfo) {
        MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
        assertNotNull(attributeInfos);
        assertEquals(1, attributeInfos.length);

        MBeanAttributeInfo attributeInfo = notNull("Null attribute info!",
                                                   attributeInfos[0]);
        assertEquals("Wrong description of " + attributeInfo,
                     "Cost so far", attributeInfo.getDescription());
        return attributeInfo;
    }

    private void assertNoParameterOperation(MBeanOperationInfo[] operationInfos, boolean checkImpact) {
        MBeanOperationInfo parameterLess = get(operationInfos, "operate");
        assertEquals("Expected no arguments to " + parameterLess,
                     0, signature(parameterLess).length);
        assertEquals("Simple operation, first fix is free", parameterLess.getDescription());
        if (checkImpact) {
            assertEquals(MBeanOperationInfo.ACTION_INFO, parameterLess.getImpact());
        }
    }

    private MBeanOperationInfo[] assertTwoOperations(DynamicMBean bean, MBeanInfo mbeanInfo) {
        MBeanOperationInfo[] operationInfos = notNull("Expected two operations in " + bean,
                                                      mbeanInfo.getOperations());
        assertEquals("Expected two operations: " + Arrays.toString(operationInfos),
                     2, operationInfos.length);
        return operationInfos;
    }

    private MBeanParameterInfo[] signature(MBeanOperationInfo parameterLess) {
        return notNull(parameterLess + " had no signature",
                                           parameterLess.getSignature());
    }

    public static MBeanOperationInfo get(MBeanOperationInfo[] infos, String method) {
        for (MBeanOperationInfo info : infos) {
            if (info.getName().equals(method)) {
                return info;
            }
        }
        fail("Found no method " + method + " among " + Arrays.toString(infos));
        return null; // As if!
    }

}
