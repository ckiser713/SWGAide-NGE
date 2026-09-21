package swg.infinity.engine;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftOutcomeTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.PropertyWeight;
import swg.crafting.simulator.contracts.Provenance;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/** Dependency-free end-to-end deterministic engine smoke test. */
public final class InfinityCraftEngineSelfTest {
    private InfinityCraftEngineSelfTest() {
    }

    public static void main(String[] args) {
        shouldProduceFunctionalWeaponResult();
        System.out.println("InfinityCraftEngineSelfTest PASS");
    }

    private static void shouldProduceFunctionalWeaponResult() {
        List<ExperimentalProperty> properties = new ArrayList<ExperimentalProperty>();
        properties.add(weighted("mindamage", 10.0d, 50.0d));
        properties.add(weighted("maxdamage", 20.0d, 100.0d));
        properties.add(weighted("attackspeed", 5.0d, 3.0d));
        properties.add(fixed("attackhealthcost", 20.0d));
        properties.add(fixed("attackactioncost", 30.0d));
        properties.add(fixed("attackmindcost", 40.0d));
        properties.add(weighted("hitpoints", 500.0d, 1000.0d));

        SchematicDefinition schematic = new SchematicDefinition(
                "weapon-fixture",
                "Weapon Fixture",
                "object/draft_schematic/weapon/fixture.iff",
                "object/weapon/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly",
                "experimentation",
                Collections.singletonList(new IngredientSlotDefinition(
                        0, "Steel", SlotKind.RESOURCE,
                        "steel", 10, 0.0d, source())),
                properties,
                "weapon",
                source());

        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.UT, Integer.valueOf(1000));
        ResourceInput steel = new ResourceInput(
                "Perfect Steel",
                "steel",
                new HashSet<String>(Arrays.asList("steel", "metal")),
                stats,
                1000,
                ResourceOrigin.MANUAL);

        CraftScenario scenario = new CraftScenario(
                schematic,
                Collections.singletonList(new ResourceSlotAssignment(0, steel)),
                Collections.emptyList(),
                CraftOutcomeTier.GREAT,
                Collections.singletonList(
                        new ExperimentStep("exp", 20, CraftOutcomeTier.AMAZING)));

        CraftResult result = new InfinityCraftEngine().execute(scenario);

        assertClose(
                50.0d,
                result.getFunctionalResult().get("mindamage").doubleValue(),
                0.000001d,
                "min damage");
        assertClose(
                100.0d,
                result.getFunctionalResult().get("maxdamage").doubleValue(),
                0.000001d,
                "max damage");
        assertClose(
                3.0d,
                result.getFunctionalResult().get("attackspeed").doubleValue(),
                0.000001d,
                "attack speed");
        assertClose(
                1000.0d,
                result.getFunctionalResult().get("hitpoints").doubleValue(),
                0.000001d,
                "condition");
    }

    private static ExperimentalProperty weighted(
            String name, double min, double max) {
        return new ExperimentalProperty(
                name,
                "exp",
                min,
                max,
                2,
                false,
                CombineType.RESOURCE,
                Collections.singletonList(
                        new PropertyWeight(ResourceStat.UT, 1, 1.0d)),
                source());
    }

    private static ExperimentalProperty fixed(String name, double value) {
        return new ExperimentalProperty(
                name,
                "",
                value,
                value,
                0,
                true,
                CombineType.OVERRIDE,
                Collections.emptyList(),
                source());
    }

    private static Provenance source() {
        return new Provenance(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "fixture");
    }

    private static void assertClose(
            double expected, double actual, double tolerance, String label) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(
                    label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
