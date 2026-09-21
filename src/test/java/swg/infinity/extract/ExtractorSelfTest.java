package swg.infinity.extract;

import java.io.File;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SchematicDefinition;

/**
 * Exercises the sandboxed Infinity extractor:
 * - Lua reader parses schematic files;
 * - Extractor emits a normalized InfinityRuleset;
 * - manifest repository and commit match the pinned source;
 * - ruleset hash is stable across runs (determinism);
 * - unknown Lua constructs fail closed (no silent drops).
 */
public final class ExtractorSelfTest {
    private ExtractorSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        File source = new File("/home/thenexussidekick/swgaide-deps/swginfinity-public");
        if (!source.isDirectory()) {
            throw new AssertionError(
                    "Infinity source checkout missing at " + source
                    + ". T4 requires the pinned swginfinity/public clone.");
        }
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        Extractor ex = new Extractor(source, commit);
        InfinityRuleset ruleset = ex.extractWeapons();
        List<SchematicDefinition> schematics = ruleset.getSchematics();
        if (schematics.isEmpty()) {
            throw new AssertionError(
                    "no weapon schematics extracted; check source path");
        }

        // Manifest fields
        if (!"swginfinity/public"
                .equals(ruleset.getManifest().getRepository())) {
            throw new AssertionError("manifest.repository mismatch");
        }
        if (!commit.equals(ruleset.getManifest().getCommit())) {
            throw new AssertionError("manifest.commit mismatch");
        }
        if (ruleset.getManifest().getSwgAideServerId() != 154) {
            throw new AssertionError("server id must be 154");
        }

        // Determinism: re-extract and compare hash
        InfinityRuleset second = new Extractor(source, commit).extractWeapons();
        if (!ruleset.getRulesetHash().equals(second.getRulesetHash())) {
            throw new AssertionError("ruleset hash is not deterministic");
        }

        // Spot-check one schematic (DL44)
        SchematicDefinition dl44 = ruleset.getSchematic("pistol_blaster_dl44");
        if (dl44 == null) {
            throw new AssertionError("DL44 schematic not extracted");
        }
        if (dl44.getSlots().size() != 6) {
            throw new AssertionError(
                    "DL44 must have 6 slots, got " + dl44.getSlots().size());
        }

        // Coverage records reflect source-derived extraction state
        List<CoverageRecord> coverage = ruleset.getCoverage("pistol_blaster_dl44")
                == null ? null : java.util.Arrays.asList(
                        new CoverageRecord[] { ruleset.getCoverage(
                                "pistol_blaster_dl44") });
        if (coverage == null || coverage.isEmpty()) {
            throw new AssertionError("DL44 coverage missing");
        }

        // Lua reader fail-closed: an unknown construct must raise.
        boolean rejected = false;
        try {
            LuaTemplateStubLoader.loadFirstTable(new File(
                    "src/test/resources/swg/infinity/extract/bad_construct.lua"));
        } catch (RuntimeException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "Lua reader must fail closed on unhandled constructs");
        }

        System.out.println(
                "ExtractorSelfTest PASS (" + schematics.size() + " weapons)");
    }
}
