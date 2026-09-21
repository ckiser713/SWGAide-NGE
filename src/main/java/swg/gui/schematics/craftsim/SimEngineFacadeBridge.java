package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.simulator.components.ComponentInstance;
import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.components.ComponentUse;
import swg.crafting.simulator.components.ExactComponentInput;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;

/**
 * Package-private component bridge for the native Crafting Simulator tab.
 */
final class SimEngineFacadeBridge {
    private SimEngineFacadeBridge() {
    }

    /**
     * Builds one exact owned/manual component assignment for the explicitly
     * selected component slot.
     */
    static List<ComponentSlotAssignment> componentsForExact(
            ExactComponentInput input,
            int recursionDepth,
            SchematicDefinition bound) {
        if (input == null || bound == null) {
            return Collections.<ComponentSlotAssignment>emptyList();
        }
        if (recursionDepth < 1 || recursionDepth > 64) {
            throw new IllegalArgumentException(
                    "recursionDepth must be within [1,64]");
        }

        IngredientSlotDefinition target = null;
        for (IngredientSlotDefinition slot : bound.getSlots()) {
            if (slot.getIndex() == input.getSlotIndex()) {
                target = slot;
                break;
            }
        }
        if (target == null || target.getKind() == null
                || !target.getKind().isComponent()) {
            throw new IllegalArgumentException(
                    "slot " + input.getSlotIndex()
                    + " is not a component slot");
        }
        if (input.getAvailableUses() < target.getQuantity()) {
            throw new IllegalArgumentException(
                    "component has " + input.getAvailableUses()
                    + " uses but slot requires " + target.getQuantity());
        }

        ComponentInstance component = new ComponentInstance(
                input.getComponentId(),
                target.getAcceptedType(),
                input.getSerial(),
                input.getAvailableUses(),
                ComponentOrigin.OWNED_LOOT,
                input.getProperties());

        ComponentUse use =
                new ComponentUse(component, target.getQuantity());
        return Collections.singletonList(
                new ComponentSlotAssignment(
                        target.getIndex(),
                        Collections.singletonList(use)));
    }

    /**
     * Legacy helper retained for source compatibility with earlier tests.
     * New production UI code must use {@link #componentsForExact}.
     */
    static List<ComponentSlotAssignment> componentsFor(
            String exoticName,
            int recursionDepth,
            SchematicDefinition bound) {
        if (exoticName == null || exoticName.trim().isEmpty()
                || bound == null) {
            return Collections.<ComponentSlotAssignment>emptyList();
        }

        for (IngredientSlotDefinition slot : bound.getSlots()) {
            if (slot.getKind() != null && slot.getKind().isComponent()) {
                ExactComponentInput input = new ExactComponentInput(
                        slot.getIndex(),
                        exoticName.trim(),
                        "",
                        Math.max(1, slot.getQuantity()),
                        Collections.<ComponentProperty>emptyList());
                return componentsForExact(input, recursionDepth, bound);
            }
        }
        return Collections.<ComponentSlotAssignment>emptyList();
    }
}
