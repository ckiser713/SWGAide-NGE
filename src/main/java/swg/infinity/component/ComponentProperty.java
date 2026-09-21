package swg.infinity.component;

import swg.crafting.simulator.contracts.Provenance;

/** One ordered attribute exposed by a crafted or looted component. */
public final class ComponentProperty {
    private final String attribute;
    private final double value;
    private final int precision;
    private final String group;
    private final boolean hidden;
    private final Provenance provenance;

    public ComponentProperty(
            String attribute,
            double value,
            int precision,
            String group,
            boolean hidden,
            Provenance provenance) {
        if (attribute == null || attribute.trim().isEmpty()) {
            throw new IllegalArgumentException("attribute must not be blank");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }
        if (precision < 0) {
            throw new IllegalArgumentException("precision must be >= 0");
        }
        if (provenance == null) throw new NullPointerException("provenance");
        this.attribute = attribute;
        this.value = value;
        this.precision = precision;
        this.group = group == null ? "" : group;
        this.hidden = hidden;
        this.provenance = provenance;
    }

    public String getAttribute() { return attribute; }
    public double getValue() { return value; }
    public int getPrecision() { return precision; }
    public String getGroup() { return group; }
    public boolean isHidden() { return hidden; }
    public Provenance getProvenance() { return provenance; }
}
