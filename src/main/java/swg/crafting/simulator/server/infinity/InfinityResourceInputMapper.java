package swg.crafting.simulator.server.infinity;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import swg.crafting.simulator.resources.ResourceSnapshot;
import swg.crafting.simulator.resources.ResourceSource;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;

/** Converts a provider-neutral resource snapshot into Infinity engine input. */
public final class InfinityResourceInputMapper {

    public ResourceInput map(ResourceSnapshot snapshot) {
        if (snapshot == null) throw new NullPointerException("snapshot");

        Map<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        for (ResourceStat stat : ResourceStat.values()) {
            if (stat == ResourceStat.BK) continue;
            stats.put(stat, Integer.valueOf(snapshot.getStat(stat.name())));
        }

        Set<String> classes = new LinkedHashSet<String>();
        classes.addAll(snapshot.getClassTokens());
        classes.addAll(snapshot.getClassNames());

        return new ResourceInput(
                snapshot.getName(),
                snapshot.getTypeToken(),
                classes,
                stats,
                snapshot.getAvailableQuantity(),
                origin(snapshot.getSource()));
    }

    private ResourceOrigin origin(ResourceSource source) {
        switch (source) {
        case CURRENT:
            return ResourceOrigin.CURRENT;
        case INVENTORY:
            return ResourceOrigin.INVENTORY;
        case MANUAL:
            return ResourceOrigin.MANUAL;
        case HYPOTHETICAL:
            return ResourceOrigin.HYPOTHETICAL;
        case HISTORICAL_REMOTE:
        case LOADED_RECENT:
        default:
            return ResourceOrigin.HISTORICAL_REMOTE;
        }
    }
}
