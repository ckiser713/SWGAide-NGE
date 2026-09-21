package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.resources.SWGKnownResource;
import swg.crafting.resources.SWGResourceSet;
import swg.crafting.schematics.SWGSchematic;
import swg.crafting.simulator.integration.SwgAideResourceSnapshotAdapter;
import swg.crafting.simulator.resources.ResourceSnapshot;
import swg.crafting.simulator.resources.ResourceSource;
import swg.crafting.simulator.server.infinity.InfinityResourceInputMapper;
import swg.gui.resources.SWGInventoryWrapper;
import swg.gui.resources.SWGResController;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceSlotAssignment;
import swg.model.SWGCGalaxy;
import swg.swgcraft.SWGResourceManager;

/**
 * Native SWGAide resource bridge used by the current Infinity server module.
 *
 * <p>SWGAide resources are first converted into provider-neutral immutable
 * {@link ResourceSnapshot}s. The Infinity mapper then translates only the
 * stat/class semantics that Infinity understands. No resource stats are
 * fabricated and inventory quantities are preserved.</p>
 */
public final class NativeResourceAdapter {

    public enum Scope {
        CURRENT_AND_RECENT,
        CURRENT_SPAWNING,
        INVENTORY
    }

    public static final class Resolved {
        public final List<ResourceSlotAssignment> resources;
        public final List<String> warnings;

        Resolved(List<ResourceSlotAssignment> resources,
                 List<String> warnings) {
            this.resources = resources;
            this.warnings = warnings;
        }
    }

    private static final SwgAideResourceSnapshotAdapter SNAPSHOT =
            new SwgAideResourceSnapshotAdapter();
    private static final InfinityResourceInputMapper INFINITY =
            new InfinityResourceInputMapper();

    private NativeResourceAdapter() {
        throw new AssertionError("Do not instantiate");
    }

    /** Returns immutable snapshots from SWGAide's existing selected-server data. */
    public static List<ResourceSnapshot> loadScope(
            Scope scope, SWGCGalaxy galaxy) {
        if (galaxy == null) return Collections.emptyList();
        if (scope == null) scope = Scope.CURRENT_AND_RECENT;

        List<ResourceSnapshot> out = new ArrayList<ResourceSnapshot>();
        switch (scope) {
        case CURRENT_SPAWNING: {
            SWGResourceSet set = SWGResourceManager.getSpawning(galaxy);
            if (set != null) {
                for (SWGKnownResource resource : set) {
                    out.add(SNAPSHOT.fromKnownResource(
                            resource, ResourceSource.CURRENT, -1L));
                }
            }
            break;
        }
        case INVENTORY: {
            List<SWGInventoryWrapper> inventory =
                    SWGResController.inventory(galaxy);
            if (inventory != null) {
                for (SWGInventoryWrapper wrapper : inventory) {
                    if (wrapper != null && wrapper.getResource() != null) {
                        out.add(SNAPSHOT.fromInventory(wrapper));
                    }
                }
            }
            break;
        }
        case CURRENT_AND_RECENT:
        default: {
            SWGResourceSet set = SWGResourceManager.getSet(galaxy);
            if (set != null) {
                for (SWGKnownResource resource : set) {
                    out.add(SNAPSHOT.fromKnownResource(
                            resource, ResourceSource.LOADED_RECENT, -1L));
                }
            }
            break;
        }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Resolves one compatible resource per raw-resource slot using exact
     * SWGAide resource-class ancestry captured in each snapshot.
     */
    public static Resolved resolve(
            SchematicDefinition definition,
            List<ResourceSnapshot> candidates) {
        if (definition == null) throw new NullPointerException("definition");

        List<ResourceSlotAssignment> assignments =
                new ArrayList<ResourceSlotAssignment>();
        List<String> warnings = new ArrayList<String>();

        if (candidates == null || candidates.isEmpty()) {
            return new Resolved(
                    Collections.unmodifiableList(assignments),
                    Collections.unmodifiableList(warnings));
        }

        for (IngredientSlotDefinition slot : definition.getSlots()) {
            if (!slot.getKind().equals(
                    swg.infinity.contracts.SlotKind.RESOURCE)) {
                continue;
            }

            ResourceSnapshot match = null;
            for (ResourceSnapshot candidate : candidates) {
                if (candidate != null
                        && candidate.matchesAcceptedType(
                                slot.getAcceptedType())) {
                    match = candidate;
                    break;
                }
            }

            if (match == null) {
                warnings.add("no resource matches slot "
                        + slot.getIndex() + " (accepted="
                        + slot.getAcceptedType() + ")");
                continue;
            }

            ResourceInput input = INFINITY.map(match);
            assignments.add(new ResourceSlotAssignment(
                    slot.getIndex(), input));
        }

        return new Resolved(
                Collections.unmodifiableList(assignments),
                Collections.unmodifiableList(warnings));
    }

    public static int swgAideIdFor(SWGSchematic schematic) {
        if (schematic == null) return -1;
        try {
            return schematic.getID();
        } catch (Throwable t) {
            return -1;
        }
    }
}
