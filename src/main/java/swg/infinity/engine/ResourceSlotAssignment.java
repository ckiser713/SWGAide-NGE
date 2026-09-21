package swg.infinity.engine;

/** One resource selected for one raw-resource schematic slot. */
public final class ResourceSlotAssignment {
    private final int slotIndex;
    private final ResourceInput resource;

    public ResourceSlotAssignment(int slotIndex, ResourceInput resource) {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be >= 0");
        }
        if (resource == null) throw new NullPointerException("resource");
        this.slotIndex = slotIndex;
        this.resource = resource;
    }

    public int getSlotIndex() { return slotIndex; }
    public ResourceInput getResource() { return resource; }
}
