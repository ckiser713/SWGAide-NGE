package swg.infinity.integration;

/** Immutable mapping between SWGAide navigation identity and Infinity rules identity. */
public final class SchematicBinding {
    private final int swgAideServerId;
    private final int swgAideSchematicId;
    private final String infinitySchematicId;
    private final BindingState state;
    private final String evidence;

    public SchematicBinding(
            int swgAideServerId,
            int swgAideSchematicId,
            String infinitySchematicId,
            BindingState state,
            String evidence) {
        if (swgAideServerId <= 0) {
            throw new IllegalArgumentException("swgAideServerId must be > 0");
        }
        if (swgAideSchematicId <= 0) {
            throw new IllegalArgumentException("swgAideSchematicId must be > 0");
        }
        if (state == null) throw new NullPointerException("state");
        if (state.isRunnable()
                && (infinitySchematicId == null
                    || infinitySchematicId.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    "Runnable binding requires infinitySchematicId");
        }
        this.swgAideServerId = swgAideServerId;
        this.swgAideSchematicId = swgAideSchematicId;
        this.infinitySchematicId =
                infinitySchematicId == null ? "" : infinitySchematicId;
        this.state = state;
        this.evidence = evidence == null ? "" : evidence;
    }

    public int getSwgAideServerId() { return swgAideServerId; }
    public int getSwgAideSchematicId() { return swgAideSchematicId; }
    public String getInfinitySchematicId() { return infinitySchematicId; }
    public BindingState getState() { return state; }
    public String getEvidence() { return evidence; }
}
