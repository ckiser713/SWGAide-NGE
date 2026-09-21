package swg.crafting.simulator.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact observed component supplied by the user for one component slot. */
public final class ExactComponentInput {
    private final int slotIndex;
    private final String componentId;
    private final String serial;
    private final int availableUses;
    private final List<ComponentProperty> properties;

    public ExactComponentInput(
            int slotIndex,
            String componentId,
            String serial,
            int availableUses,
            List<ComponentProperty> properties) {
        if (slotIndex < 0) throw new IllegalArgumentException("slotIndex must be >= 0");
        if (componentId == null || componentId.trim().isEmpty()) {
            throw new IllegalArgumentException("componentId must not be blank");
        }
        if (availableUses < 1) {
            throw new IllegalArgumentException("availableUses must be >= 1");
        }
        if (properties == null) throw new NullPointerException("properties");
        this.slotIndex = slotIndex;
        this.componentId = componentId.trim();
        this.serial = serial == null ? "" : serial.trim();
        this.availableUses = availableUses;
        this.properties = Collections.unmodifiableList(
                new ArrayList<ComponentProperty>(properties));
    }

    public int getSlotIndex() { return slotIndex; }
    public String getComponentId() { return componentId; }
    public String getSerial() { return serial; }
    public int getAvailableUses() { return availableUses; }
    public List<ComponentProperty> getProperties() { return properties; }
}
