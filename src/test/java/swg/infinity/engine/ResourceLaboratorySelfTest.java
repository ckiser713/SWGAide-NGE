package swg.infinity.engine;

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
import swg.infinity.contracts.Provenance;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/**
 * Dependency-free source-derived deterministic smoke tests.
 *
 * <p>This is intentionally a main-based harness because the fork has no
 * committed unit-test dependency. Codex should execute it after mvn
 * test-compile and may later migrate it to the admitted test framework.</p>
 */
public final class ResourceLaboratorySelfTest {

    private ResourceLaboratorySelfTest() {
    }

    public static void main(String[] args) {
        shouldQuantityWeightOnlyNonZeroContributors();
        shouldCalculateUtOnlyMaterialCeiling();
        shouldInterpolateReversedRange();
        shouldClampExperimentationToMaterialCeiling();
        shouldRejectIllegalResourceClass();
        System.out.println("ResourceLaboratorySelfTest PASS");
    }

    private static void shouldQuantityWeightOnlyNonZeroContributors() {
        SchematicDefinition schematic = schematic(
                property("damage", "expDamage", 1.0, 20.0,
                        Arrays.asList(new PropertyWeight(ResourceStat.UT, 1, 1.0))),
                Arrays.asList(
                        slot(0, "steel", 10),
                        slot(1, "metal", 30)));

        ResourceInput hasUt = resource("A", "steel", 1000, 1000);
        ResourceInput zeroUt = resource("B", "metal", 0, 1000);

        ResourceLaboratory lab = new ResourceLaboratory();
        double result = lab.calculateWeightedValue(
                schematic,
                Arrays.asList(
                        new ResourceSlotAssignment(0, hasUt),
                        new ResourceSlotAssignment(1, zeroUt)),
                ResourceStat.UT);

        assertClose(1000.0, result, 0.000001, "non-zero contributor denominator");
    }

    private static void shouldCalculateUtOnlyMaterialCeiling() {
        SchematicDefinition schematic = schematic(
                property("maxdamage", "expDamage", 1.0, 20.0,
                        Arrays.asList(new PropertyWeight(ResourceStat.UT, 1, 1.0))),
                Arrays.asList(
                        slot(0, "steel", 10),
                        slot(1, "metal", 7)));

        ResourceInput steel = resource("Steel992", "steel", 992, 1000);
        ResourceInput metal = resource("Metal992", "metal", 992, 1000);

        CraftState state = new ResourceLaboratory().initialize(
                schematic,
                Arrays.asList(
                        new ResourceSlotAssignment(0, steel),
                        new ResourceSlotAssignment(1, metal)),
                CraftOutcomeTier.GREAT);

        AttributeState damage = state.getAttribute("maxdamage");
        assertClose(992.0, damage.getWeightedScore(), 0.000001, "weighted score");
        assertClose(0.992, damage.getMaxPercentage(), 0.000001, "material ceiling");
        assertClose(
                ResourceLaboratory.assemblyPercentage(992.0),
                damage.getCurrentPercentage(),
                0.000001,
                "initial assembly percentage");
    }

    private static void shouldInterpolateReversedRange() {
        ExperimentalProperty speed = property(
                "attackspeed",
                "expSpeed",
                7.0,
                4.0,
                Arrays.asList(new PropertyWeight(ResourceStat.UT, 1, 1.0)));

        assertClose(
                4.3,
                ResourceLaboratory.interpolate(speed, 0.90),
                0.000001,
                "reversed interpolation");
    }

    private static void shouldClampExperimentationToMaterialCeiling() {
        SchematicDefinition schematic = schematic(
                property("maxdamage", "expDamage", 1.0, 20.0,
                        Arrays.asList(new PropertyWeight(ResourceStat.UT, 1, 1.0))),
                Collections.singletonList(slot(0, "steel", 10)));

        ResourceInput steel = resource("Steel900", "steel", 900, 1000);
        ResourceLaboratory lab = new ResourceLaboratory();
        CraftState initial = lab.initialize(
                schematic,
                Collections.singletonList(new ResourceSlotAssignment(0, steel)),
                CraftOutcomeTier.GREAT);
        CraftState experimented = lab.experiment(
                initial, "expDamage", 20, CraftOutcomeTier.AMAZING);

        assertClose(
                0.900,
                experimented.getAttribute("maxdamage").getCurrentPercentage(),
                0.000001,
                "experiment clamp");
    }

    private static void shouldRejectIllegalResourceClass() {
        SchematicDefinition schematic = schematic(
                property("damage", "expDamage", 1.0, 20.0,
                        Arrays.asList(new PropertyWeight(ResourceStat.UT, 1, 1.0))),
                Collections.singletonList(slot(0, "steel", 10)));

        boolean rejected = false;
        try {
            new ResourceLaboratory().initialize(
                    schematic,
                    Collections.singletonList(
                            new ResourceSlotAssignment(
                                    0, resource("Copper", "copper", 900, 1000))),
                    CraftOutcomeTier.GREAT);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError("illegal resource class was accepted");
        }
    }

    private static SchematicDefinition schematic(
            ExperimentalProperty property,
            List<IngredientSlotDefinition> slots) {
        Provenance source = source();
        return new SchematicDefinition(
                "fixture",
                "Fixture",
                "object/draft_schematic/fixture.iff",
                "object/tangible/fixture.iff",
                LaboratoryType.RESOURCE,
                "crafting_weapon_assembly",
                "crafting_weapon_experimentation",
                slots,
                Collections.singletonList(property),
                "generic",
                source);
    }

    private static IngredientSlotDefinition slot(
            int index, String type, int quantity) {
        return new IngredientSlotDefinition(
                index,
                type,
                SlotKind.RESOURCE,
                type,
                quantity,
                0.0,
                source());
    }

    private static ExperimentalProperty property(
            String attribute,
            String group,
            double min,
            double max,
            List<PropertyWeight> weights) {
        return new ExperimentalProperty(
                attribute,
                group,
                min,
                max,
                2,
                false,
                CombineType.RESOURCE,
                weights,
                source());
    }

    private static ResourceInput resource(
            String name, String type, int ut, long available) {
        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.UT, Integer.valueOf(ut));
        return new ResourceInput(
                name,
                type,
                new HashSet<String>(Arrays.asList(type, "metal")),
                stats,
                available,
                ResourceOrigin.MANUAL);
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
