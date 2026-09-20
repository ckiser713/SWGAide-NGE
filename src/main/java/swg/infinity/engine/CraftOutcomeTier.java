package swg.infinity.engine;

/**
 * Forced assembly/experimentation result tiers used by deterministic mode.
 *
 * <p>The numeric modifiers are derived from the pinned SWG Infinity
 * SharedLabratory implementation. This enum deliberately does not encode an
 * assumed Core3 enum ordinal.</p>
 */
public enum CraftOutcomeTier {
    AMAZING(1.05d, 0.080d),
    GREAT(1.00d, 0.070d),
    GOOD(0.90d, 0.055d),
    MODERATE(0.80d, 0.015d),
    SUCCESS(0.70d, 0.010d),
    MARGINAL(0.60d, 0.000d),
    OK(0.50d, -0.040d),
    BARELY(0.40d, -0.070d),
    CRITICAL(0.30d, -0.080d);

    private final double assemblyModifier;
    private final double experimentModifierPerPoint;

    CraftOutcomeTier(double assemblyModifier, double experimentModifierPerPoint) {
        this.assemblyModifier = assemblyModifier;
        this.experimentModifierPerPoint = experimentModifierPerPoint;
    }

    public double getAssemblyModifier() {
        return assemblyModifier;
    }

    public double getExperimentModifierPerPoint() {
        return experimentModifierPerPoint;
    }
}
