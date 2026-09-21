package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.simulator.components.ComponentInstance;
import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.components.ComponentUse;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/**
 * Internal helper used by the Crafting Simulator tab to derive
 * {@link ComponentSlotAssignment} entries from the user-supplied
 * exotic component name and recursion depth. Lives in the same
 * package as {@link SWGCraftingSimulatorTab} so it can remain
 * package-private.
 */
final class SimEngineFacadeBridge {
    private SimEngineFacadeBridge() {
    }

    static List<ComponentSlotAssignment> componentsFor(
            String exoticName,
            int recursionDepth,
            SchematicDefinition bound) {
        if (exoticName == null || exoticName.isEmpty() || bound == null) {
            return Collections.<ComponentSlotAssignment>emptyList();
        }
        List<ComponentSlotAssignment> out =
                new ArrayList<ComponentSlotAssignment>();
        for (IngredientSlotDefinition slot : bound.getSlots()) {
            if (slot == null
                    || slot.getKind() == null
                    || !slot.getKind().isComponent()) {
                continue;
            }
            // Build one synthetic exact/manual component per recursion
            // depth. The serial is empty; the origin marks it MANUAL.
            List<ComponentUse> uses = new ArrayList<ComponentUse>();
            int n = Math.max(1, recursionDepth);
            for (int i = 0; i < n; ++i) {
                ComponentInstance ci = new ComponentInstance(
                        exoticName + "-" + slot.getIndex() + "-r" + i,
                        exoticName,
                        "",
                        1,
                        ComponentOrigin.MANUAL,
                        Collections.<ComponentProperty>emptyList());
                uses.add(new ComponentUse(ci, 1));
            }
            out.add(new ComponentSlotAssignment(
                    slot.getIndex(),
                    Collections.unmodifiableList(uses)));
        }
        // Optional: collapse to one assignment per slot to keep the
        // engine call deterministic; if the slot is component-kind but
        // we have no exotic name we leave it empty.
        return out.isEmpty()
                ? Collections.<ComponentSlotAssignment>emptyList()
                : Collections.unmodifiableList(out);
    }
}
