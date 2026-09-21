package swg.infinity.runtime;

import java.io.IOException;
import java.io.InputStream;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.integration.InfinityGalaxyBinding;
import swg.infinity.ruleset.RulesetAdmission;

/**
 * Loads the packaged effective Infinity ruleset without requiring a server
 * source checkout at application runtime.
 */
public final class InfinityRuntimeBootstrap {
    public static final String PINNED_COMMIT =
            "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
    public static final String BUNDLED_RULESET =
            "/swg/crafting/simulator/server/infinity/weapon-ruleset.json";

    private InfinityRuntimeBootstrap() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Returns the admitted bundled ruleset, or null when the artifact has not
     * yet been packaged. Malformed/incompatible artifacts fail closed.
     */
    public static InfinityRuleset loadBundled() throws IOException {
        InputStream in =
                InfinityRuntimeBootstrap.class.getResourceAsStream(
                        BUNDLED_RULESET);
        if (in == null) return null;
        try {
            InfinityRuleset ruleset = InfinityRulesetJsonCodec.read(in);
            return new RulesetAdmission(
                    PINNED_COMMIT,
                    InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID)
                    .admit(ruleset);
        } finally {
            in.close();
        }
    }
}
