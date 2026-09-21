package swg.crafting.simulator.contracts;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fail-closed structural validation for normalized Infinity rules. */
public final class RulesetValidator {
    private static final double WEIGHT_EPSILON = 0.000001d;

    private RulesetValidator() {
        throw new AssertionError("Do not instantiate");
    }

    public static void validate(InfinityRuleset ruleset) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        RulesetManifest manifest = ruleset.getManifest();
        if (!"swginfinity/public".equals(manifest.getRepository())) {
            throw new IllegalArgumentException(
                    "Unexpected rules repository: " + manifest.getRepository());
        }

        for (SchematicDefinition schematic : ruleset.getSchematics()) {
            validateSchematic(schematic);
            if (ruleset.getCoverage(schematic.getId()) == null) {
                throw new IllegalArgumentException(
                        "Missing coverage record: " + schematic.getId());
            }
        }
    }

    private static void validateSchematic(SchematicDefinition schematic) {
        Set<Integer> slotIndexes = new HashSet<Integer>();
        for (IngredientSlotDefinition slot : schematic.getSlots()) {
            if (!slotIndexes.add(Integer.valueOf(slot.getIndex()))) {
                throw new IllegalArgumentException(
                        "Duplicate slot index " + slot.getIndex()
                        + " in " + schematic.getId());
            }
        }

        Set<String> attributes = new HashSet<String>();
        for (ExperimentalProperty property : schematic.getProperties()) {
            if (!attributes.add(property.getAttribute())) {
                throw new IllegalArgumentException(
                        "Duplicate attribute " + property.getAttribute()
                        + " in " + schematic.getId());
            }
            validateWeights(schematic, property);
        }
    }

    private static void validateWeights(
            SchematicDefinition schematic,
            ExperimentalProperty property) {
        List<PropertyWeight> weights = property.getWeights();
        if (weights.isEmpty()) return;

        double normalized = 0.0d;
        int raw = 0;
        for (PropertyWeight weight : weights) {
            normalized += weight.getNormalizedWeight();
            raw += weight.getRawWeight();
            if (weight.getStat() == ResourceStat.BK) {
                throw new IllegalArgumentException(
                        "BK is source-recognized but not accepted by generic exact coverage: "
                        + schematic.getId() + ":" + property.getAttribute());
            }
        }
        if (raw <= 0) {
            throw new IllegalArgumentException(
                    "Non-empty weights have zero raw total for "
                    + schematic.getId() + ":" + property.getAttribute());
        }
        if (Math.abs(1.0d - normalized) > WEIGHT_EPSILON) {
            throw new IllegalArgumentException(
                    "Normalized weights do not sum to 1 for "
                    + schematic.getId() + ":" + property.getAttribute()
                    + " (" + normalized + ")");
        }
    }
}
