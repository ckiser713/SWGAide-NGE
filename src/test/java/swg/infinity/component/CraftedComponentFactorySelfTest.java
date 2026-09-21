package swg.infinity.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import swg.infinity.contracts.CombineType;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.LaboratoryType;
import swg.crafting.simulator.contracts.Provenance;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.engine.AttributeState;
import swg.infinity.engine.CraftState;

/** Dependency-free conversion test for recursive crafted components. */
public final class CraftedComponentFactorySelfTest {
    private CraftedComponentFactorySelfTest() {
    }

    public static void main(String[] args) {
        Provenance source = source();
        ExperimentalProperty useCount = property(
                "useCount", "", 5.0d, source);
        ExperimentalProperty maxDamage = property(
                "maxdamage", "expDamage", 171.0d, source);

        Map<String, AttributeState> attributes =
                new LinkedHashMap<String, AttributeState>();
        attributes.put("useCount",
                new AttributeState(useCount, 0.0d, 0.0d, 0.0d, 5.0d));
        attributes.put("maxdamage",
                new AttributeState(maxDamage, 0.0d, 0.0d, 0.0d, 171.0d));

        SchematicDefinition schematic = new SchematicDefinition(
                "component-fixture",
                "Component Fixture",
                "object/draft_schematic/component/fixture.iff",
                "object/tangible/component/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly",
                "experiment",
                Collections.emptyList(),
                Arrays.asList(useCount, maxDamage),
                "generic",
                source);

        ComponentInstance component = new CraftedComponentFactory().create(
                "owned-component-1",
                schematic.getTargetTemplate(),
                "(SERIAL)",
                new CraftState(
                        schematic,
                        EvidenceState.EXACT_WITH_FORCED_OUTCOME,
                        attributes,
                        Collections.<String>emptyList()));

        if (component.getUses() != 5) {
            throw new AssertionError("useCount not converted");
        }
        if (component.getProperties().size() != 1) {
            throw new AssertionError("useCount leaked into component properties");
        }
        if (!"maxdamage".equals(component.getProperties().get(0).getAttribute())) {
            throw new AssertionError("component property order/value not preserved");
        }
        System.out.println("CraftedComponentFactorySelfTest PASS");
    }

    private static ExperimentalProperty property(
            String name, String group, double value, Provenance source) {
        return new ExperimentalProperty(
                name, group, value, value, 0, false,
                CombineType.LINEAR, Collections.emptyList(), source);
    }

    private static Provenance source() {
        return new Provenance(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "fixture");
    }
}
