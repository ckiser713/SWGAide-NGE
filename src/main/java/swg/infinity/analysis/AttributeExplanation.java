package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Full explainability record for one final CraftingValues attribute. */
public final class AttributeExplanation {
    private final String attribute;
    private final double baseMin;
    private final double baseMax;
    private final double effectiveMin;
    private final double effectiveMax;
    private final double componentMinDelta;
    private final double componentMaxDelta;
    private final double weightedScore;
    private final double maxPercentage;
    private final double currentPercentage;
    private final double currentValue;
    private final List<WeightedStatExplanation> weightedStats;

    public AttributeExplanation(
            String attribute,
            double baseMin,
            double baseMax,
            double effectiveMin,
            double effectiveMax,
            double weightedScore,
            double maxPercentage,
            double currentPercentage,
            double currentValue,
            List<WeightedStatExplanation> weightedStats) {
        if (attribute == null || attribute.trim().isEmpty()) {
            throw new IllegalArgumentException("attribute must not be blank");
        }
        if (weightedStats == null) throw new NullPointerException("weightedStats");
        this.attribute = attribute;
        this.baseMin = baseMin;
        this.baseMax = baseMax;
        this.effectiveMin = effectiveMin;
        this.effectiveMax = effectiveMax;
        this.componentMinDelta = effectiveMin - baseMin;
        this.componentMaxDelta = effectiveMax - baseMax;
        this.weightedScore = weightedScore;
        this.maxPercentage = maxPercentage;
        this.currentPercentage = currentPercentage;
        this.currentValue = currentValue;
        this.weightedStats = Collections.unmodifiableList(
                new ArrayList<WeightedStatExplanation>(weightedStats));
    }

    public String getAttribute() { return attribute; }
    public double getBaseMin() { return baseMin; }
    public double getBaseMax() { return baseMax; }
    public double getEffectiveMin() { return effectiveMin; }
    public double getEffectiveMax() { return effectiveMax; }
    public double getComponentMinDelta() { return componentMinDelta; }
    public double getComponentMaxDelta() { return componentMaxDelta; }
    public double getWeightedScore() { return weightedScore; }
    public double getMaxPercentage() { return maxPercentage; }
    public double getCurrentPercentage() { return currentPercentage; }
    public double getCurrentValue() { return currentValue; }
    public List<WeightedStatExplanation> getWeightedStats() { return weightedStats; }
}
