package swg.infinity.contracts;

/** Infinity AttributesMap ingredient-combine behavior. */
public enum CombineType {
    RESOURCE(0),
    LINEAR(1),
    PERCENTAGE(2),
    BITSET(3),
    OVERRIDE(4),
    LIMITED(5);

    private final int sourceId;

    CombineType(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public static CombineType fromSourceId(int id) {
        for (CombineType value : values()) {
            if (value.sourceId == id) return value;
        }
        throw new IllegalArgumentException("Unknown Infinity combine type: " + id);
    }
}
