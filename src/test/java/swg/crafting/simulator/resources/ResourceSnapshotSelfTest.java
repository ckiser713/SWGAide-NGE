package swg.crafting.simulator.resources;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import swg.crafting.simulator.server.infinity.InfinityResourceInputMapper;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.engine.ResourceInput;

/** Dependency-free contract test for provider-neutral native resource snapshots. */
public final class ResourceSnapshotSelfTest {
    private ResourceSnapshotSelfTest() {
    }

    public static void main(String[] args) {
        Set<String> tokens =
                new LinkedHashSet<String>(Arrays.asList("steel", "metal"));
        Set<String> names =
                new LinkedHashSet<String>(Arrays.asList("Steel", "Metal"));
        Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
        stats.put("OQ", Integer.valueOf(987));
        stats.put("UT", Integer.valueOf(876));
        stats.put("ER", Integer.valueOf(765));

        ResourceSnapshot snapshot = new ResourceSnapshot(
                154,
                "Testium",
                "steel",
                "Steel",
                tokens,
                names,
                stats,
                12345L,
                ResourceSource.INVENTORY);

        if (!snapshot.matchesAcceptedType("metal")
                || !snapshot.matchesAcceptedType("Steel")) {
            throw new AssertionError("resource ancestry match failed");
        }
        if (snapshot.getStat("ER") != 765) {
            throw new AssertionError("provider-neutral ER metadata lost");
        }

        ResourceInput input = new InfinityResourceInputMapper().map(snapshot);
        if (input.getStat(ResourceStat.OQ) != 987
                || input.getStat(ResourceStat.UT) != 876) {
            throw new AssertionError("Infinity stat mapping failed");
        }
        if (input.getAvailableQuantity() != 12345L) {
            throw new AssertionError("inventory quantity was not preserved");
        }
        if (!input.isType("metal")) {
            throw new AssertionError("Infinity resource ancestry was not preserved");
        }

        System.out.println("ResourceSnapshotSelfTest PASS");
    }
}
