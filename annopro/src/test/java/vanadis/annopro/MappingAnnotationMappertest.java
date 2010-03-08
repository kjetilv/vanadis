package vanadis.annopro;

import org.junit.Test;
import vanadis.core.collections.Generic;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MappingAnnotationMappertest {

    @interface A {}
    @interface B { String movie() default ""; }
    @interface C { String value() default ""; }

    @Test
    public void testSelfMap() {
        AnnotationMapper mapper = new MappingAnnotationMapper(A.class, B.class, C.class);
        assertEquals(A.class, mapper.getClientCodeType(A.class));
        assertEquals(B.class, mapper.getClientCodeType(B.class));
        assertEquals(C.class, mapper.getClientCodeType(C.class));

        assertNull(mapper.getClientCodeAttribute(A.class, "foo"));
        assertNull(mapper.getClientCodeAttribute(B.class, "foo"));
        assertNull(mapper.getClientCodeAttribute(C.class, "foo"));
    }

    @Test
    public void testAttributeMap() {
        AnnotationMapper mapper = new MappingAnnotationMapper
                (Generic.<Class<?>,Class<?>>map(B.class, C.class),
                 Generic.<Class<?>, Map<String, String>>map(B.class, Generic.map("movie", "value")));
        assertEquals(C.class, mapper.getClientCodeType(B.class));
        assertEquals("value", mapper.getClientCodeAttribute(B.class, "movie"));
    }
}
