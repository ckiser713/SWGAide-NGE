package swg.infinity.planning;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Resource units and component uses required for a scenario quantity. */
public final class MaterialPlan {
    private final int craftCount;
    private final Map<String, Long> resourceUnits;
    private final Map<String, Long> componentUses;

    public MaterialPlan(
            int craftCount,
            Map<String, Long> resourceUnits,
            Map<String, Long> componentUses) {
        if (craftCount < 1) throw new IllegalArgumentException("craftCount must be >= 1");
        if (resourceUnits == null) throw new NullPointerException("resourceUnits");
        if (componentUses == null) throw new NullPointerException("componentUses");
        this.craftCount = craftCount;
        this.resourceUnits = Collections.unmodifiableMap(
                new LinkedHashMap<String, Long>(resourceUnits));
        this.componentUses = Collections.unmodifiableMap(
                new LinkedHashMap<String, Long>(componentUses));
    }

    public int getCraftCount() { return craftCount; }
    public Map<String, Long> getResourceUnits() { return resourceUnits; }
    public Map<String, Long> getComponentUses() { return componentUses; }
}
