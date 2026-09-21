package swg.infinity.engine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import swg.infinity.contracts.ResourceStat;

/**
 * Immutable resource snapshot consumed by the Infinity engine.
 *
 * <p>The object is detached from SWGAide mutable/cache state. ER and other
 * metadata may remain in SWGAide, but only source-proven generic crafting stats
 * are represented here.</p>
 */
public final class ResourceInput {
    private final String name;
    private final String resourceType;
    private final Set<String> classNames;
    private final Map<ResourceStat, Integer> stats;
    private final long availableQuantity;
    private final ResourceOrigin origin;

    public ResourceInput(
            String name,
            String resourceType,
            Set<String> classNames,
            Map<ResourceStat, Integer> stats,
            long availableQuantity,
            ResourceOrigin origin) {
        this.name = requireText(name, "name");
        this.resourceType = requireText(resourceType, "resourceType");
        if (classNames == null) throw new NullPointerException("classNames");
        if (stats == null) throw new NullPointerException("stats");
        if (origin == null) throw new NullPointerException("origin");
        if (availableQuantity < -1L) {
            throw new IllegalArgumentException(
                    "availableQuantity must be -1 (unknown) or >= 0");
        }

        this.classNames = Collections.unmodifiableSet(
                new HashSet<String>(classNames));

        EnumMap<ResourceStat, Integer> copied =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        for (Map.Entry<ResourceStat, Integer> entry : stats.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new NullPointerException("resource stat");
            }
            int value = entry.getValue().intValue();
            if (value < 0 || value > 1000) {
                throw new IllegalArgumentException(
                        "Resource stat outside [0,1000]: "
                        + entry.getKey() + "=" + value);
            }
            copied.put(entry.getKey(), Integer.valueOf(value));
        }

        this.stats = Collections.unmodifiableMap(copied);
        this.availableQuantity = availableQuantity;
        this.origin = origin;
    }

    public String getName() { return name; }
    public String getResourceType() { return resourceType; }
    public Set<String> getClassNames() { return classNames; }
    public Map<ResourceStat, Integer> getStats() { return stats; }
    public long getAvailableQuantity() { return availableQuantity; }
    public ResourceOrigin getOrigin() { return origin; }

    public int getStat(ResourceStat stat) {
        Integer value = stats.get(stat);
        return value == null ? 0 : value.intValue();
    }

    public boolean isType(String requiredType) {
        if (requiredType == null) return false;
        return resourceType.equals(requiredType) || classNames.contains(requiredType);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
