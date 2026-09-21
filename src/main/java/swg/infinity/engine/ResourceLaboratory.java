package swg.infinity.engine;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.AttributeState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.component.ComponentInstance;
import swg.infinity.component.ComponentSlotAssignment;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/**
 * Deterministic implementation of the pinned Infinity generic Resource Laboratory.
 *
 * <p>This class intentionally excludes RNG. Assembly and experimentation result
 * tiers are forced by the caller so arithmetic can be source-parity tested
 * independently from Engine3 random-number semantics.</p>
 */
public final class ResourceLaboratory {

    public CraftState initialize(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> assignments,
            CraftOutcomeTier assemblyTier) {
        return initialize(
                schematic,
                assignments,
                new ArrayList<ComponentSlotAssignment>(),
                assemblyTier);
    }

    public CraftState initialize(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> assignments,
            List<ComponentSlotAssignment> componentAssignments,
            CraftOutcomeTier assemblyTier) {
        requireResourceLaboratory(schematic);
        if (assignments == null) throw new NullPointerException("assignments");
        if (componentAssignments == null) {
            throw new NullPointerException("componentAssignments");
        }
        if (assemblyTier == null) throw new NullPointerException("assemblyTier");

        Map<Integer, ResourceInput> resources = indexAssignments(schematic, assignments);
        Map<Integer, ComponentSlotAssignment> components =
                indexComponentAssignments(componentAssignments);
        validateRequiredResourceSlots(schematic, resources);

        Map<String, AttributeState> attributes =
                new LinkedHashMap<String, AttributeState>();
        List<String> warnings = new ArrayList<String>();

        for (ExperimentalProperty property : schematic.getProperties()) {
            double weightedScore = calculatePropertyScore(
                    schematic, resources, components, property);
            double maxPercentage = weightedScore > 0.0d
                    ? weightedScore / 1000.0d
                    : 0.0d;
            double currentPercentage = weightedScore > 0.0d
                    ? assemblyPercentage(weightedScore)
                            * assemblyTier.getAssemblyModifier()
                    : 0.0d;

            currentPercentage = Math.max(
                    0.0d, Math.min(maxPercentage, currentPercentage));

            double currentValue = interpolate(property, currentPercentage);
            attributes.put(property.getAttribute(), new AttributeState(
                    property,
                    weightedScore,
                    maxPercentage,
                    currentPercentage,
                    currentValue));
        }

        return new CraftState(
                schematic,
                EvidenceState.EXACT_WITH_FORCED_OUTCOME,
                attributes,
                warnings);
    }

    public CraftState experiment(
            CraftState state,
            String experimentalGroup,
            int pointsAttempted,
            CraftOutcomeTier outcome) {
        if (state == null) throw new NullPointerException("state");
        if (experimentalGroup == null) throw new NullPointerException("experimentalGroup");
        if (outcome == null) throw new NullPointerException("outcome");
        if (pointsAttempted < 1) {
            throw new IllegalArgumentException("pointsAttempted must be >= 1");
        }

        double delta = outcome.getExperimentModifierPerPoint() * pointsAttempted;
        Map<String, AttributeState> changed =
                new LinkedHashMap<String, AttributeState>(state.getAttributes());

        boolean matched = false;
        for (Map.Entry<String, AttributeState> entry :
                state.getAttributes().entrySet()) {
            AttributeState attribute = entry.getValue();
            if (experimentalGroup.equals(attribute.getProperty().getGroup())) {
                matched = true;
                changed.put(entry.getKey(), attribute.withCurrentPercentage(
                        attribute.getCurrentPercentage() + delta));
            }
        }

        if (!matched) {
            throw new IllegalArgumentException(
                    "No experimental group named: " + experimentalGroup);
        }
        return state.replaceAttributes(changed);
    }

    public double calculateWeightedValue(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> assignments,
            ResourceStat stat) {
        return calculateWeightedValue(
                schematic, assignments,
                new ArrayList<ComponentSlotAssignment>(), stat);
    }

    public double calculateWeightedValue(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> assignments,
            List<ComponentSlotAssignment> componentAssignments,
            ResourceStat stat) {
        if (schematic == null) throw new NullPointerException("schematic");
        if (assignments == null) throw new NullPointerException("assignments");
        if (componentAssignments == null) {
            throw new NullPointerException("componentAssignments");
        }
        if (stat == null) throw new NullPointerException("stat");
        if (stat == ResourceStat.BK) {
            throw new UnsupportedInfinityRuleException(
                    "BK is source-recognized but generic value resolution is unresolved");
        }
        Map<Integer, ResourceInput> resources = indexAssignments(schematic, assignments);
        Map<Integer, ComponentSlotAssignment> components =
                indexComponentAssignments(componentAssignments);
        validateRequiredResourceSlots(schematic, resources);
        return calculateWeightedValue(schematic, resources, components, stat);
    }

    public static double assemblyPercentage(double weightedScore) {
        requireFiniteNonNegative(weightedScore, "weightedScore");
        return weightedScore
                * (0.000015d * weightedScore + 0.015d)
                * 0.01d;
    }

    public static double interpolate(
            ExperimentalProperty property,
            double percentage) {
        if (property == null) throw new NullPointerException("property");
        return interpolate(
                property.getGroup(),
                property.getMinValue(),
                property.getMaxValue(),
                percentage);
    }

    static double interpolate(
            String group,
            double min,
            double max,
            double percentage) {
        return swg.crafting.simulator.scenario.Interpolation.interpolate(
                group, min, max, percentage);
    }

