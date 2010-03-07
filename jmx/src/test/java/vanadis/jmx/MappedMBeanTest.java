package vanadis.jmx;

import org.junit.Test;

import javax.management.*;
import java.lang.annotation.Retention;
import java.util.Arrays;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.*;

public class MappedMBeanTest {

    @Retention(RUNTIME) @interface O { String desc(); }
    @Retention(RUNTIME) @interface A { String desc(); }
    @Retention(RUNTIME) @interface P { String desc(); }
    @Retention(RUNTIME) @interface M { String desc(); }

    @Test
    public void testMapped() throws ReflectionException, AttributeNotFoundException, MBeanException {
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans(new JmxMapping(M.class, O.class, A.class, P.class));

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
        Object manager = new MyObject();
        DynamicMBean bean = digest(manager, beans);
        MBeanInfo mbeanInfo = assertMBeanInfoWithDescription(bean);
        MBeanOperationInfo[] operationInfos = assertTwoOperations(bean, mbeanInfo);
        assertNoParameterOperation(operationInfos);
        assertSingleParameterOperation(operationInfos);
        assertSingleAttribute(mbeanInfo);
    }

    private static <T> T notNull(String error, T t) {
        assertNotNull(error, t);
        return t;
    }

    private void assertSingleParameterOperation(MBeanOperationInfo[] operationInfos) {
        MBeanOperationInfo oneParameter = get(operationInfos, "operateMore");
        assertEquals("Another simple operation, but now we pay", oneParameter.getDescription());
        assertEquals("The cost", signature(oneParameter)[0].getDescription());
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

    private void assertSingleAttribute(MBeanInfo mbeanInfo) {
        MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
        assertNotNull(attributeInfos);
        assertEquals(1, attributeInfos.length);

        MBeanAttributeInfo attributeInfo = notNull("Null attribute info!",
                                                   attributeInfos[0]);
        assertEquals("Wrong description of " + attributeInfo,
                     "Cost so far", attributeInfo.getDescription());
    }

    private void assertNoParameterOperation(MBeanOperationInfo[] operationInfos) {
        MBeanOperationInfo parameterLess = get(operationInfos, "operate");
        assertEquals("Expected no arguments to " + parameterLess,
                     0, signature(parameterLess).length);
        assertEquals("Simple operation, first fix is free", parameterLess.getDescription());
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
