package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.infinity.contracts.ResourceStat;

/** Explanation of one resource-stat term inside an experimental property score. */
public final class WeightedStatExplanation {
    private final ResourceStat stat;
    private final double normalizedWeight;
    private final double weightedStatValue;
    private final double propertyScoreContribution;
    private final List<InputContribution> inputs;

    public WeightedStatExplanation(
            ResourceStat stat,
            double normalizedWeight,
            double weightedStatValue,
            double propertyScoreContribution,
            List<InputContribution> inputs) {
        if (stat == null) throw new NullPointerException("stat");
        if (inputs == null) throw new NullPointerException("inputs");
        this.stat = stat;
        this.normalizedWeight = normalizedWeight;
        this.weightedStatValue = weightedStatValue;
        this.propertyScoreContribution = propertyScoreContribution;
        this.inputs = Collections.unmodifiableList(
                new ArrayList<InputContribution>(inputs));
    }

    public ResourceStat getStat() { return stat; }
    public double getNormalizedWeight() { return normalizedWeight; }
    public double getWeightedStatValue() { return weightedStatValue; }
    public double getPropertyScoreContribution() { return propertyScoreContribution; }
    public List<InputContribution> getInputs() { return inputs; }
}
