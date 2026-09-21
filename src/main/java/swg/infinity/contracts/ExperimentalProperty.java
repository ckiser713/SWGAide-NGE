package swg.infinity.contracts;
import swg.crafting.simulator.contracts.Provenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Effective experimental attribute resolved from an Infinity target template. */
public final class ExperimentalProperty {
    private final String attribute;
    private final String group;
    private final double minValue;
    private final double maxValue;
    private final int precision;
    private final boolean hidden;
    private final CombineType combineType;
    private final List<PropertyWeight> weights;
    private final Provenance provenance;

    public ExperimentalProperty(
            String attribute,
            String group,
            double minValue,
            double maxValue,
            int precision,
            boolean hidden,
            CombineType combineType,
            List<PropertyWeight> weights,
            Provenance provenance) {
        this.attribute = requireText(attribute, "attribute");
        this.group = group == null ? "" : group;
        if (!isFinite(minValue) || !isFinite(maxValue)) {
            throw new IllegalArgumentException("min/max must be finite");
        }
        if (precision < 0) {
            throw new IllegalArgumentException("precision must be >= 0");
        }
        if (combineType == null) throw new NullPointerException("combineType");
        if (weights == null) throw new NullPointerException("weights");
        if (provenance == null) throw new NullPointerException("provenance");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.precision = precision;
        this.hidden = hidden;
        this.combineType = combineType;
        this.weights = Collections.unmodifiableList(
                new ArrayList<PropertyWeight>(weights));
        this.provenance = provenance;
    }

    public String getAttribute() { return attribute; }
    public String getGroup() { return group; }
    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    public int getPrecision() { return precision; }
    public boolean isHidden() { return hidden; }
    public CombineType getCombineType() { return combineType; }
    public List<PropertyWeight> getWeights() { return weights; }
    public Provenance getProvenance() { return provenance; }

    public boolean isExperimentable() {
        return !group.isEmpty() && !weights.isEmpty();
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
