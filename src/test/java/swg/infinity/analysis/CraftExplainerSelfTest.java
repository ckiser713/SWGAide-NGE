package swg.infinity.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
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
import swg.infinity.engine.CraftOutcomeTier;
import swg.infinity.engine.CraftResult;
import swg.infinity.engine.CraftScenario;
import swg.infinity.engine.InfinityCraftEngine;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.engine.ResourceSlotAssignment;

/** Dependency-free arithmetic explainability test. */
public final class CraftExplainerSelfTest {
    private CraftExplainerSelfTest() {
    }

    public static void main(String[] args) {
        Provenance source = source();
        ExperimentalProperty property = new ExperimentalProperty(
                "quality", "expQuality", 0.0d, 100.0d, 2, false,
                CombineType.RESOURCE,
                Collections.singletonList(
                        new PropertyWeight(ResourceStat.OQ, 1, 1.0d)),
                source);
        SchematicDefinition schematic = new SchematicDefinition(
                "fixture", "Fixture",
                "object/draft_schematic/fixture.iff",
                "object/tangible/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly", "experiment",
                Arrays.asList(
                        new IngredientSlotDefinition(
                                0, "A", SlotKind.RESOURCE, "metal", 10, 0.0d, source),
                        new IngredientSlotDefinition(
                                1, "B", SlotKind.RESOURCE, "metal", 30, 0.0d, source)),
                Collections.singletonList(property),
                "generic",
                source);

        CraftScenario scenario = new CraftScenario(
                schematic,
                Arrays.asList(
                        new ResourceSlotAssignment(
                                0, resource("A", 1000, source)),
                        new ResourceSlotAssignment(
                                1, resource("B", 500, source))),
                Collections.emptyList(),
                CraftOutcomeTier.GREAT,
                Collections.emptyList());

        CraftResult result = new InfinityCraftEngine().execute(scenario);
        AttributeExplanation explanation =
                new CraftExplainer().explain(scenario, result).get("quality");

        assertClose(625.0d, explanation.getWeightedScore(), 0.000001d, "score");
        WeightedStatExplanation stat = explanation.getWeightedStats().get(0);
        assertClose(625.0d, stat.getWeightedStatValue(), 0.000001d, "weighted stat");
        assertClose(
                250.0d,
                stat.getInputs().get(0).getWeightedStatContribution(),
                0.000001d,
                "slot A contribution");
        assertClose(
                375.0d,
                stat.getInputs().get(1).getWeightedStatContribution(),
                0.000001d,
                "slot B contribution");
        System.out.println("CraftExplainerSelfTest PASS");
    }

    private static ResourceInput resource(
            String name, int oq, Provenance unused) {
        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.OQ, Integer.valueOf(oq));
        return new ResourceInput(
                name, "metal",
                new HashSet<String>(Arrays.asList("metal")),
                stats, 1000L, ResourceOrigin.MANUAL);
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
