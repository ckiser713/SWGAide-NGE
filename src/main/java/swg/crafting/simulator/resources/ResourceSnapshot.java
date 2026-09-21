package swg.crafting.simulator.resources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Immutable provider-neutral snapshot of one SWGAide resource.
 *
 * <p>The simulator snapshots native resources before arithmetic so server
 * modules never depend on mutable/serialized SWGAide resource objects. All
 * native stat codes are retained by name (including ER) even when a particular
 * server module does not use them.</p>
 */
public final class ResourceSnapshot {
    private final int serverId;
    private final String name;
    private final String typeToken;
    private final String typeName;
    private final Set<String> classTokens;
    private final Set<String> classNames;
    private final Map<String, Integer> stats;
    private final long availableQuantity;
    private final ResourceSource source;

    public ResourceSnapshot(
            int serverId,
            String name,
            String typeToken,
            String typeName,
            Set<String> classTokens,
            Set<String> classNames,
            Map<String, Integer> stats,
            long availableQuantity,
            ResourceSource source) {
        if (serverId <= 0) {
            throw new IllegalArgumentException("serverId must be > 0");
        }
        this.serverId = serverId;
        this.name = requireText(name, "name");
        this.typeToken = requireText(typeToken, "typeToken");
        this.typeName = requireText(typeName, "typeName");
        if (classTokens == null) throw new NullPointerException("classTokens");
        if (classNames == null) throw new NullPointerException("classNames");
        if (stats == null) throw new NullPointerException("stats");
        if (source == null) throw new NullPointerException("source");
        if (availableQuantity < -1L) {
            throw new IllegalArgumentException(
                    "availableQuantity must be -1 (unknown) or >= 0");
        }

        this.classTokens = Collections.unmodifiableSet(
                new LinkedHashSet<String>(classTokens));
        this.classNames = Collections.unmodifiableSet(
                new LinkedHashSet<String>(classNames));

        Map<String, Integer> copied = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            String code = requireText(entry.getKey(), "stat code").toUpperCase();
            if (entry.getValue() == null) throw new NullPointerException("stat value");
            int value = entry.getValue().intValue();
            if (value < 0 || value > 1000) {
                throw new IllegalArgumentException(
                        "stat outside [0,1000]: " + code + "=" + value);
            }
            copied.put(code, Integer.valueOf(value));
        }
        this.stats = Collections.unmodifiableMap(copied);
        this.availableQuantity = availableQuantity;
        this.source = source;
    }

    public int getServerId() { return serverId; }
    public String getName() { return name; }
    public String getTypeToken() { return typeToken; }
    public String getTypeName() { return typeName; }
    public Set<String> getClassTokens() { return classTokens; }
    public Set<String> getClassNames() { return classNames; }
    public Map<String, Integer> getStats() { return stats; }
    public long getAvailableQuantity() { return availableQuantity; }
    public ResourceSource getSource() { return source; }

    public int getStat(String code) {
        if (code == null) return 0;
        Integer value = stats.get(code.toUpperCase());
        return value == null ? 0 : value.intValue();
    }

    /** Exact class-token/name ancestry match; no substring heuristics. */
    public boolean matchesAcceptedType(String acceptedType) {
        if (acceptedType == null || acceptedType.trim().isEmpty()) return true;
        String wanted = acceptedType.trim();
        if (typeToken.equalsIgnoreCase(wanted)
                || typeName.equalsIgnoreCase(wanted)) {
            return true;
        }
        for (String token : classTokens) {
            if (token.equalsIgnoreCase(wanted)) return true;
        }
        for (String nameValue : classNames) {
            if (nameValue.equalsIgnoreCase(wanted)) return true;
        }
        return false;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" [").append(source.name()).append("]");
        if (availableQuantity >= 0L) {
            sb.append(" qty=").append(availableQuantity);
        }
        if (!stats.isEmpty()) {
            sb.append(" {");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                if (entry.getValue().intValue() == 0) continue;
                if (!first) sb.append(' ');
                first = false;
                sb.append(entry.getKey()).append('=').append(entry.getValue());
            }
            sb.append('}');
        }
        return sb.toString();
    }
}
