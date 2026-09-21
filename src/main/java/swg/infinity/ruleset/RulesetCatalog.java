package swg.infinity.ruleset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.contracts.InfinityRuleset;

/**
 * In-memory immutable-version catalog keyed by Infinity source commit.
 *
 * <p>Catalog insertion is explicit; no "latest file wins" behavior exists.</p>
 */
public final class RulesetCatalog {
    private final Map<String, InfinityRuleset> byCommit =
            new LinkedHashMap<String, InfinityRuleset>();

    public synchronized void add(InfinityRuleset ruleset) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        String commit = ruleset.getManifest().getCommit().toLowerCase();
        InfinityRuleset previous = byCommit.get(commit);
        if (previous != null
                && !previous.getManifest().getRulesetHash().equals(
                        ruleset.getManifest().getRulesetHash())) {
            throw new IllegalStateException(
                    "Different ruleset artifact already registered for commit "
                    + commit);
        }
        byCommit.put(commit, ruleset);
    }

    public synchronized InfinityRuleset get(String commit) {
        if (commit == null) return null;
        return byCommit.get(commit.toLowerCase());
    }

    public synchronized InfinityRuleset require(String commit) {
        InfinityRuleset ruleset = get(commit);
        if (ruleset == null) {
            throw new IllegalStateException(
                    "No admitted Infinity ruleset for commit " + commit);
        }
        return ruleset;
    }

    public synchronized List<String> commits() {
        return Collections.unmodifiableList(
                new ArrayList<String>(byCommit.keySet()));
    }
}
