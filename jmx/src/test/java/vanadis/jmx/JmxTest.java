package vanadis.jmx;

import org.junit.Assert;
import org.junit.Test;
import vanadis.core.jmx.Jmx;
import vanadis.core.system.VM;

import javax.management.*;

import static org.junit.Assert.assertNotNull;

public class JmxTest {

    public interface Foo {

        String getName();
    }

    @Test
    public void testStandardMBean() throws Exception {
        Foo foo = new Foo() {

            @Override
            public String getName() {
                return "name";
            }
        };
        ObjectName objectName = Jmx.registerJmx("com.test:type=foo", foo, Foo.class);
        assertNotNull(objectName);
        assertNotNull(VM.JMX.getMBeanInfo(objectName));

        Assert.assertSame("name", VM.JMX.getAttribute(objectName, "Name"));
    }
}
