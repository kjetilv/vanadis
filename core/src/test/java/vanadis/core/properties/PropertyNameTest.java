package vanadis.core.properties;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import vanadis.core.collections.Generic;
import vanadis.core.properties.CaseString;

import java.util.Map;

public class PropertyNameTest {

    @Test
    public void sameCase() {
        assertEquals(new CaseString("caseName"), new CaseString("caseName"));
    }
    @Test
    public void sameHash() {
        assertEquals(new CaseString("caseName").hashCode(), new CaseString("caseName").hashCode());
    }

    @Test
    public void differentCase() {
        assertEquals(new CaseString("caseName"), new CaseString("CaseName"));
    }

    @Test
    public void snakeAndCamelCase() {
        assertEquals(new CaseString("case_name"), new CaseString("CaseName"));
        assertEquals(new CaseString("case_name"), new CaseString("caseName"));
    }

    @Test
    public void mapTest() {
        Map<CaseString,String> map = Generic.map();
        map.put(new CaseString("someCrazyKey"), "foo");
        assertEquals("foo", map.get(new CaseString("some_crazy_key")));
    }
}
