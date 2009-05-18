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
 * <p>I.e. <code>new PropertyName("snake_case")</code> is equals to <code>new PropertyName("SnakeCase")</code>,
 * allowing clients of Maps/Sets to use any of the supported conventions,
 * and leaving the key class itself to do the resoultion:</p>
 *
 * <pre>
 * Map&lt;CaseString,String&gt; map = Generic.map();
 * map.put(new CaseString("someCrazyKey"), "foo");
 * assertEquals("foo", map.get(new CaseString("some_crazy_key")));
 * </pre>
 */
public final class CaseString {

    private final String name;

    private final int hash;

    public CaseString(String name) {
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
        CaseString name = EqHc.retyped(this, obj);
        return name != null && EqHc.eq(this.name, name.name);
    }
}
