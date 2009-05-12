package vanadis.extrt;

import vanadis.ext.ConfigureSummary;

class ConfigureSummaryImpl implements ConfigureSummary {

    private final Configurer configurer;

    ConfigureSummaryImpl(Configurer configurer) {
        this.configurer = configurer;
    }

    @Override
    public String getName() {
        return configurer.getPropertyName();
    }

    @Override
    public String getType() {
        return configurer.getPropertyType().getName();
    }

    @Override
    public String getValue() {
        Object finalValue = configurer.getFinalValue();
        return finalValue == null ? null
                : finalValue.toString();
    }
}
