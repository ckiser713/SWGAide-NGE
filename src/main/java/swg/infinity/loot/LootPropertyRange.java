package swg.infinity.loot;

import swg.crafting.simulator.contracts.Provenance;

/** Source-derived range for one loot/component attribute. */
public final class LootPropertyRange {
    private final String attribute;
    private final double minimum;
    private final double maximum;
    private final int precision;
    private final String group;
    private final Provenance provenance;

    public LootPropertyRange(
            String attribute,
            double minimum,
            double maximum,
            int precision,
            String group,
            Provenance provenance) {
        if (attribute == null || attribute.trim().isEmpty()) {
            throw new IllegalArgumentException("attribute must not be blank");
        }
        requireFinite(minimum, "minimum");
        requireFinite(maximum, "maximum");
        if (precision < 0) throw new IllegalArgumentException("precision must be >= 0");
        if (provenance == null) throw new NullPointerException("provenance");
        this.attribute = attribute;
        this.minimum = minimum;
        this.maximum = maximum;
        this.precision = precision;
        this.group = group == null ? "" : group;
        this.provenance = provenance;
    }

    public String getAttribute() { return attribute; }
    public double getMinimum() { return minimum; }
    public double getMaximum() { return maximum; }
    public int getPrecision() { return precision; }
    public String getGroup() { return group; }
    public Provenance getProvenance() { return provenance; }

    private static void requireFinite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
