package vanadis.core.properties;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

/**
 * <p>A unifying map key class, collapsing strings in three case conventions into
 * a single key identity:</p>
 *
 * <ul>
 *  <li>veryLongName</li>
 *  <li>CamelCaseIThinkItsCalled</li>
 *  <li>snake_case</li>
 * </ul>
 *
 * <p>That is, <code>new PropertyName("snake_case")</code> is equals to <code>new PropertyName("SnakeCase")</code></p>
 */
public final class PropertyName {

    private final String name;

    private final int hash;

    public PropertyName(String name) {
        this.name = veryLongName(name)
                ? (isSnakeCase(name) ? desnake(name) : name)
                : downcaseName(name);
        hash = EqHc.hc(this.name);
    }

    private static String desnake(String snakeName) {
        String[] strings = snakeName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string.substring(0, 1).toUpperCase()).append(string.substring(1));
        }
        return downcaseName(sb.toString());
    }

    private static boolean veryLongName(String name) {
        return Character.isLowerCase(name.charAt(0));
    }

    private static boolean isSnakeCase(String name) {
        return name.contains("_") && name.toLowerCase().equals(name);
    }

    private static String downcaseName(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    @Override
    public String toString() {
        return ToString.of(this, name);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        PropertyName name = EqHc.retyped(this, obj);
        return name != null && EqHc.eq(this.name, name.name);
    }
}
