package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.component.ComponentInstance;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SlotKind;
import swg.crafting.simulator.scenario.AttributeState;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceLaboratory;
import swg.infinity.engine.ResourceSlotAssignment;

/**
 * Builds an auditable arithmetic explanation from scenario inputs and the final
 * CraftingValues state. No UI formatting is embedded here.
 */
public final class CraftExplainer {
    private final ResourceLaboratory resourceLaboratory;

    public CraftExplainer() {
        this(new ResourceLaboratory());
    }

    public CraftExplainer(ResourceLaboratory resourceLaboratory) {
        if (resourceLaboratory == null) throw new NullPointerException("resourceLaboratory");
        this.resourceLaboratory = resourceLaboratory;
    }

    public CraftExplanation explain(
            CraftScenario scenario,
            CraftResult result) {
        if (scenario == null) throw new NullPointerException("scenario");
        if (result == null) throw new NullPointerException("result");
        if (!scenario.getSchematic().getId().equals(
                result.getCraftState().getSchematic().getId())) {
            throw new IllegalArgumentException(
                    "Scenario/result schematic mismatch");
        }

        Map<Integer, ResourceInput> resources = new HashMap<Integer, ResourceInput>();
        for (ResourceSlotAssignment assignment : scenario.getResources()) {
            resources.put(Integer.valueOf(assignment.getSlotIndex()),
                    assignment.getResource());
        }
        Map<Integer, ComponentSlotAssignment> components =
                new HashMap<Integer, ComponentSlotAssignment>();
        for (ComponentSlotAssignment assignment : scenario.getComponents()) {
            components.put(Integer.valueOf(assignment.getSlotIndex()), assignment);
        }

        List<AttributeExplanation> explained =
                new ArrayList<AttributeExplanation>();
        for (Map.Entry<String, AttributeState> entry :
                result.getCraftState().getAttributes().entrySet()) {
            AttributeState state = entry.getValue();
            ExperimentalProperty property = state.getProperty();
            List<WeightedStatExplanation> stats =
                    new ArrayList<WeightedStatExplanation>();

            for (PropertyWeight weight : property.getWeights()) {
                double weightedStat = resourceLaboratory.calculateWeightedValue(
                        scenario.getSchematic(),
                        scenario.getResources(),
                        scenario.getComponents(),
                        weight.getStat());
                stats.add(new WeightedStatExplanation(
                        weight.getStat(),
                        weight.getNormalizedWeight(),
                        weightedStat,
                        weightedStat * weight.getNormalizedWeight(),
                        explainInputs(
                                scenario,
                                resources,
                                components,
                                weight.getStat(),
                                weight.getNormalizedWeight())));
            }

            explained.add(new AttributeExplanation(
                    entry.getKey(),
                    property.getMinValue(),
                    property.getMaxValue(),
                    state.getMinValue(),
                    state.getMaxValue(),
                    state.getWeightedScore(),
                    state.getMaxPercentage(),
                    state.getCurrentPercentage(),
                    state.getCurrentValue(),
                    stats));
        }
        return new CraftExplanation(explained);
    }

    private List<InputContribution> explainInputs(
            CraftScenario scenario,
            Map<Integer, ResourceInput> resources,
            Map<Integer, ComponentSlotAssignment> components,
            ResourceStat stat,
            double propertyWeight) {
        long denominator = 0L;

        for (IngredientSlotDefinition slot : scenario.getSchematic().getSlots()) {
            int value = statValue(slot, resources, components, stat);
            if (value != 0) denominator += slot.getQuantity();
        }

        List<InputContribution> contributions =
                new ArrayList<InputContribution>();
        if (denominator == 0L) return contributions;

        for (IngredientSlotDefinition slot : scenario.getSchematic().getSlots()) {
            int value = statValue(slot, resources, components, stat);
            if (value == 0) continue;

            double weightedStatContribution =
                    ((double) value * (double) slot.getQuantity())
                    / (double) denominator;
            contributions.add(new InputContribution(
                    slot.getIndex(),
                    inputId(slot, resources, components),
                    value,
                    slot.getQuantity(),
                    weightedStatContribution,
                    weightedStatContribution * propertyWeight));
        }
        return contributions;
    }

    private int statValue(
            IngredientSlotDefinition slot,
            Map<Integer, ResourceInput> resources,
            Map<Integer, ComponentSlotAssignment> components,
            ResourceStat stat) {
        if (slot.getKind() == SlotKind.RESOURCE) {
            ResourceInput resource = resources.get(Integer.valueOf(slot.getIndex()));
            return resource == null ? 0 : resource.getStat(stat);
        }

        ComponentSlotAssignment assignment =
                components.get(Integer.valueOf(slot.getIndex()));
        if (assignment == null || assignment.getComponentUses().isEmpty()) {
            return 0;
        }
        ComponentInstance prototype =
                assignment.getComponentUses().get(0).getComponent();
        return prototype.isCustomResourceIngredient()
                ? prototype.getResourceStat(stat)
                : 0;
    }

    private String inputId(
            IngredientSlotDefinition slot,
            Map<Integer, ResourceInput> resources,
            Map<Integer, ComponentSlotAssignment> components) {
        if (slot.getKind() == SlotKind.RESOURCE) {
            ResourceInput resource = resources.get(Integer.valueOf(slot.getIndex()));
            return resource == null ? "<missing>" : resource.getName();
        }
        ComponentSlotAssignment assignment =
                components.get(Integer.valueOf(slot.getIndex()));
        if (assignment == null || assignment.getComponentUses().isEmpty()) {
            return "<missing>";
        }
        return assignment.getComponentUses().get(0).getComponent().getId();
    }
}
