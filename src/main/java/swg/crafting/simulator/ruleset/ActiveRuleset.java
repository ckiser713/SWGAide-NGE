package swg.crafting.simulator.ruleset;

import swg.infinity.contracts.InfinityRuleset;

/**
 * Explicit active-ruleset pointer for application wiring.
 *
 * <p>Changing the active commit is a caller-controlled operation; saved
 * scenarios remain bound to the commit they recorded.</p>
 */
public final class ActiveRuleset {
    private final RulesetCatalog catalog;
    private String activeCommit;

    public ActiveRuleset(RulesetCatalog catalog) {
        if (catalog == null) throw new NullPointerException("catalog");
        this.catalog = catalog;
    }

    public synchronized void select(String commit) {
        InfinityRuleset ruleset = catalog.require(commit);
        activeCommit = ruleset.getManifest().getCommit().toLowerCase();
    }

    public synchronized InfinityRuleset get() {
        if (activeCommit == null) {
            throw new IllegalStateException("No active Infinity ruleset selected");
        }
        return catalog.require(activeCommit);
    }

    public synchronized String getCommit() {
        return activeCommit;
    }
}
