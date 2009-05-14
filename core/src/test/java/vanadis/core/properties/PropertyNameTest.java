package vanadis.core.properties;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import vanadis.core.collections.Generic;

import java.util.Map;

public class PropertyNameTest {

    @Test
    public void sameCase() {
        assertEquals(new PropertyName("caseName"), new PropertyName("caseName"));
    }
    @Test
    public void sameHash() {
        assertEquals(new PropertyName("caseName").hashCode(), new PropertyName("caseName").hashCode());
    }

    @Test
    public void differentCase() {
        assertEquals(new PropertyName("caseName"), new PropertyName("CaseName"));
    }

    @Test
    public void snakeAndCamelCase() {
        assertEquals(new PropertyName("case_name"), new PropertyName("CaseName"));
        assertEquals(new PropertyName("case_name"), new PropertyName("caseName"));
    }

    @Test
    public void mapTest() {
        Map<PropertyName,String> map = Generic.map();
        map.put(new PropertyName("someCrazyKey"), "foo");
        assertEquals("foo", map.get(new PropertyName("some_crazy_key")));
    }
}
