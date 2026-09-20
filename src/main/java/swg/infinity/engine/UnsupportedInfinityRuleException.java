package swg.infinity.engine;

/** Raised when source behavior is known to exist but is not evidence-complete. */
public final class UnsupportedInfinityRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnsupportedInfinityRuleException(String message) {
        super(message);
    }
}