    private double calculatePropertyScore(
            SchematicDefinition schematic,
            Map<Integer, ResourceInput> resources,
            Map<Integer, ComponentSlotAssignment> components,
            ExperimentalProperty property) {
        double weightedScore = 0.0d;
        for (PropertyWeight weight : property.getWeights()) {
            if (weight.getStat() == ResourceStat.BK) {
                throw new UnsupportedInfinityRuleException(
                        "BK cannot be treated as exact in generic Resource Laboratory");
            }
            weightedScore += calculateWeightedValue(
                    schematic, resources, components, weight.getStat())
                    * weight.getNormalizedWeight();
        }
        return weightedScore;
    }

    private double calculateWeightedValue(
            SchematicDefinition schematic,
            Map<Integer, ResourceInput> resources,
            Map<Integer, ComponentSlotAssignment> components,
            ResourceStat stat) {
        long quantitySum = 0L;
        double weightedTotal = 0.0d;

        for (IngredientSlotDefinition slot : schematic.getSlots()) {
            if (slot.getKind() == SlotKind.RESOURCE) {
                ResourceInput resource =
                        resources.get(Integer.valueOf(slot.getIndex()));
                if (resource == null) continue;

                int value = resource.getStat(stat);
                if (value != 0) {
                    quantitySum += slot.getQuantity();
                    weightedTotal +=
                            (double) value * (double) slot.getQuantity();
                }
                continue;
            }

            ComponentSlotAssignment assignment =
                    components.get(Integer.valueOf(slot.getIndex()));
            if (assignment == null || assignment.getComponentUses().isEmpty()) {
                continue;
            }

            // Mirrors ComponentSlot::getPrototype(): only contents[0].
            ComponentInstance prototype =
                    assignment.getComponentUses().get(0).getComponent();
            if (!prototype.isCustomResourceIngredient()) continue;

            int value = prototype.getResourceStat(stat);
            if (value != 0) {
                // Infinity uses the draft-slot quantity, not inserted uses.
                quantitySum += slot.getQuantity();
                weightedTotal +=
                        (double) value * (double) slot.getQuantity();
            }
        }

        return weightedTotal == 0.0d
                ? 0.0d
                : weightedTotal / (double) quantitySum;
    }

    private Map<Integer, ComponentSlotAssignment> indexComponentAssignments(
            List<ComponentSlotAssignment> assignments) {
        Map<Integer, ComponentSlotAssignment> indexed =
                new HashMap<Integer, ComponentSlotAssignment>();
        for (ComponentSlotAssignment assignment : assignments) {
            if (assignment == null) throw new NullPointerException("component assignment");
            Integer index = Integer.valueOf(assignment.getSlotIndex());
            if (indexed.put(index, assignment) != null) {
                throw new IllegalArgumentException(
                        "Duplicate component assignment for slot " + index);
            }
        }
        return indexed;
    }

    private Map<Integer, ResourceInput> indexAssignments(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> assignments) {
        Map<Integer, IngredientSlotDefinition> slotByIndex =
                new HashMap<Integer, IngredientSlotDefinition>();
        for (IngredientSlotDefinition slot : schematic.getSlots()) {
            slotByIndex.put(Integer.valueOf(slot.getIndex()), slot);
        }

        Map<Integer, ResourceInput> indexed =
                new HashMap<Integer, ResourceInput>();
        for (ResourceSlotAssignment assignment : assignments) {
            if (assignment == null) throw new NullPointerException("assignment");
            Integer index = Integer.valueOf(assignment.getSlotIndex());
            IngredientSlotDefinition slot = slotByIndex.get(index);
            if (slot == null) {
                throw new IllegalArgumentException(
                        "No schematic slot at index " + assignment.getSlotIndex());
            }
            if (slot.getKind() != SlotKind.RESOURCE) {
                throw new IllegalArgumentException(
                        "Slot " + assignment.getSlotIndex() + " is not a resource slot");
            }
            if (!assignment.getResource().isType(slot.getAcceptedType())) {
                throw new IllegalArgumentException(
                        assignment.getResource().getName()
                        + " cannot satisfy resource type "
                        + slot.getAcceptedType()
                        + " at slot " + slot.getIndex());
            }
            if (indexed.put(index, assignment.getResource()) != null) {
                throw new IllegalArgumentException(
                        "Duplicate assignment for resource slot " + index);
            }
            long available = assignment.getResource().getAvailableQuantity();
            if (available >= 0L && available < slot.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient quantity of "
                        + assignment.getResource().getName()
                        + " for slot " + slot.getIndex());
            }
        }
        return indexed;
    }

    private void validateRequiredResourceSlots(
            SchematicDefinition schematic,
            Map<Integer, ResourceInput> resources) {
        for (IngredientSlotDefinition slot : schematic.getSlots()) {
            if (slot.getKind() == SlotKind.RESOURCE
                    && !resources.containsKey(Integer.valueOf(slot.getIndex()))) {
                throw new IllegalArgumentException(
                        "Missing resource assignment for slot " + slot.getIndex());
            }
        }
    }

    private void requireResourceLaboratory(SchematicDefinition schematic) {
        if (schematic == null) throw new NullPointerException("schematic");
        if (schematic.getLaboratory() != LaboratoryType.RESOURCE) {
            throw new UnsupportedInfinityRuleException(
                    "ResourceLaboratory cannot process " + schematic.getLaboratory());
        }
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0d) {
            throw new IllegalArgumentException(name + " must be finite and >= 0");
        }
    }
}
