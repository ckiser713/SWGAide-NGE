package swg.crafting.simulator.rules;

/**
 * Strength of a server-rules-provider match.
 *
 * <p>Exact server modules always outrank verified family modules. A family
 * provider must be explicitly verified; broad SWGAide type labels alone do not
 * create a match.</p>
 */
public enum ServerRulesMatch {
    NONE(0),
    VERIFIED_FAMILY(1),
    EXACT_SERVER(2);

    private final int priority;

    ServerRulesMatch(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
