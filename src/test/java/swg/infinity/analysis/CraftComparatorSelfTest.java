package swg.infinity.analysis;
import swg.crafting.simulator.compare.ResultDelta;
import swg.crafting.simulator.compare.CraftComparison;
import swg.crafting.simulator.compare.CraftComparator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.LaboratoryType;
import swg.crafting.simulator.contracts.Provenance;
import swg.infinity.contracts.SchematicDefinition;
import swg.crafting.simulator.scenario.CraftResult;
import swg.infinity.engine.CraftState;
import swg.infinity.processor.FunctionalItemResult;

/** Dependency-free comparison test. */
public final class CraftComparatorSelfTest {
    private CraftComparatorSelfTest() {
    }

    public static void main(String[] args) {
        CraftComparison comparison = new CraftComparator().compare(
                result(500.0d),
                result(512.0d));

        ResultDelta damage = comparison.get("maxdamage");
        if (damage == null || Math.abs(damage.getDelta().doubleValue() - 12.0d) > 0.000001d) {
            throw new AssertionError("final-item delta incorrect");
        }
        System.out.println("CraftComparatorSelfTest PASS");
    }

    private static CraftResult result(double maxDamage) {
        SchematicDefinition schematic = new SchematicDefinition(
                "fixture", "Fixture",
                "object/draft_schematic/fixture.iff",
                "object/tangible/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly", "experiment",
                Collections.emptyList(),
                Collections.emptyList(),
                "weapon",
                new Provenance(
                        "swginfinity/public",
                        "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                        "fixture"));
        CraftState state = new CraftState(
                schematic,
                EvidenceState.EXACT_WITH_FORCED_OUTCOME,
                Collections.emptyMap(),
                Collections.<String>emptyList());
        Map<String, Double> values = new LinkedHashMap<String, Double>();
        values.put("maxdamage", Double.valueOf(maxDamage));
        FunctionalItemResult functional = new FunctionalItemResult(
                "weapon",
                EvidenceState.EXACT_WITH_FORCED_OUTCOME,
                values,
                Collections.<String>emptyList());
        return new CraftResult(state, functional);
    }
}
