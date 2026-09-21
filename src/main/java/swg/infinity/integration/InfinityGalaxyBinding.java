package swg.infinity.integration;

import swg.crafting.simulator.contracts.RulesetManifest;
import swg.model.SWGCGalaxy;

/** Central feature gate for the SWGAide SWG Infinity server identity. */
public final class InfinityGalaxyBinding {
    public static final int SWGAIDE_INFINITY_SERVER_ID = 154;

    private InfinityGalaxyBinding() {
        throw new AssertionError("Do not instantiate");
    }

    public static boolean isInfinity(SWGCGalaxy galaxy) {
        return galaxy != null && galaxy.id() == SWGAIDE_INFINITY_SERVER_ID;
    }

    public static void requireInfinity(SWGCGalaxy galaxy) {
        if (!isInfinity(galaxy)) {
            throw new IllegalArgumentException(
                    "Infinity Crafting Lab requires SWGAide server id "
                    + SWGAIDE_INFINITY_SERVER_ID);
        }
    }

    public static void requireCompatibleRuleset(
            SWGCGalaxy galaxy,
            RulesetManifest manifest) {
        requireInfinity(galaxy);
        if (manifest == null) throw new NullPointerException("manifest");
        if (manifest.getSwgAideServerId() != SWGAIDE_INFINITY_SERVER_ID) {
            throw new IllegalArgumentException(
                    "Ruleset targets SWGAide server "
                    + manifest.getSwgAideServerId()
                    + ", expected " + SWGAIDE_INFINITY_SERVER_ID);
        }
    }
}
