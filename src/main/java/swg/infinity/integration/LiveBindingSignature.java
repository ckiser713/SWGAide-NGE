package swg.infinity.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import swg.crafting.schematics.SWGComponentSlot;
import swg.crafting.schematics.SWGResourceSlot;
import swg.crafting.schematics.SWGSchematic;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/**
 * Multi-signal runtime signature for binding real SWGAide schematic IDs to
 * normalized Infinity schematics.
 *
 * <p>This deliberately avoids test-only synthetic IDs and avoids relying on
 * experiment-group display strings, which live in different naming domains on
 * the SWGAide and Infinity sides.</p>
 */
public final class LiveBindingSignature {
    private final String normalizedName;
    private final List<String> resourceSlots;
    private final List<String> componentSlots;

    private LiveBindingSignature(
            String normalizedName,
            List<String> resourceSlots,
            List<String> componentSlots) {
        this.normalizedName = normalizedName;
        this.resourceSlots = Collections.unmodifiableList(
                new ArrayList<String>(resourceSlots));
        this.componentSlots = Collections.unmodifiableList(
                new ArrayList<String>(componentSlots));
    }

    public static LiveBindingSignature fromSwgAide(SWGSchematic schematic) {
        if (schematic == null) throw new NullPointerException("schematic");
        List<String> resources = new ArrayList<String>();
        for (SWGResourceSlot slot : schematic.getResourceSlots()) {
            resources.add(
                    slot.getResourceClass().rcToken()
                    + ":" + slot.getUnits());
        }
        Collections.sort(resources);

        List<String> components = new ArrayList<String>();
        for (SWGComponentSlot slot : schematic.getComponentSlots()) {
            SlotKind kind;
            if (slot.isOptional()) {
                kind = slot.isSimilar()
                        ? SlotKind.OPTIONAL_MIXED_COMPONENT
                        : SlotKind.OPTIONAL_IDENTICAL_COMPONENT;
            } else {
                kind = slot.isSimilar()
                        ? SlotKind.MIXED_COMPONENT
                        : SlotKind.IDENTICAL_COMPONENT;
            }
            components.add(kind.name() + ":" + slot.getAmount());
        }
        Collections.sort(components);

        return new LiveBindingSignature(
                normalizeName(schematic.getName()),
                resources,
                components);
    }

    public static LiveBindingSignature fromInfinity(
            SchematicDefinition definition) {
        if (definition == null) throw new NullPointerException("definition");
        List<String> resources = new ArrayList<String>();
        List<String> components = new ArrayList<String>();
        for (IngredientSlotDefinition slot : definition.getSlots()) {
            if (slot.getKind() == SlotKind.RESOURCE) {
                resources.add(slot.getAcceptedType() + ":" + slot.getQuantity());
            } else {
                components.add(
                        slot.getKind().name() + ":" + slot.getQuantity());
            }
        }
        Collections.sort(resources);
        Collections.sort(components);

        return new LiveBindingSignature(
                normalizeName(definition.getDisplayName()),
                resources,
                components);
    }

    public boolean alignsWith(LiveBindingSignature other) {
        if (other == null) return false;
        return normalizedName.equals(other.normalizedName)
                && resourceSlots.equals(other.resourceSlots)
                && componentSlots.equals(other.componentSlots);
    }

    public String getNormalizedName() { return normalizedName; }
    public List<String> getResourceSlots() { return resourceSlots; }
    public List<String> getComponentSlots() { return componentSlots; }

    static LiveBindingSignature fixture(
            String name,
            List<String> resourceSlots,
            List<String> componentSlots) {
        List<String> resources = new ArrayList<String>(resourceSlots);
        List<String> components = new ArrayList<String>(componentSlots);
        Collections.sort(resources);
        Collections.sort(components);
        return new LiveBindingSignature(
                normalizeName(name), resources, components);
    }

    private static String normalizeName(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    @Override
    public String toString() {
        return normalizedName + "|r=" + resourceSlots + "|c=" + componentSlots;
    }
}
