package swg.infinity.runtime;

import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.InfinityRuleset;

/**
 * Post-generation smoke test for the packaged source-free Infinity ruleset.
 *
 * <p>This test is intentionally not in FoundationSelfTestSuite until the
 * generated artifact is committed.</p>
 */
public final class InfinityBundledRulesetSelfTest {
    private InfinityBundledRulesetSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        InfinityRuleset ruleset = InfinityRuntimeBootstrap.loadBundled();
        if (ruleset == null) {
            throw new AssertionError(
                    "bundled Infinity runtime ruleset is missing at "
                    + InfinityRuntimeBootstrap.BUNDLED_RULESET);
        }
        if (ruleset.getTargetServerId() != 154) {
            throw new AssertionError("bundled ruleset server id mismatch");
        }

        String[] exact = {
            "pistol_blaster_dl44",
            "carbine_geo",
            "pistol_blaster_scout_trooper",
            "rifle_berserker"
        };
        for (String id : exact) {
            if (ruleset.getSchematic(id) == null) {
                throw new AssertionError(
                        "bundled ruleset missing admitted schematic " + id);
            }
            if (ruleset.getCoverage(id) == null
                    || ruleset.getCoverage(id).overall()
                        != EvidenceState.EXACT) {
                throw new AssertionError(
                        "bundled schematic is not EXACT: " + id);
            }
        }

        System.out.println(
                "InfinityBundledRulesetSelfTest PASS schematics="
                + ruleset.getSchematics().size()
                + " hash=" + ruleset.getRulesetHash());
    }
}
