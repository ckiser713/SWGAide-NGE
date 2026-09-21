package swg.infinity.planning;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.component.ComponentUse;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SlotKind;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceSlotAssignment;

/**
 * Expands one already-resolved craft scenario into material consumption.
 *
 * <p>Nested component scenarios should first be recursively resolved by the
 * caller; this planner intentionally does not invent component recipes.</p>
 */
public final class MaterialPlanner {

    public MaterialPlan plan(CraftScenario scenario, int craftCount) {
        if (scenario == null) throw new NullPointerException("scenario");
        if (craftCount < 1) throw new IllegalArgumentException("craftCount must be >= 1");

        Map<Integer, IngredientSlotDefinition> slots =
                new HashMap<Integer, IngredientSlotDefinition>();
        for (IngredientSlotDefinition slot : scenario.getSchematic().getSlots()) {
            slots.put(Integer.valueOf(slot.getIndex()), slot);
        }

        Map<String, Long> resources = new LinkedHashMap<String, Long>();
        for (ResourceSlotAssignment assignment : scenario.getResources()) {
            IngredientSlotDefinition slot =
                    slots.get(Integer.valueOf(assignment.getSlotIndex()));
            if (slot == null || slot.getKind() != SlotKind.RESOURCE) {
                throw new IllegalArgumentException(
                        "Invalid resource slot " + assignment.getSlotIndex());
            }
            long quantity = (long) slot.getQuantity() * (long) craftCount;
            add(resources, assignment.getResource().getName(), quantity);
        }

        Map<String, Long> components = new LinkedHashMap<String, Long>();
        for (ComponentSlotAssignment assignment : scenario.getComponents()) {
            IngredientSlotDefinition slot =
                    slots.get(Integer.valueOf(assignment.getSlotIndex()));
            if (slot == null || !slot.getKind().isComponent()) {
                throw new IllegalArgumentException(
                        "Invalid component slot " + assignment.getSlotIndex());
            }
            for (ComponentUse use : assignment.getComponentUses()) {
                long quantity = (long) use.getUses() * (long) craftCount;
                add(components, use.getComponent().getId(), quantity);
            }
        }

        return new MaterialPlan(craftCount, resources, components);
    }

    /**
     * Calculates the maximum count possible from quantities captured in the
     * scenario. A return of Long.MAX_VALUE means every selected input has
     * unknown/unbounded quantity and therefore no finite bottleneck is known.
     */
    public long maximumCraftCount(CraftScenario scenario) {
        if (scenario == null) throw new NullPointerException("scenario");

        Map<Integer, IngredientSlotDefinition> slots =
                new HashMap<Integer, IngredientSlotDefinition>();
        for (IngredientSlotDefinition slot : scenario.getSchematic().getSlots()) {
            slots.put(Integer.valueOf(slot.getIndex()), slot);
        }

        long maximum = Long.MAX_VALUE;
        for (ResourceSlotAssignment assignment : scenario.getResources()) {
            IngredientSlotDefinition slot =
                    slots.get(Integer.valueOf(assignment.getSlotIndex()));
            if (slot == null || slot.getKind() != SlotKind.RESOURCE) {
                throw new IllegalArgumentException(
                        "Invalid resource slot " + assignment.getSlotIndex());
            }
            ResourceInput resource = assignment.getResource();
            if (resource.getAvailableQuantity() >= 0L) {
                maximum = Math.min(
                        maximum,
                        resource.getAvailableQuantity() / slot.getQuantity());
            }
        }

        Map<String, Long> availableByComponent = new HashMap<String, Long>();
        Map<String, Long> usesPerCraft = new HashMap<String, Long>();
        for (ComponentSlotAssignment assignment : scenario.getComponents()) {
            for (ComponentUse use : assignment.getComponentUses()) {
                String id = use.getComponent().getId();
                availableByComponent.put(
                        id, Long.valueOf(use.getComponent().getUses()));
                Long prior = usesPerCraft.get(id);
                usesPerCraft.put(
                        id,
                        Long.valueOf((prior == null ? 0L : prior.longValue())
                                + use.getUses()));
            }
        }

        for (Map.Entry<String, Long> entry : usesPerCraft.entrySet()) {
            long available = availableByComponent.get(entry.getKey()).longValue();
            long perCraft = entry.getValue().longValue();
            maximum = Math.min(maximum, available / perCraft);
        }

        return maximum;
    }

    private void add(Map<String, Long> target, String key, long quantity) {
        Long prior = target.get(key);
        target.put(
                key,
                Long.valueOf((prior == null ? 0L : prior.longValue()) + quantity));
    }
}
