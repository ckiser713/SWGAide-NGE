package swg.crafting.simulator.explain;

/** One input's normalized contribution to an experimental weighted-stat score. */
public final class InputContribution {
    private final int slotIndex;
    private final String inputId;
    private final int statValue;
    private final int quantity;
    private final double weightedStatContribution;
    private final double propertyScoreContribution;

    public InputContribution(
            int slotIndex,
            String inputId,
            int statValue,
            int quantity,
            double weightedStatContribution,
            double propertyScoreContribution) {
        if (slotIndex < 0) throw new IllegalArgumentException("slotIndex must be >= 0");
        if (inputId == null || inputId.trim().isEmpty()) {
            throw new IllegalArgumentException("inputId must not be blank");
        }
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        this.slotIndex = slotIndex;
        this.inputId = inputId;
        this.statValue = statValue;
        this.quantity = quantity;
        this.weightedStatContribution = weightedStatContribution;
        this.propertyScoreContribution = propertyScoreContribution;
    }

    public int getSlotIndex() { return slotIndex; }
    public String getInputId() { return inputId; }
    public int getStatValue() { return statValue; }
    public int getQuantity() { return quantity; }
    public double getWeightedStatContribution() { return weightedStatContribution; }
    public double getPropertyScoreContribution() { return propertyScoreContribution; }
}
