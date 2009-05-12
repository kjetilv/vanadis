package vanadis.ext;

/**
 * Information about a configured property.
 */
public interface ConfigureSummary {

    /**
     * Property name.
     *
     * @return Property name
     */
    String getName();

    /**
     * Property type.
     *
     * @return Property type
     */
    String getType();

    /**
     * Property value.
     *
     * @return Property value
     */
    String getValue();
}
