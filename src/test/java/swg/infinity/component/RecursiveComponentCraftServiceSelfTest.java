package swg.infinity.component;

import java.util.Arrays;
import java.util.Collections;

import swg.crafting.simulator.components.ComponentInstance;
import swg.crafting.simulator.components.RecursiveCraftCycleDetector;
import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.SchematicDefinition;

/** Dependency-free nested component orchestration smoke test. */
public final class RecursiveComponentCraftServiceSelfTest {
    private RecursiveComponentCraftServiceSelfTest() {
    }

    public static void main(String[] args) {
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        Provenance source = new Provenance(
                "swginfinity/public", commit, "fixture");

        ExperimentalProperty fixed = new ExperimentalProperty(
                "maxdamage", "", 42.0d, 42.0d, 0, false,
                CombineType.OVERRIDE,
                Collections.emptyList(), source);

        SchematicDefinition schematic = new SchematicDefinition(
                "component-fixture",
                "Component Fixture",
                "object/draft_schematic/component/fixture.iff",
                "object/tangible/component/fixture.iff",
                LaboratoryType.RESOURCE,
                "weapon_assembly",
                "weapon_experimentation",
                Collections.emptyList(),
                Collections.singletonList(fixed),
                "generic",
                source);

        InfinityRuleset ruleset = new InfinityRuleset(
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
                                "component-fixture",
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                EvidenceState.EXACT,
                                Arrays.asList("fixture"))));

        CraftScenario scenario = new CraftScenario(
                schematic,
                Collections.emptyList(),
                Collections.emptyList(),
                CraftOutcomeTier.GREAT,
                Collections.emptyList());

        ComponentInstance component =
                new RecursiveComponentCraftService().craft(
                        "root/component-fixture",
                        "crafted-component-1",
                        schematic.getTargetTemplate(),
                        "(SERIAL)",
                        ruleset,
                        scenario,
                        new RecursiveCraftCycleDetector());

        if (component == null
                || component.getProperties().isEmpty()
                || !"maxdamage".equals(
                        component.getProperties().get(0).getAttribute())) {
            throw new AssertionError(
                    "nested craft did not become a reusable component");
        }

        System.out.println("RecursiveComponentCraftServiceSelfTest PASS");
    }
}
