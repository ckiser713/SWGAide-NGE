package swg.infinity.runtime;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/** Dependency-free round-trip test for the packaged runtime ruleset codec. */
public final class InfinityRulesetJsonCodecSelfTest {
    private InfinityRulesetJsonCodecSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        String commit = InfinityRuntimeBootstrap.PINNED_COMMIT;
        Provenance p = new Provenance(
                "swginfinity/public", commit, "fixture");

        SchematicDefinition schematic = new SchematicDefinition(
                "fixture",
                "Fixture Weapon",
                "object/draft_schematic/weapon/fixture.iff",
                "object/weapon/fixture.iff",
                LaboratoryType.RESOURCE,
                "weapon_assembly",
                "weapon_experimentation",
                Collections.singletonList(
                        new IngredientSlotDefinition(
                                0, "frame", SlotKind.RESOURCE,
                                "metal", 20, 100.0d, p)),
                Collections.singletonList(
                        new ExperimentalProperty(
                                "maxdamage",
                                "expDamage",
                                10.0d, 100.0d, 0, false,
                                CombineType.LINEAR,
                                Collections.singletonList(
                                        new PropertyWeight(
                                                ResourceStat.OQ, 1, 1.0d)),
                                p)),
                "weapon",
                p);

        InfinityRuleset source = new InfinityRuleset(
                new RulesetManifest(
                        1,
                        "swginfinity/public",
                        commit,
                        "fixture",
                        "fixture-hash",
                        154),
                Collections.singletonList(schematic),
                Collections.singletonList(
                        new CoverageRecord(
                                "fixture",
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                Arrays.asList("fixture"))));

        StringWriter out = new StringWriter();
        InfinityRulesetJsonCodec.write(source, out);

        InfinityRuleset restored =
                InfinityRulesetJsonCodec.read(
                        new ByteArrayInputStream(
                                out.toString().getBytes("UTF-8")));

        if (!restored.getManifest().equals(source.getManifest())) {
            throw new AssertionError("manifest round-trip mismatch");
        }
        SchematicDefinition actual = restored.getSchematic("fixture");
        if (actual == null || actual.getProperties().size() != 1
                || actual.getSlots().size() != 1) {
            throw new AssertionError("schematic round-trip mismatch");
        }
        if (restored.getCoverage("fixture").overall()
                != EvidenceState.EXACT) {
            throw new AssertionError("coverage round-trip mismatch");
        }

        System.out.println("InfinityRulesetJsonCodecSelfTest PASS");
    }
}
