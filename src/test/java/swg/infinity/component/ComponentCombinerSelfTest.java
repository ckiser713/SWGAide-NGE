package swg.infinity.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.EvidenceState;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.Provenance;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.infinity.engine.AttributeState;
import swg.infinity.engine.CraftState;

/** Dependency-free tests for source-observed generic component behavior. */
public final class ComponentCombinerSelfTest {
    private ComponentCombinerSelfTest() {
    }

    public static void main(String[] args) {
        shouldApplyLinearContribution();
        shouldUseFirstPrototypeForMixedSlot();
        shouldAcceptMultiUseComponentWithoutSerial();
        shouldRejectSerialMismatchForIdenticalSlot();
        shouldInjectPreviouslyUnknownComponentProperty();
        shouldIgnorePartialOptionalComponentStats();
        shouldRejectMissingRequiredComponent();
        System.out.println("ComponentCombinerSelfTest PASS");
    }

    private static void shouldApplyLinearContribution() {
        CraftState initial = state(
                SlotKind.IDENTICAL_COMPONENT,
                1,
                0.50d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance component = component(
                "c1", "(SERIAL)", 1,
                new ComponentProperty(
                        "maxdamage", 100.0d, 0, "expDamage", false, source()));

        CraftState result = apply(initial,
                new ComponentSlotAssignment(
                        0,
                        Collections.singletonList(new ComponentUse(component, 1))));

        assertClose(60.0d, result.getAttribute("maxdamage").getCurrentValue(),
                0.000001d, "linear current");
        assertClose(51.0d, result.getAttribute("maxdamage").getMinValue(),
                0.000001d, "linear min");
        assertClose(70.0d, result.getAttribute("maxdamage").getMaxValue(),
                0.000001d, "linear max");
    }

    private static void shouldUseFirstPrototypeForMixedSlot() {
        CraftState initial = state(
                SlotKind.MIXED_COMPONENT,
                2,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance first = component(
                "first", "", 1,
                new ComponentProperty(
                        "maxdamage", 10.0d, 0, "expDamage", false, source()));
        ComponentInstance second = component(
                "second", "", 1,
                new ComponentProperty(
                        "maxdamage", 999.0d, 0, "expDamage", false, source()));

        CraftState result = apply(initial,
                new ComponentSlotAssignment(
                        0,
                        Arrays.asList(
                                new ComponentUse(first, 1),
                                new ComponentUse(second, 1))));

        assertClose(20.0d, result.getAttribute("maxdamage").getCurrentValue(),
                0.000001d, "mixed first prototype");
    }

    private static void shouldAcceptMultiUseComponentWithoutSerial() {
        CraftState initial = state(
                SlotKind.IDENTICAL_COMPONENT,
                3,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance component = component(
                "multi", "", 5,
                new ComponentProperty(
                        "maxdamage", 5.0d, 0, "expDamage", false, source()));

        apply(initial,
                new ComponentSlotAssignment(
                        0,
                        Collections.singletonList(new ComponentUse(component, 3))));
    }

    private static void shouldRejectSerialMismatchForIdenticalSlot() {
        CraftState initial = state(
                SlotKind.IDENTICAL_COMPONENT,
                2,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance one = component(
                "one", "(A)", 1,
                new ComponentProperty(
                        "maxdamage", 5.0d, 0, "expDamage", false, source()));
        ComponentInstance two = component(
                "two", "(B)", 1,
                new ComponentProperty(
                        "maxdamage", 5.0d, 0, "expDamage", false, source()));

        boolean rejected = false;
        try {
            apply(initial,
                    new ComponentSlotAssignment(
                            0,
                            Arrays.asList(
                                    new ComponentUse(one, 1),
                                    new ComponentUse(two, 1))));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("serial mismatch accepted");
    }

    private static void shouldInjectPreviouslyUnknownComponentProperty() {
        CraftState initial = state(
                SlotKind.IDENTICAL_COMPONENT,
                1,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance component = component(
                "c1", "(SERIAL)", 1,
                new ComponentProperty(
                        "specialbonus", 7.5d, 1, "bonus", false, source()));

        CraftState result = apply(initial,
                new ComponentSlotAssignment(
                        0,
                        Collections.singletonList(new ComponentUse(component, 1))));

        assertClose(7.5d, result.getAttribute("specialbonus").getCurrentValue(),
                0.000001d, "injected property");
    }

    private static void shouldIgnorePartialOptionalComponentStats() {
        CraftState initial = state(
                SlotKind.OPTIONAL_MIXED_COMPONENT,
                2,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        ComponentInstance one = component(
                "one", "", 1,
                new ComponentProperty(
                        "maxdamage", 100.0d, 0, "expDamage", false, source()));

        CraftState result = apply(initial,
                new ComponentSlotAssignment(
                        0,
                        Collections.singletonList(new ComponentUse(one, 1))));

        assertClose(10.0d, result.getAttribute("maxdamage").getCurrentValue(),
                0.000001d, "partial optional unchanged");
        if (result.getWarnings().isEmpty()) {
            throw new AssertionError("partial optional slot warning missing");
        }
    }

    private static void shouldRejectMissingRequiredComponent() {
        CraftState initial = state(
                SlotKind.IDENTICAL_COMPONENT,
                1,
                1.0d,
                property("maxdamage", CombineType.LINEAR, 1.0d, 20.0d),
                10.0d);
        boolean rejected = false;
        try {
            new ComponentCombiner().apply(
                    initial, Collections.<ComponentSlotAssignment>emptyList());
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError("missing required component accepted");
        }
    }

    private static CraftState apply(
            CraftState state, ComponentSlotAssignment assignment) {
        return new ComponentCombiner().apply(
                state, Collections.singletonList(assignment));
    }

    private static CraftState state(
            SlotKind kind,
            int quantity,
            double contribution,
            ExperimentalProperty property,
            double currentValue) {
        IngredientSlotDefinition slot = new IngredientSlotDefinition(
                0, "Component", kind, "component", quantity,
                contribution, source());
        SchematicDefinition schematic = new SchematicDefinition(
                "fixture",
                "Fixture",
                "object/draft_schematic/fixture.iff",
                "object/tangible/fixture.iff",
                LaboratoryType.RESOURCE,
                "assembly",
                "experimentation",
                Collections.singletonList(slot),
                Collections.singletonList(property),
                "generic",
                source());
        Map<String, AttributeState> attributes =
                new LinkedHashMap<String, AttributeState>();
        attributes.put(
                property.getAttribute(),
                new AttributeState(
                        property, 1000.0d, 1.0d, 0.5d, currentValue));
        return new CraftState(
                schematic,
                EvidenceState.EXACT_WITH_FORCED_OUTCOME,
                attributes,
                Collections.<String>emptyList());
    }

    private static ExperimentalProperty property(
            String name, CombineType combine, double min, double max) {
        return new ExperimentalProperty(
                name, "expDamage", min, max, 2, false, combine,
                Collections.emptyList(), source());
    }

    private static ComponentInstance component(
            String id,
            String serial,
            int uses,
            ComponentProperty property) {
        return new ComponentInstance(
                id,
                "object/tangible/component/fixture.iff",
                serial,
                uses,
                ComponentOrigin.MANUAL,
                Collections.singletonList(property));
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
