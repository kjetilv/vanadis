package vanadis.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MappedMBeanTest {

    private static final JmxMapping MAPPING = new JmxMapping(Mng.class, Op.class, At.class, null);

    @Test
    public void testMapped() throws ReflectionException, AttributeNotFoundException, MBeanException {
        ManagedDynamicMBeans beans = new ManagedDynamicMBeans(MAPPING);
        DynamicMBean bean = beans.create(new LocalManaged());
        assertNotNull(bean);

        assertEquals("Managed", bean.getMBeanInfo().getDescription());
        MBeanOperationInfo[] beanOperationInfos = bean.getMBeanInfo().getOperations();
        assertNotNull(beanOperationInfos);
        assertEquals(2, beanOperationInfos.length);

        boolean foundParameterLess = false;
        boolean foundWithParameter= false;

        for (MBeanOperationInfo info : beanOperationInfos) {
            MBeanParameterInfo[] sig = info.getSignature();
            if (sig.length == 1) {
                foundWithParameter = true;
                assertEquals("Nose operation again", info.getDescription());
                MBeanParameterInfo parameterInfo = sig[0];
                assertEquals("cost", parameterInfo.getDescription());
            }
            if (sig.length == 0) {
                foundParameterLess = true;
                assertEquals("Nose operation", info.getDescription());
            }
        }
        Assert.assertTrue(foundWithParameter);
        Assert.assertTrue(foundParameterLess);

        MBeanAttributeInfo[] attributeInfos = bean.getMBeanInfo().getAttributes();
        assertNotNull(attributeInfos);
        assertEquals(1, attributeInfos.length);
    }
}

@Mng(desc = "Managed")
class LocalManaged {

    private String nose;

    @Op(desc = "Nose operation")
    public void operate() {
        nose = "nose";
    }

    @Op(desc = "Nose operation again")
    public void operateMore(@Par(desc = "cost") int cost) {
        nose = "" + cost;
    }

    @At(desc = "Nose result")
    public String nose() {
        return nose;
    }
}

@Retention(RUNTIME)
@Target(METHOD)
@interface Op {
    String desc();
}

@Retention(RUNTIME)
@Target(METHOD)
@interface At {
    String desc();
}

@Retention(RUNTIME)
@Target(PARAMETER)
@interface Par {
    String desc();
}

@Retention(RUNTIME)
@Target(TYPE)
@interface Mng {
    String desc();
}
