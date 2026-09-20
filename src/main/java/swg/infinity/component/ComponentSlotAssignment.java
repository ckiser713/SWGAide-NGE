package swg.infinity.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ordered component uses supplied to one component schematic slot. */
public final class ComponentSlotAssignment {
    private final int slotIndex;
    private final List<ComponentUse> componentUses;

    public ComponentSlotAssignment(int slotIndex, List<ComponentUse> componentUses) {
        if (slotIndex < 0) throw new IllegalArgumentException("slotIndex must be >= 0");
        if (componentUses == null) throw new NullPointerException("componentUses");
        this.slotIndex = slotIndex;
        this.componentUses = Collections.unmodifiableList(
                new ArrayList<ComponentUse>(componentUses));
    }

    public int getSlotIndex() { return slotIndex; }
    public List<ComponentUse> getComponentUses() { return componentUses; }

    public int totalUses() {
        int total = 0;
        for (ComponentUse use : componentUses) total += use.getUses();
        return total;
    }
}
