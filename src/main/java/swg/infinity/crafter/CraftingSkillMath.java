package swg.infinity.crafter;

/**
 * Deterministic pre-RNG arithmetic copied by behavior from the pinned Infinity
 * crafting call graph.
 *
 * <p>No method in this class returns a probability or outcome tier.</p>
 */
public final class CraftingSkillMath {
    private CraftingSkillMath() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * CraftingSession initializes total experiment points from the base
     * schematic experimentation skill modifier divided by 10. Force
     * experimentation is not added on this path.
     */
    public static int totalExperimentationPoints(int experimentationSkill) {
        return experimentationSkill / 10;
    }

    /**
     * CraftingManager::calculateExperimentationFailureRate intermediate.
     *
     * <p>Despite the variable name in the server source, this integer is later
     * passed to calculateExperimentationSuccess as the parameter named
     * effectiveness.</p>
     */
    public static int experimentationIntermediate(
            double weightedMalleability,
            int experimentationSkill,
            int pointsUsed) {
        requireFinite(weightedMalleability, "weightedMalleability");
        if (pointsUsed < 0) {
            throw new IllegalArgumentException("pointsUsed must be >= 0");
        }
        return (int) (
                50.0d
                + (weightedMalleability - 500.0d) / 40.0d
                + ((double) experimentationSkill / 10.0d)
                - 5.0d * (double) pointsUsed);
    }

    public static int assemblyFailureMitigation(CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        int base = (int) (
                ((double) profile.getAssemblySkill()
                - 100.0d
                + (double) profile.getPrivateAssemblyBonus()) / 7.0d);
        return clampFailureMitigation(
                base + profile.getForceFailureReduction());
    }

    /**
     * Infinity experimentation failure mitigation also references assembly
     * skill, not experimentation skill.
     */
    public static int experimentationFailureMitigation(CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        int base = (int) (
                ((double) profile.getAssemblySkill()
                - 100.0d
                + (double) profile.getPrivateExperimentationBonus()) / 7.0d);
        return clampFailureMitigation(
                base + profile.getForceFailureReduction());
    }

    public static double assemblySkillPoints(CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        return ((double) profile.getAssemblySkill()
                + (double) profile.getForceAssembly()) / 10.0d;
    }

    public static double experimentationSkillPoints(CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        return ((double) profile.getExperimentationSkill()
                + (double) profile.getForceExperimentation()) / 10.0d;
    }

    public static double assemblyToolModifier(CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        double modifier = 1.0d + profile.getToolEffectiveness() / 100.0d;
        return modifier * (1.0d + profile.getCraftFoodBonus() / 100.0d);
    }

    /**
     * The active experimentation call graph passes the failure/intermediate
     * integer into the parameter named effectiveness.
     */
    public static double experimentationToolModifier(
            int experimentationIntermediate,
            CrafterProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        double modifier =
                1.0d + (double) experimentationIntermediate / 100.0d;
        return modifier
                * (1.0d + profile.getExperimentFoodBonus() / 100.0d);
    }

    private static int clampFailureMitigation(int value) {
        if (value < 0) return 0;
        if (value > 5) return 5;
        return value;
    }

    private static void requireFinite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
