package swg.infinity.rules;

import java.util.Collections;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.RulesetManifest;

/** Dependency-free immutable-version catalog tests. */
public final class RulesetCatalogSelfTest {
    private RulesetCatalogSelfTest() {
    }

    public static void main(String[] args) {
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        InfinityRuleset one = ruleset(commit, "hash-one");
        RulesetCatalog catalog = new RulesetCatalog();
        catalog.add(one);

        if (catalog.require(commit) != one) {
            throw new AssertionError("ruleset lookup lost identity");
        }

        boolean rejected = false;
        try {
            catalog.add(ruleset(commit, "hash-two"));
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "different artifact for same source commit accepted");
        }

        ActiveRuleset active = new ActiveRuleset(catalog);
        active.select(commit);
        if (active.get() != one) {
            throw new AssertionError("active ruleset selection failed");
        }
        System.out.println("RulesetCatalogSelfTest PASS");
    }

    private static InfinityRuleset ruleset(String commit, String hash) {
        return new InfinityRuleset(
                new RulesetManifest(
                        1,
                        "swginfinity/public",
                        commit,
                        "foundation",
                        hash,
                        154),
                Collections.emptyList(),
                Collections.emptyList());
    }
}
