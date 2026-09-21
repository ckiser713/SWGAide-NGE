package swg.infinity.integration;

/**
 * Operator-issued override for an ambiguous or missing schematic
 * binding. Overrides are required to carry non-empty {@code evidence}
 * and {@code operatorSignature} fields; validation rejects any
 * override that omits either. The override's {@link BindingState} is
 * always {@link BindingState#MANUAL_OVERRIDE}.
 *
 * <p>Overrides are immutable. Callers may persist them, hash them,
 * or feed them directly into {@link SchematicBindingRegistry}.</p>
 */
public final class BindingOverride {
    private final int swgAideServerId;
    private final int swgAideSchematicId;
    private final String infinitySchematicId;
    private final String evidence;
    private final String operatorSignature;

    public BindingOverride(
            int swgAideServerId,
            int swgAideSchematicId,
            String infinitySchematicId,
            String evidence,
            String operatorSignature) {
        if (swgAideServerId <= 0) {
            throw new IllegalArgumentException("swgAideServerId must be > 0");
        }
        if (swgAideSchematicId <= 0) {
            throw new IllegalArgumentException(
                    "swgAideSchematicId must be > 0");
        }
        if (infinitySchematicId == null
                || infinitySchematicId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manual override requires infinitySchematicId");
        }
        if (evidence == null || evidence.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manual override requires non-empty evidence");
        }
        if (operatorSignature == null
                || operatorSignature.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Manual override requires non-empty operatorSignature");
        }
        this.swgAideServerId = swgAideServerId;
        this.swgAideSchematicId = swgAideSchematicId;
        this.infinitySchematicId = infinitySchematicId.trim();
        this.evidence = evidence.trim();
        this.operatorSignature = operatorSignature.trim();
    }

    public int getSwgAideServerId() { return swgAideServerId; }
    public int getSwgAideSchematicId() { return swgAideSchematicId; }
    public String getInfinitySchematicId() { return infinitySchematicId; }
    public String getEvidence() { return evidence; }
    public String getOperatorSignature() { return operatorSignature; }

    /** Materializes this override as a runnable binding. */
    public SchematicBinding toBinding() {
        String combinedEvidence = "MANUAL_OVERRIDE by " + operatorSignature
                + ": " + evidence;
        return new SchematicBinding(
                swgAideServerId, swgAideSchematicId,
                infinitySchematicId,
                BindingState.MANUAL_OVERRIDE,
                combinedEvidence);
    }
}
