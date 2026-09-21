package swg.crafting.simulator.server.infinity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.simulator.resources.ResourceRequirement;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/** Maps Infinity raw-resource slots into provider-neutral requirements. */
public final class InfinityResourceRequirements {
    private InfinityResourceRequirements() {
        throw new AssertionError("Do not instantiate");
    }

    public static List<ResourceRequirement> from(
            SchematicDefinition definition) {
        if (definition == null) throw new NullPointerException("definition");
        List<ResourceRequirement> out =
                new ArrayList<ResourceRequirement>();
        for (IngredientSlotDefinition slot : definition.getSlots()) {
            if (slot.getKind() == SlotKind.RESOURCE) {
                out.add(new ResourceRequirement(
                        slot.getIndex(),
                        slot.getTitle(),
                        slot.getAcceptedType(),
                        slot.getQuantity()));
            }
        }
        return Collections.unmodifiableList(out);
    }
}
