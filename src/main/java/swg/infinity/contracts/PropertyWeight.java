package swg.infinity.contracts;

import java.util.Objects;

/** One normalized resource-stat contribution to an experimental property. */
public final class PropertyWeight {
    private final ResourceStat stat;
    private final int rawWeight;
    private final double normalizedWeight;

    public PropertyWeight(ResourceStat stat, int rawWeight, double normalizedWeight) {
        if (stat == null) throw new NullPointerException("stat");
        if (rawWeight < 0) {
            throw new IllegalArgumentException("rawWeight must be >= 0");
        }
        if (Double.isNaN(normalizedWeight)
                || Double.isInfinite(normalizedWeight)
                || normalizedWeight < 0.0
                || normalizedWeight > 1.0) {
            throw new IllegalArgumentException(
                    "normalizedWeight must be finite and within [0,1]");
        }
        this.stat = stat;
        this.rawWeight = rawWeight;
        this.normalizedWeight = normalizedWeight;
    }

    public ResourceStat getStat() { return stat; }
    public int getRawWeight() { return rawWeight; }
    public double getNormalizedWeight() { return normalizedWeight; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PropertyWeight)) return false;
        PropertyWeight that = (PropertyWeight) other;
        return rawWeight == that.rawWeight
                && Double.compare(normalizedWeight, that.normalizedWeight) == 0
                && stat == that.stat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stat, rawWeight, normalizedWeight);
    }
}
