package swg.infinity.engine;

import swg.infinity.contracts.ExperimentalProperty;

/**
 * Immutable effective calculation state for one crafting attribute.
 *
 * <p>The source property preserves extracted provenance while effective min/max
 * values may be shifted by component contribution exactly as Infinity does.</p>
 */
public final class AttributeState {
    private static final double RANGE_EPSILON = 0.0000001d;

    private final ExperimentalProperty property;
    private final double weightedScore;
    private final double maxPercentage;
    private final double currentPercentage;
    private final double currentValue;
    private final double minValue;
    private final double maxValue;

    public AttributeState(
            ExperimentalProperty property,
            double weightedScore,
            double maxPercentage,
            double currentPercentage,
            double currentValue) {
        this(property, weightedScore, maxPercentage, currentPercentage,
                currentValue, property.getMinValue(), property.getMaxValue());
    }

    AttributeState(
            ExperimentalProperty property,
            double weightedScore,
            double maxPercentage,
            double currentPercentage,
            double currentValue,
            double minValue,
            double maxValue) {
        if (property == null) throw new NullPointerException("property");
        requireFinite(weightedScore, "weightedScore");
        requireFinite(maxPercentage, "maxPercentage");
        requireFinite(currentPercentage, "currentPercentage");
        requireFinite(currentValue, "currentValue");
        requireFinite(minValue, "minValue");
        requireFinite(maxValue, "maxValue");
        this.property = property;
        this.weightedScore = weightedScore;
        this.maxPercentage = Math.max(0.0d, maxPercentage);
        this.currentPercentage = clampPercentage(currentPercentage, this.maxPercentage);
        this.currentValue = currentValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ExperimentalProperty getProperty() { return property; }
    public double getWeightedScore() { return weightedScore; }
    public double getMaxPercentage() { return maxPercentage; }
    public double getCurrentPercentage() { return currentPercentage; }
    public double getCurrentValue() { return currentValue; }
    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }

    /**
     * Changes percentage and immediately recalculates current value. This is the
     * correct operation for experimentation after ResourceLabratory::experimentRow.
     */
    public AttributeState withCurrentPercentage(double percentage) {
        double bounded = clampPercentage(percentage, maxPercentage);
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                bounded,
                ResourceLaboratory.interpolate(
                        property.getGroup(), minValue, maxValue, bounded),
                minValue,
                maxValue);
    }

    /**
     * Mirrors AttributesMap::setCurrentPercentage without an immediate
     * CraftingValues::recalculateValues pass. Used by component combination.
     */
    public AttributeState withPercentageOnly(double percentage) {
        double bounded = clampPercentage(percentage, maxPercentage);
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                bounded,
                currentValue,
                minValue,
                maxValue);
    }

    /**
     * Mirrors Values::setValue: preserve the supplied current value while
     * deriving/clamping the current percentage from the effective range.
     */
    public AttributeState withCurrentValue(double value) {
        requireFinite(value, "value");
        double percentage;
        if (Math.abs(maxValue - minValue) <= RANGE_EPSILON) {
            percentage = value - minValue;
        } else if (maxValue > minValue) {
            percentage = (value - minValue) / (maxValue - minValue);
        } else {
            percentage = 1.0d - ((value - maxValue) / (minValue - maxValue));
        }
        percentage = clampPercentage(percentage, maxPercentage);
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                percentage,
                value,
                minValue,
                maxValue);
    }

    public AttributeState withRange(double newMin, double newMax) {
        requireFinite(newMin, "newMin");
        requireFinite(newMax, "newMax");
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                currentPercentage,
                currentValue,
                newMin,
                newMax);
    }

    public AttributeState recalculate() {
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                currentPercentage,
                ResourceLaboratory.interpolate(
                        property.getGroup(), minValue, maxValue, currentPercentage),
                minValue,
                maxValue);
    }

    private static double clampPercentage(double value, double max) {
        requireFinite(value, "percentage");
        if (value > max) value = max;
        if (value < 0.0d) value = 0.0d;
        return value;
    }

    private static void requireFinite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
