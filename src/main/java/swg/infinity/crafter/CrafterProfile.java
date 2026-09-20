package swg.infinity.crafter;

/**
 * Immutable skill/buff inputs observed by the pinned Infinity crafting success
 * paths. This object stores values only; it does not imply RNG parity.
 */
public final class CrafterProfile {
    private final int assemblySkill;
    private final int experimentationSkill;
    private final int forceAssembly;
    private final int forceExperimentation;
    private final int forceFailureReduction;
    private final int luck;
    private final int forceLuck;
    private final int privateAssemblyBonus;
    private final int privateExperimentationBonus;
    private final double toolEffectiveness;
    private final double craftFoodBonus;
    private final double experimentFoodBonus;

    public CrafterProfile(
            int assemblySkill,
            int experimentationSkill,
            int forceAssembly,
            int forceExperimentation,
            int forceFailureReduction,
            int luck,
            int forceLuck,
            int privateAssemblyBonus,
            int privateExperimentationBonus,
            double toolEffectiveness,
            double craftFoodBonus,
            double experimentFoodBonus) {
        this.assemblySkill = assemblySkill;
        this.experimentationSkill = experimentationSkill;
        this.forceAssembly = forceAssembly;
        this.forceExperimentation = forceExperimentation;
        this.forceFailureReduction = forceFailureReduction;
        this.luck = luck;
        this.forceLuck = forceLuck;
        this.privateAssemblyBonus = privateAssemblyBonus;
        this.privateExperimentationBonus = privateExperimentationBonus;
        this.toolEffectiveness = requireFinite(toolEffectiveness, "toolEffectiveness");
        this.craftFoodBonus = requireFinite(craftFoodBonus, "craftFoodBonus");
        this.experimentFoodBonus =
                requireFinite(experimentFoodBonus, "experimentFoodBonus");
    }

    public int getAssemblySkill() { return assemblySkill; }
    public int getExperimentationSkill() { return experimentationSkill; }
    public int getForceAssembly() { return forceAssembly; }
    public int getForceExperimentation() { return forceExperimentation; }
    public int getForceFailureReduction() { return forceFailureReduction; }
    public int getLuck() { return luck; }
    public int getForceLuck() { return forceLuck; }
    public int getPrivateAssemblyBonus() { return privateAssemblyBonus; }
    public int getPrivateExperimentationBonus() { return privateExperimentationBonus; }
    public double getToolEffectiveness() { return toolEffectiveness; }
    public double getCraftFoodBonus() { return craftFoodBonus; }
    public double getExperimentFoodBonus() { return experimentFoodBonus; }

    private static double requireFinite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
