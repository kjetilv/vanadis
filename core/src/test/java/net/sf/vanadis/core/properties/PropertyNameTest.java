package net.sf.vanadis.core.properties;

import org.junit.Assert;
import org.junit.Test;

public class PropertyNameTest {

    @Test
    public void sameCase() {
        Assert.assertEquals(new PropertyName("caseName"), new PropertyName("caseName"));
    }
    @Test
    public void sameHash() {
        Assert.assertEquals(new PropertyName("caseName").hashCode(), new PropertyName("caseName").hashCode());
    }

    @Test
    public void differentCase() {
        Assert.assertEquals(new PropertyName("caseName"), new PropertyName("CaseName"));
    }

    @Test
    public void snakeAndCamelCase() {
        Assert.assertEquals(new PropertyName("case_name"), new PropertyName("CaseName"));
        Assert.assertEquals(new PropertyName("case_name"), new PropertyName("caseName"));
    }
}
