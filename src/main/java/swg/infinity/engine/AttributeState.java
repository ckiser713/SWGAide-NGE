package swg.infinity.engine;

import swg.infinity.contracts.ExperimentalProperty;

/** Immutable calculation state for one crafting attribute. */
public final class AttributeState {
    private final ExperimentalProperty property;
    private final double weightedScore;
    private final double maxPercentage;
    private final double currentPercentage;
    private final double currentValue;

    public AttributeState(
            ExperimentalProperty property,
            double weightedScore,
            double maxPercentage,
            double currentPercentage,
            double currentValue) {
        if (property == null) throw new NullPointerException("property");
        requireFinite(weightedScore, "weightedScore");
        requireFinite(maxPercentage, "maxPercentage");
        requireFinite(currentPercentage, "currentPercentage");
        requireFinite(currentValue, "currentValue");
        this.property = property;
        this.weightedScore = weightedScore;
        this.maxPercentage = maxPercentage;
        this.currentPercentage = currentPercentage;
        this.currentValue = currentValue;
    }

    public ExperimentalProperty getProperty() { return property; }
    public double getWeightedScore() { return weightedScore; }
    public double getMaxPercentage() { return maxPercentage; }
    public double getCurrentPercentage() { return currentPercentage; }
    public double getCurrentValue() { return currentValue; }

    public AttributeState withCurrentPercentage(double percentage) {
        double bounded = Math.max(0.0d, Math.min(maxPercentage, percentage));
        return new AttributeState(
                property,
                weightedScore,
                maxPercentage,
                bounded,
                ResourceLaboratory.interpolate(property, bounded));
    }

    private static void requireFinite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
