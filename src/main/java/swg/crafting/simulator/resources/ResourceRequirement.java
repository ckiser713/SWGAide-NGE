package swg.crafting.simulator.resources;

/** Provider-neutral raw-resource requirement for one schematic slot. */
public final class ResourceRequirement {
    private final int slotIndex;
    private final String title;
    private final String acceptedType;
    private final int quantity;

    public ResourceRequirement(
            int slotIndex,
            String title,
            String acceptedType,
            int quantity) {
        if (slotIndex < 0) throw new IllegalArgumentException("slotIndex must be >= 0");
        if (acceptedType == null || acceptedType.trim().isEmpty()) {
            throw new IllegalArgumentException("acceptedType must not be blank");
        }
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        this.slotIndex = slotIndex;
        this.title = title == null ? "" : title;
        this.acceptedType = acceptedType;
        this.quantity = quantity;
    }

    public int getSlotIndex() { return slotIndex; }
    public String getTitle() { return title; }
    public String getAcceptedType() { return acceptedType; }
    public int getQuantity() { return quantity; }
}
