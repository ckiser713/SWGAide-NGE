package swg.infinity.contracts;

/**
 * Evidence state for an Infinity crafting result or coverage path.
 *
 * <p>States describe reproducible evidence quality rather than a subjective
 * confidence percentage. UNKNOWN is valid and must never be silently promoted.</p>
 */
public enum EvidenceState {
    EXACT,
    EXACT_WITH_FORCED_OUTCOME,
    SIMULATED,
    PARTIAL,
    UNSUPPORTED,
    UNKNOWN
}
