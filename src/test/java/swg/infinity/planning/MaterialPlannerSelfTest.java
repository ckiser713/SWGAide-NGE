package swg.infinity.planning;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import swg.crafting.simulator.components.ComponentInstance;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.component.ComponentSlotAssignment;
import swg.crafting.simulator.components.ComponentUse;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.planning.MaterialPlan;
import swg.crafting.simulator.planning.MaterialPlanner;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.engine.ResourceSlotAssignment;

/** Dependency-free material plan and bottleneck tests. */
public final class MaterialPlannerSelfTest {
    private MaterialPlannerSelfTest() {
    }

    public static void main(String[] args) {
        Provenance source = new Provenance(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "fixture");
        SchematicDefinition schematic = new SchematicDefinition(
                "fixture", "Fixture",
                "object/draft_schematic/fixture.iff",
                "object/tangible/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly", "experiment",
                Arrays.asList(
                        new IngredientSlotDefinition(
                                0, "Steel", SlotKind.RESOURCE,
                                "steel", 10, 0.0d, source),
                        new IngredientSlotDefinition(
                                1, "Enhancement", SlotKind.OPTIONAL_MIXED_COMPONENT,
                                "enhancement", 2, 1.0d, source)),
                Collections.emptyList(),
                "generic",
                source);

        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.UT, Integer.valueOf(900));
        ResourceInput steel = new ResourceInput(
                "SteelA", "steel",
                new HashSet<String>(Arrays.asList("steel", "metal")),
                stats, 95L, ResourceOrigin.INVENTORY);

        ComponentInstance enhancement = new ComponentInstance(
                "enhancement-1",
                "object/tangible/component/enhancement.iff",
                "", 5, ComponentOrigin.OWNED_LOOT,
                Collections.emptyList());

        CraftScenario scenario = new CraftScenario(
                schematic,
                Collections.singletonList(
                        new ResourceSlotAssignment(0, steel)),
                Collections.singletonList(
                        new ComponentSlotAssignment(
                                1,
                                Collections.singletonList(
                                        new ComponentUse(enhancement, 2)))),
                CraftOutcomeTier.GREAT,
                Collections.emptyList());

        MaterialPlanner planner = new MaterialPlanner();
        MaterialPlan plan = planner.plan(scenario, 3);

        if (plan.getResourceUnits().get("SteelA").longValue() != 30L) {
            throw new AssertionError("resource plan incorrect");
        }
        if (plan.getComponentUses().get("enhancement-1").longValue() != 6L) {
            throw new AssertionError("component plan incorrect");
        }
        if (planner.maximumCraftCount(scenario) != 2L) {
            throw new AssertionError("component should be limiting bottleneck");
        }
        System.out.println("MaterialPlannerSelfTest PASS");
    }
}
