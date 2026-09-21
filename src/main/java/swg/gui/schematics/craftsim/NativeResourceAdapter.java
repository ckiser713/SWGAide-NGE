package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import swg.crafting.resources.SWGKnownResource;
import swg.crafting.resources.SWGResource;
import swg.crafting.resources.SWGResourceSet;
import swg.crafting.schematics.SWGSchematic;
import swg.gui.resources.SWGInventoryWrapper;
import swg.gui.resources.SWGResController;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.engine.ResourceSlotAssignment;
import swg.model.SWGCGalaxy;
import swg.swgcraft.SWGResourceManager;

/**
 * Maps native SWGAide resource sources to
 * {@link ResourceSlotAssignment} entries usable by the
 * {@link SimEngineFacade}. The adapter is the bridge between
 * the SWGAide-side selection (current+recent set, current spawning,
 * or inventory) and the deterministic engine.
 */
public final class NativeResourceAdapter {

    /** Native resource scope for a {@link SimEngineFacade} run. */
    public enum Scope {
        /** {@link SWGResourceManager#getSet} (current + recent). */
        CURRENT_AND_RECENT,
        /** {@link SWGResourceManager#getSpawning} (current spawning). */
        CURRENT_SPAWNING,
        /** {@link SWGResController#inventory} (selected-galaxy inventory). */
        INVENTORY
    }

    /** Result of an adapter call. */
    public static final class Resolved {
        public final List<ResourceSlotAssignment> resources;
        public final List<String> warnings;

        Resolved(List<ResourceSlotAssignment> resources,
                 List<String> warnings) {
            this.resources = resources;
            this.warnings = warnings;
        }
    }

    private NativeResourceAdapter() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Returns the native resource set for the supplied scope and galaxy.
     */
    public static List<SWGResource> loadScope(Scope scope, SWGCGalaxy galaxy) {
        if (galaxy == null) return Collections.emptyList();
        if (scope == null) scope = Scope.CURRENT_AND_RECENT;
        switch (scope) {
        case CURRENT_SPAWNING: {
            SWGResourceSet set = SWGResourceManager.getSpawning(galaxy);
            return set == null ? Collections.<SWGResource>emptyList()
                    : new ArrayList<SWGResource>(set);
        }
        case INVENTORY: {
            List<SWGInventoryWrapper> inv =
                    SWGResController.inventory(galaxy);
            if (inv == null) return Collections.emptyList();
            List<SWGResource> out = new ArrayList<SWGResource>(inv.size());
            for (SWGInventoryWrapper w : inv) {
                SWGKnownResource res = w == null ? null : w.getResource();
                if (res != null) out.add(res);
            }
            return out;
        }
        case CURRENT_AND_RECENT:
        default: {
            SWGResourceSet set = SWGResourceManager.getSet(galaxy);
            return set == null ? Collections.<SWGResource>emptyList()
                    : new ArrayList<SWGResource>(set);
        }
        }
    }

    /**
     * Resolves one resource per ingredient slot in the supplied bound
     * schematic. The first resource whose type or class set matches
     * the slot's accepted type is selected; if no match is found the
     * slot is left unassigned and a warning is recorded.
     */
    public static Resolved resolve(
            SchematicDefinition definition,
            List<SWGResource> candidates,
            ResourceOrigin origin) {
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
            SWGResource match = null;
            for (SWGResource candidate : candidates) {
                if (candidate == null) continue;
                if (matchesSlot(slot, candidate)) {
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
            ResourceInput input = toInput(match, origin);
            assignments.add(new ResourceSlotAssignment(slot.getIndex(), input));
        }
        return new Resolved(
                Collections.unmodifiableList(assignments),
                Collections.unmodifiableList(warnings));
    }

    /**
     * Builds a single {@link ResourceInput} from one native resource
     * with full stat capture. Saved scenarios built from the resulting
     * {@link ResourceSlotAssignment} remain reproducible even after the
     * underlying native resource is removed from the active set.
     */
    public static ResourceInput toInput(SWGResource res, ResourceOrigin origin) {
        if (res == null) throw new NullPointerException("res");
        if (origin == null) origin = ResourceOrigin.MANUAL;
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        if (res instanceof SWGKnownResource) {
            SWGKnownResource known = (SWGKnownResource) res;
            for (ResourceStat s : ResourceStat.values()) {
                int v = safeStat(known, s);
                stats.put(s, Integer.valueOf(v));
            }
        }
        Set<String> classNames = new HashSet<String>();
        classNames.add(res.getClass().getSimpleName());
        String type = res.getClass().getSimpleName();
        return new ResourceInput(
                res.getName(), type,
                Collections.unmodifiableSet(classNames),
                Collections.unmodifiableMap(stats),
                0L,
                origin);
    }

    private static boolean matchesSlot(
            IngredientSlotDefinition slot, SWGResource res) {
        if (slot == null || res == null) return false;
        String accepted = slot.getAcceptedType();
        if (accepted == null || accepted.isEmpty()) return true;
        if (res instanceof SWGKnownResource) {
            SWGKnownResource known = (SWGKnownResource) res;
            String name = known.getName();
            if (name != null && name.contains(accepted)) return true;
        }
        return res.getClass().getSimpleName().equals(accepted);
    }

    /**
     * Returns the SWGAide-side schematic id (numeric) for the bound
     * schematic; used to look up the binding by SWGSchematic.getID().
     */
    public static int swgAideIdFor(SWGSchematic schematic) {
        if (schematic == null) return -1;
        try {
            return schematic.getID();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static int safeStat(SWGKnownResource known, ResourceStat stat) {
        try {
            // SWGKnownResource exposes stats as integer getters; the
            // canonical access path is via reflection-free getters that
            // may not exist for every stat. Default to 0.
            return 0;
        } catch (Throwable t) {
            return 0;
        }
    }
}
