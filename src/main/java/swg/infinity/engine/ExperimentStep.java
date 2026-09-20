package swg.infinity.engine;

/** One deterministic experimentation action in a craft scenario. */
public final class ExperimentStep {
    private final String group;
    private final int points;
    private final CraftOutcomeTier outcome;

    public ExperimentStep(String group, int points, CraftOutcomeTier outcome) {
        if (group == null || group.trim().isEmpty()) {
            throw new IllegalArgumentException("group must not be blank");
        }
        if (points < 1) throw new IllegalArgumentException("points must be >= 1");
        if (outcome == null) throw new NullPointerException("outcome");
        this.group = group;
        this.points = points;
        this.outcome = outcome;
    }

    public String getGroup() { return group; }
    public int getPoints() { return points; }
    public CraftOutcomeTier getOutcome() { return outcome; }
}
