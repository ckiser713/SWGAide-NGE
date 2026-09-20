package swg.infinity.engine;

import java.util.Arrays;
import java.util.Collections;

import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.CoverageRecord;
import swg.infinity.contracts.EvidenceState;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.Provenance;
import swg.infinity.contracts.RulesetManifest;
import swg.infinity.contracts.SchematicDefinition;

/** Dependency-free fail-closed execution-gate tests. */
public final class InfinityCraftServiceSelfTest {
    private InfinityCraftServiceSelfTest() {
    }

    public static void main(String[] args) {
        shouldRejectPartialCoverage();
        shouldRejectScenarioOutsideRuleset();
        System.out.println("InfinityCraftServiceSelfTest PASS");
    }

    private static void shouldRejectPartialCoverage() {
        SchematicDefinition schematic = schematic("one");
        InfinityRuleset ruleset = new InfinityRuleset(
                manifest(),
                Collections.singletonList(schematic),
                Collections.singletonList(new CoverageRecord(
                        "one",
                        EvidenceState.EXACT,
                        EvidenceState.EXACT,
                        EvidenceState.EXACT,
                        EvidenceState.PARTIAL,
                        Collections.<String>emptyList())));

        boolean rejected = false;
        try {
            new InfinityCraftService().executeExact(
                    ruleset,
                    emptyScenario(schematic));
        } catch (UnsupportedInfinityRuleException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("partial coverage executed as exact");
    }

    private static void shouldRejectScenarioOutsideRuleset() {
        SchematicDefinition canonical = schematic("canonical");
        InfinityRuleset ruleset = new InfinityRuleset(
                manifest(),
                Collections.singletonList(canonical),
                Collections.singletonList(exactCoverage("canonical")));

        boolean rejected = false;
        try {
            new InfinityCraftService().executeExact(
                    ruleset,
                    emptyScenario(schematic("other")));
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("out-of-ruleset scenario accepted");
    }

    private static CraftScenario emptyScenario(SchematicDefinition schematic) {
        return new CraftScenario(
                schematic,
                Collections.<ResourceSlotAssignment>emptyList(),
                Collections.emptyList(),
                CraftOutcomeTier.GREAT,
                Collections.<ExperimentStep>emptyList());
    }

    private static SchematicDefinition schematic(String id) {
        Provenance source = source();
        return new SchematicDefinition(
                id,
                id,
                "object/draft_schematic/" + id + ".iff",
                "object/tangible/" + id + ".iff",
                LaboratoryType.RESOURCE,
                "assembly",
                "experiment",
                Collections.emptyList(),
                Collections.singletonList(new ExperimentalProperty(
                        "fixed", "", 1.0d, 1.0d, 0, true,
                        CombineType.OVERRIDE,
                        Collections.emptyList(),
                        source)),
                "generic",
                source);
    }

    private static CoverageRecord exactCoverage(String id) {
        return new CoverageRecord(
                id,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                Arrays.asList("fixture"));
    }

    private static RulesetManifest manifest() {
        return new RulesetManifest(
                1,
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "foundation",
                "fixture-hash",
                154);
    }

    private static Provenance source() {
        return new Provenance(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "fixture");
    }
}
