package swg.infinity.contracts;

/** Effective ingredient-slot contract extracted from an Infinity draft schematic. */
public final class IngredientSlotDefinition {
    private final int index;
    private final String title;
    private final SlotKind kind;
    private final String acceptedType;
    private final int quantity;
    private final double contribution;
    private final Provenance provenance;

    public IngredientSlotDefinition(
            int index,
            String title,
            SlotKind kind,
            String acceptedType,
            int quantity,
            double contribution,
            Provenance provenance) {
        if (index < 0) throw new IllegalArgumentException("index must be >= 0");
        if (kind == null) throw new NullPointerException("kind");
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        if (Double.isNaN(contribution)
                || Double.isInfinite(contribution)
                || contribution < 0.0) {
            throw new IllegalArgumentException("contribution must be finite and >= 0");
        }
        if (provenance == null) throw new NullPointerException("provenance");
        this.index = index;
        this.title = title == null ? "" : title;
        this.kind = kind;
        this.acceptedType = requireText(acceptedType, "acceptedType");
        this.quantity = quantity;
        this.contribution = contribution;
        this.provenance = provenance;
    }

    public int getIndex() { return index; }
    public String getTitle() { return title; }
    public SlotKind getKind() { return kind; }
    public String getAcceptedType() { return acceptedType; }
    public int getQuantity() { return quantity; }
    public double getContribution() { return contribution; }
    public Provenance getProvenance() { return provenance; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
