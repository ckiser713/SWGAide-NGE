package swg.infinity.crafter;

/** Dependency-free source-derived pre-RNG skill arithmetic tests. */
public final class CraftingSkillMathSelfTest {
    private CraftingSkillMathSelfTest() {
    }

    public static void main(String[] args) {
        if (CraftingSkillMath.totalExperimentationPoints(119) != 11) {
            throw new AssertionError("experiment points must use integer /10");
        }
        if (CraftingSkillMath.experimentationIntermediate(
                800.0d, 120, 3) != 54) {
            throw new AssertionError("experimentation intermediate mismatch");
        }

        CrafterProfile profile = new CrafterProfile(
                120, 120, 20, 30, 2, 50, 10,
                10, 10, 5.0d, 10.0d, 15.0d);

        if (CraftingSkillMath.assemblyFailureMitigation(profile) != 5) {
            throw new AssertionError("assembly fail mitigation clamp mismatch");
        }
        if (CraftingSkillMath.experimentationFailureMitigation(profile) != 5) {
            throw new AssertionError("experiment fail mitigation clamp mismatch");
        }
        assertClose(
                14.0d,
                CraftingSkillMath.assemblySkillPoints(profile),
                0.000001d,
                "assembly skill points");
        assertClose(
                15.0d,
                CraftingSkillMath.experimentationSkillPoints(profile),
                0.000001d,
                "experiment skill points");
        System.out.println("CraftingSkillMathSelfTest PASS");
    }

    private static void assertClose(
            double expected, double actual, double tolerance, String label) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(
                    label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
