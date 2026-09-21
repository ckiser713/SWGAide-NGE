package swg.crafting.simulator.integration;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import swg.crafting.Stat;
import swg.crafting.resources.SWGKnownResource;
import swg.crafting.resources.SWGResourceClass;
import swg.crafting.resources.SWGResourceStats;
import swg.crafting.simulator.resources.ResourceSnapshot;
import swg.crafting.simulator.resources.ResourceSource;
import swg.gui.resources.SWGInventoryWrapper;
import swg.model.SWGCGalaxy;

/**
 * One-way adapter from native SWGAide resources into provider-neutral immutable
 * simulator snapshots.
 */
public final class SwgAideResourceSnapshotAdapter {

    public ResourceSnapshot fromInventory(SWGInventoryWrapper wrapper) {
        if (wrapper == null) throw new NullPointerException("wrapper");
        return fromKnownResource(
                wrapper.getResource(),
                ResourceSource.INVENTORY,
                wrapper.getAmount());
    }

    public ResourceSnapshot fromKnownResource(
            SWGKnownResource resource,
            ResourceSource source,
            long availableQuantity) {
        if (resource == null) throw new NullPointerException("resource");
        if (source == null) throw new NullPointerException("source");

        SWGCGalaxy galaxy = resource.galaxy();
        if (galaxy == null) {
            throw new IllegalArgumentException(
                    "Native known resource has no galaxy: " + resource.getName());
        }

        SWGResourceClass resourceClass = resource.rc();
        Set<String> tokens = new LinkedHashSet<String>();
        Set<String> names = new LinkedHashSet<String>();
        tokens.add(resourceClass.rcToken());
        names.add(resourceClass.rcName());

        for (int id = 1; id <= SWGResourceClass.maxID(); ++id) {
            try {
                SWGResourceClass candidate = SWGResourceClass.rc(id);
                if (resourceClass.isSub(candidate)) {
                    tokens.add(candidate.rcToken());
                    names.add(candidate.rcName());
                }
            } catch (IllegalArgumentException unusedHole) {
                // SWGAide resource-class IDs intentionally contain holes.
            }
        }

        Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
        SWGResourceStats sourceStats = resource.stats();
        for (Stat stat : Stat.values()) {
            stats.put(stat.name(), Integer.valueOf(sourceStats.value(stat)));
        }

        return new ResourceSnapshot(
                galaxy.id(),
                resource.getName(),
                resourceClass.rcToken(),
                resourceClass.rcName(),
                tokens,
                names,
                stats,
                availableQuantity,
                source);
    }
}
