package vanadis.core.reflection;

import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Helper abstract class for coercers.  Generic arguments
 * are captured:
 *
 * <PRE>private static class FloatCoercer extends AbstractCoercer<Float> {
 * public Float coerce(String string) {
 * return Float.parseFloat(nonNull(string));
 * }
 * }
 * </PRE>
 */
public abstract class AbstractCoercer<T> implements Retyper.Coercer<T> {

    private final Class<T> type;

    private static final String[] NO_STRINGS = new String[]{};

    @SuppressWarnings({"unchecked"})
    protected AbstractCoercer() {
        ParameterizedType parameterizedType = ParameterizedType.class.cast(getClass().getGenericSuperclass());
        Type[] arguments = parameterizedType.getActualTypeArguments();
        Type argument = arguments[0];
        this.type = (Class<T>) Class.class.cast(argument);
    }

    @Override
    public Class<T> coercedType() {
        return type;
    }

    @Override
    public List<T> coerceMulti(String input) {
        String[] split = split(input);
        List<T> ts = Generic.list(split.length);
        for (String string : split) {
            ts.add(coerce(string));
        }
        return ts;
    }

    protected static String[] split(String string) {
        Not.nil(string, "string");
        String content = string.startsWith("[") && string.endsWith("]")
                ? string.substring(1, string.length() - 1)
                : string;
        return content.trim().length() == 0 ? NO_STRINGS : content.split(",");
    }

    @Override
    public String toString(T t) {
        return t.toString();
    }
}
