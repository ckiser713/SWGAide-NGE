package swg.infinity.integration;

/** Evidence state for mapping a SWGAide schematic to an Infinity source schematic. */
public enum BindingState {
    VERIFIED,
    MANUAL_OVERRIDE,
    AMBIGUOUS,
    MISSING;

    public boolean isRunnable() {
        return this == VERIFIED || this == MANUAL_OVERRIDE;
    }
}
