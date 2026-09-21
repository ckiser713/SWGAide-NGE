package swg.crafting.simulator.compare;

/** One functional statistic delta between two completed craft results. */
public final class ResultDelta {
    private final String attribute;
    private final Double baseline;
    private final Double candidate;
    private final Double delta;

    public ResultDelta(
            String attribute,
            Double baseline,
            Double candidate) {
        if (attribute == null || attribute.trim().isEmpty()) {
            throw new IllegalArgumentException("attribute must not be blank");
        }
        this.attribute = attribute;
        this.baseline = baseline;
        this.candidate = candidate;
        this.delta = baseline == null || candidate == null
                ? null
                : Double.valueOf(candidate.doubleValue() - baseline.doubleValue());
    }

    public String getAttribute() { return attribute; }
    public Double getBaseline() { return baseline; }
    public Double getCandidate() { return candidate; }
    public Double getDelta() { return delta; }
}
