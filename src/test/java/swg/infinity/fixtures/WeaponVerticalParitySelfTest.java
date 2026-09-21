package swg.infinity.fixtures;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.infinity.extract.Extractor;

/**
 * Aggregates every accepted weapon-vertical fixture and asserts that
 * the Infinity ruleset emitted by {@link Extractor} reproduces each
 * fixture's structural and numeric truth within the project
 * tolerances (slot count, slot kinds, slot quantities, accepted
 * resource types, target template, assembly/experimentation skills).
 *
 * <p>Tolerances are the project defaults from
 * {@code docs/infinity/GOLDEN_FIXTURE_PLAN.md}: percentages
 * {@code 1e-4}, weighted scores {@code 1e-6}, attributes
 * {@code 1e-2}. This test does <em>not</em> widen tolerances to
 * pass; failures must be investigated and corrected as
 * {@code fix(infinity)} commits.</p>
 *
 * <p>Output is the literal {@code WeaponVerticalParitySelfTest PASS
 * (n/n)}; the {@code n/n} is the accepted-fixture count out of the
 * total considered.</p>
 */
public final class WeaponVerticalParitySelfTest {
    private WeaponVerticalParitySelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        File source = new File(
                "/home/thenexussidekick/swgaide-deps/swginfinity-public");
        if (!source.isDirectory()) {
            throw new AssertionError(
                    "Infinity source checkout missing at " + source);
        }
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        InfinityRuleset ruleset = new Extractor(source, commit).extractWeapons();

        File seedsDir = new File(
                "src/test/resources/swg/infinity/fixtures/seeds");
        File[] files = seedsDir.listFiles();
        if (files == null || files.length == 0) {
            throw new AssertionError("seed fixtures missing under " + seedsDir);
        }
        Arrays.sort(files);

        List<String> failures = new ArrayList<String>();
        int considered = 0;
        for (File f : files) {
            if (!f.getName().endsWith(".json")) continue;
            ++considered;
            Object json = SeedFixtureParser.parseFile(f);
            SeedFixture seed = SeedFixtureParser.parseSeed(json);
            String infinityId = f.getName().replace(".json", "");
            SchematicDefinition def = ruleset.getSchematic(infinityId);
            if (def == null) {
                failures.add(seed.getId() + ": extracted schematic missing");
                continue;
            }
            String reason = compare(seed, def);
            if (reason != null) {
                failures.add(seed.getId() + ": " + reason);
            }
        }
        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("weapon vertical parity failures (")
                    .append(failures.size()).append("):\n");
            for (String fail : failures) sb.append("  - ").append(fail).append('\n');
            throw new AssertionError(sb.toString());
        }
        int accepted = considered;
        System.out.println(
                "WeaponVerticalParitySelfTest PASS (" + accepted + "/"
                + considered + ")");
    }

    private static String compare(SeedFixture seed, SchematicDefinition def) {
        if (def.getSlots().size() != seed.getSlots().size()) {
            return "slot count mismatch (ext=" + def.getSlots().size()
                    + " seed=" + seed.getSlots().size() + ")";
        }
        if (!def.getTargetTemplate().equals(seed.getTargetTemplate())) {
            return "target template mismatch (ext=" + def.getTargetTemplate()
                    + " seed=" + seed.getTargetTemplate() + ")";
        }
        String asm = def.getAssemblySkill();
        if (asm == null || asm.isEmpty()
                || !asm.equals(seed.getAssemblySkill())) {
            return "assembly skill mismatch (ext=" + asm
                    + " seed=" + seed.getAssemblySkill() + ")";
        }
        String exp = def.getExperimentationSkill();
        if (exp == null || exp.isEmpty()
                || !exp.equals(seed.getExperimentingSkill())) {
            return "experimenting skill mismatch (ext=" + exp
                    + " seed=" + seed.getExperimentingSkill() + ")";
        }
        for (int i = 0; i < seed.getSlots().size(); ++i) {
            SeedFixture.SeedSlot ss = seed.getSlots().get(i);
            IngredientSlotDefinition is = def.getSlots().get(i);
            int expectedCode = kindToCode(is.getKind());
            if (expectedCode != ss.getSlotKind()) {
                return "slot[" + i + "] kind mismatch (ext="
                        + is.getKind() + "/" + expectedCode
                        + " seed=" + ss.getSlotKind() + ")";
            }
            if (is.getQuantity() != ss.getQuantity()) {
                return "slot[" + i + "] quantity mismatch (ext="
                        + is.getQuantity() + " seed=" + ss.getQuantity() + ")";
            }
            if (!is.getAcceptedType().equals(ss.getResourceType())) {
                return "slot[" + i + "] accepted type mismatch (ext="
                        + is.getAcceptedType() + " seed="
                        + ss.getResourceType() + ")";
            }
            // Contribution: project default tolerance is percentages 1e-4.
            double diff = Math.abs(is.getContribution() - ss.getContribution());
            if (diff > 1e-4) {
                return "slot[" + i + "] contribution out of tolerance (ext="
                        + is.getContribution() + " seed="
                        + ss.getContribution() + ")";
            }
        }
        return null;
    }

    private static int kindToCode(SlotKind kind) {
        if (kind == null) return -1;
        switch (kind) {
        case RESOURCE: return 0;
        case IDENTICAL_COMPONENT: return 1;
        case MIXED_COMPONENT: return 2;
        case OPTIONAL_IDENTICAL_COMPONENT: return 3;
        case OPTIONAL_MIXED_COMPONENT: return 4;
        default: return -1;
        }
    }
}
