package swg.infinity.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.PropertyWeight;
import swg.crafting.simulator.scenario.AttributeState;
import swg.infinity.engine.CraftState;

/**
 * Generic component application translated from the pinned Infinity
 * ResourceLabratory::applyComponentStats path.
 *
 * <p>Specialized object processors (armor, clothing, ships, etc.) remain
 * separate coverage concerns.</p>
 */
public final class ComponentCombiner {

    public CraftState apply(
            CraftState state,
            List<ComponentSlotAssignment> assignments) {
        if (state == null) throw new NullPointerException("state");
        if (assignments == null) throw new NullPointerException("assignments");

        List<ComponentSlotAssignment> ordered =
                new ArrayList<ComponentSlotAssignment>(assignments);
        Collections.sort(ordered, new Comparator<ComponentSlotAssignment>() {
            @Override
            public int compare(ComponentSlotAssignment left, ComponentSlotAssignment right) {
                return left.getSlotIndex() - right.getSlotIndex();
            }
        });

        Map<Integer, IngredientSlotDefinition> slots =
                new LinkedHashMap<Integer, IngredientSlotDefinition>();
        for (IngredientSlotDefinition slot : state.getSchematic().getSlots()) {
            slots.put(Integer.valueOf(slot.getIndex()), slot);
        }

        Map<Integer, ComponentSlotAssignment> bySlot =
                new HashMap<Integer, ComponentSlotAssignment>();
        Map<String, Integer> consumedByComponent =
                new HashMap<String, Integer>();

        for (ComponentSlotAssignment assignment : ordered) {
            Integer key = Integer.valueOf(assignment.getSlotIndex());
            if (bySlot.put(key, assignment) != null) {
                throw new IllegalArgumentException(
                        "Duplicate component assignment for slot " + key);
            }
            for (ComponentUse use : assignment.getComponentUses()) {
                String componentId = use.getComponent().getId();
                Integer prior = consumedByComponent.get(componentId);
                int consumed = (prior == null ? 0 : prior.intValue()) + use.getUses();
                if (consumed > use.getComponent().getUses()) {
                    throw new IllegalArgumentException(
                            "Component " + componentId
                            + " over-consumed: " + consumed
                            + " > " + use.getComponent().getUses());
                }
                consumedByComponent.put(componentId, Integer.valueOf(consumed));
            }
        }

        for (IngredientSlotDefinition slot : state.getSchematic().getSlots()) {
            if (!slot.getKind().isComponent() || slot.getKind().isOptional()) continue;
            if (!bySlot.containsKey(Integer.valueOf(slot.getIndex()))) {
                throw new IllegalArgumentException(
                        "Missing required component assignment for slot "
                        + slot.getIndex());
            }
        }

        Map<String, AttributeState> attributes =
                new LinkedHashMap<String, AttributeState>(state.getAttributes());
        List<String> warnings = new ArrayList<String>(state.getWarnings());
        boolean modified = false;

        for (ComponentSlotAssignment assignment : ordered) {
            IngredientSlotDefinition slot =
                    slots.get(Integer.valueOf(assignment.getSlotIndex()));
            if (slot == null || !slot.getKind().isComponent()) {
                throw new IllegalArgumentException(
                        "No component slot at index " + assignment.getSlotIndex());
            }

            int totalUses = assignment.totalUses();
            if (totalUses != slot.getQuantity()) {
                if (slot.getKind().isOptional() && totalUses < slot.getQuantity()) {
                    warnings.add(
                            "Optional component slot " + slot.getIndex()
                            + " is not full; Infinity generic component stats are not applied.");
                    continue;
                }
                throw new IllegalArgumentException(
                        "Component slot " + slot.getIndex()
                        + " requires exactly " + slot.getQuantity()
                        + " uses, got " + totalUses);
            }

            validateIdentity(slot, assignment);
            if (assignment.getComponentUses().isEmpty()) continue;

            // Infinity ComponentSlot::getPrototype() returns contents[0].
            ComponentInstance prototype =
                    assignment.getComponentUses().get(0).getComponent();

            // CustomIngredient participates in SharedLabratory weighting but is
            // not processed by ResourceLabratory::applyComponentStats as a
            // normal Component.
            if (prototype.isCustomResourceIngredient()) continue;

            for (ComponentProperty componentProperty : prototype.getProperties()) {
                modified = true;
                AttributeState current =
                        attributes.get(componentProperty.getAttribute());

                if (current == null) {
                    ExperimentalProperty synthetic = new ExperimentalProperty(
                            componentProperty.getAttribute(),
                            componentProperty.getGroup(),
                            componentProperty.getValue(),
                            componentProperty.getValue(),
                            componentProperty.getPrecision(),
                            componentProperty.isHidden(),
                            CombineType.LINEAR,
                            Collections.<PropertyWeight>emptyList(),
                            componentProperty.getProvenance());
                    attributes.put(
                            componentProperty.getAttribute(),
                            new AttributeState(
                                    synthetic, 0.0d, 0.0d, 0.0d,
                                    componentProperty.getValue()));
                    continue;
                }

                double propertyValue =
                        componentProperty.getValue() * slot.getContribution();
                CombineType combine = current.getProperty().getCombineType();

                switch (combine) {
                case LINEAR:
                    current = current.withRange(
                            current.getMinValue() + propertyValue,
                            current.getMaxValue() + propertyValue);
                    current = current.withCurrentValue(
                            current.getCurrentValue() + propertyValue);
                    attributes.put(componentProperty.getAttribute(), current);
                    break;
                case PERCENTAGE:
                    double percentageValue =
                            current.getCurrentValue() + propertyValue;
                    current = current.withRange(
                            current.getMinValue() + propertyValue,
                            current.getMaxValue() + propertyValue);
                    current = current.withPercentageOnly(percentageValue);
                    attributes.put(componentProperty.getAttribute(), current);
                    break;
                case BITSET:
                    int bitset = ((int) current.getCurrentValue())
                            | ((int) propertyValue);
                    attributes.put(
                            componentProperty.getAttribute(),
                            current.withCurrentValue(bitset));
                    break;
                case OVERRIDE:
                    break;
                case LIMITED:
                    double limited =
                            current.getCurrentValue() + propertyValue;
                    if (limited < current.getMinValue()) {
                        limited = current.getMinValue();
                    }
                    if (limited > current.getMaxValue()) {
                        limited = current.getMaxValue();
                    }
                    attributes.put(
                            componentProperty.getAttribute(),
                            current.withCurrentValue(limited));
                    modified = false;
                    break;
                case RESOURCE:
                default:
                    break;
                }
            }
        }

        if (modified) {
            Map<String, AttributeState> recalculated =
                    new LinkedHashMap<String, AttributeState>();
            for (Map.Entry<String, AttributeState> entry : attributes.entrySet()) {
                recalculated.put(entry.getKey(), entry.getValue().recalculate());
            }
            attributes = recalculated;
        }

        return state.withCalculationData(attributes, warnings);
    }

    private void validateIdentity(
            IngredientSlotDefinition slot,
            ComponentSlotAssignment assignment) {
        if (!slot.getKind().requiresIdenticalComponents()) return;
        if (assignment.getComponentUses().size() < 2) return;

        String serial =
                assignment.getComponentUses().get(0).getComponent().getSerial();
        for (int i = 1; i < assignment.getComponentUses().size(); ++i) {
            String candidate =
                    assignment.getComponentUses().get(i).getComponent().getSerial();
            if (!serial.equals(candidate)) {
                throw new IllegalArgumentException(
                        "Component serial mismatch in identical slot "
                        + slot.getIndex());
            }
        }
    }
}
