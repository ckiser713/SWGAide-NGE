package swg.infinity.contracts;

/** Crafting laboratory identifiers used by SWG Infinity draft schematics. */
public enum LaboratoryType {
    RESOURCE(0),
    GENETIC(1),
    DROID(2);

    private final int sourceId;

    LaboratoryType(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public static LaboratoryType fromSourceId(int id) {
        for (LaboratoryType value : values()) {
            if (value.sourceId == id) return value;
        }
        throw new IllegalArgumentException("Unknown Infinity laboratory id: " + id);
    }
}
