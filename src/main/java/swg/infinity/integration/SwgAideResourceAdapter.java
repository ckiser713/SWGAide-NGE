package swg.infinity.integration;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import swg.crafting.Stat;
import swg.crafting.resources.SWGKnownResource;
import swg.crafting.resources.SWGResourceClass;
import swg.crafting.resources.SWGResourceStats;
import swg.gui.resources.SWGInventoryWrapper;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;

/**
 * One-way snapshot adapter from legacy SWGAide resource objects into the
 * isolated Infinity engine.
 */
public final class SwgAideResourceAdapter {

    public ResourceInput fromInventory(SWGInventoryWrapper wrapper) {
        if (wrapper == null) throw new NullPointerException("wrapper");
        return fromKnownResource(
                wrapper.getResource(),
                ResourceOrigin.INVENTORY,
                wrapper.getAmount());
    }

    public ResourceInput fromKnownResource(
            SWGKnownResource resource,
            ResourceOrigin origin,
            long availableQuantity) {
        if (resource == null) throw new NullPointerException("resource");
        if (origin == null) throw new NullPointerException("origin");

        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        SWGResourceStats sourceStats = resource.stats();
        put(stats, ResourceStat.CR, sourceStats.value(Stat.CR));
        put(stats, ResourceStat.CD, sourceStats.value(Stat.CD));
        put(stats, ResourceStat.DR, sourceStats.value(Stat.DR));
        put(stats, ResourceStat.HR, sourceStats.value(Stat.HR));
        put(stats, ResourceStat.FL, sourceStats.value(Stat.FL));
        put(stats, ResourceStat.MA, sourceStats.value(Stat.MA));
        put(stats, ResourceStat.PE, sourceStats.value(Stat.PE));
        put(stats, ResourceStat.OQ, sourceStats.value(Stat.OQ));
        put(stats, ResourceStat.SR, sourceStats.value(Stat.SR));
        put(stats, ResourceStat.UT, sourceStats.value(Stat.UT));

        SWGResourceClass resourceClass = resource.rc();
        Set<String> ancestry = collectClassAncestry(resourceClass);

        return new ResourceInput(
                resource.getName(),
                resourceClass.rcToken(),
                ancestry,
                stats,
                availableQuantity,
                origin);
    }

    private Set<String> collectClassAncestry(SWGResourceClass resourceClass) {
        Set<String> names = new HashSet<String>();
        names.add(resourceClass.rcToken());
        names.add(resourceClass.rcName());

        for (int id = 1; id <= SWGResourceClass.maxID(); ++id) {
            try {
                SWGResourceClass candidate = SWGResourceClass.rc(id);
                if (resourceClass.isSub(candidate)) {
                    names.add(candidate.rcToken());
                    names.add(candidate.rcName());
                }
            } catch (IllegalArgumentException unusedHole) {
                // SWGAide resource-class IDs intentionally contain holes.
            }
        }
        return names;
    }

    private void put(
            Map<ResourceStat, Integer> target,
            ResourceStat stat,
            int value) {
        target.put(stat, Integer.valueOf(value));
    }
}
