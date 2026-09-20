package swg.infinity.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import swg.infinity.contracts.ResourceStat;

/**
 * Immutable exact component instance or hypothetical component candidate.
 *
 * <p>Property order is preserved because the current Infinity generic
 * component application path has order-sensitive modified-state behavior.
 * Custom resource ingredients can additionally expose raw resource-stat values
 * used by SharedLabratory::getWeightedValue.</p>
 */
public final class ComponentInstance {
    private final String id;
    private final String templateId;
    private final String serial;
    private final int uses;
    private final ComponentOrigin origin;
    private final List<ComponentProperty> properties;
    private final Map<ResourceStat, Integer> resourceStats;

    public ComponentInstance(
            String id,
            String templateId,
            String serial,
            int uses,
            ComponentOrigin origin,
            List<ComponentProperty> properties) {
        this(id, templateId, serial, uses, origin, properties,
                Collections.<ResourceStat, Integer>emptyMap());
    }

    public ComponentInstance(
            String id,
            String templateId,
            String serial,
            int uses,
            ComponentOrigin origin,
            List<ComponentProperty> properties,
            Map<ResourceStat, Integer> resourceStats) {
        this.id = requireText(id, "id");
        this.templateId = requireText(templateId, "templateId");
        this.serial = serial == null ? "" : serial;
        if (uses < 1) throw new IllegalArgumentException("uses must be >= 1");
        if (origin == null) throw new NullPointerException("origin");
        if (properties == null) throw new NullPointerException("properties");
        if (resourceStats == null) throw new NullPointerException("resourceStats");
        this.uses = uses;
        this.origin = origin;
        this.properties = Collections.unmodifiableList(
                new ArrayList<ComponentProperty>(properties));

        EnumMap<ResourceStat, Integer> copied =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        for (Map.Entry<ResourceStat, Integer> entry : resourceStats.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new NullPointerException("custom ingredient resource stat");
            }
            int value = entry.getValue().intValue();
            if (value < 0 || value > 1000) {
                throw new IllegalArgumentException(
                        "Custom ingredient resource stat outside [0,1000]: "
                        + entry.getKey() + "=" + value);
            }
            copied.put(entry.getKey(), Integer.valueOf(value));
        }
        this.resourceStats = Collections.unmodifiableMap(copied);
    }

    public String getId() { return id; }
    public String getTemplateId() { return templateId; }
    public String getSerial() { return serial; }
    public int getUses() { return uses; }
    public ComponentOrigin getOrigin() { return origin; }
    public List<ComponentProperty> getProperties() { return properties; }
    public Map<ResourceStat, Integer> getResourceStats() { return resourceStats; }

    public boolean isCustomResourceIngredient() {
        return !resourceStats.isEmpty();
    }

    public int getResourceStat(ResourceStat stat) {
        Integer value = resourceStats.get(stat);
        return value == null ? 0 : value.intValue();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
