package swg.infinity.rules;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.RulesetValidator;

/**
 * Explicit admission gate for a normalized in-memory ruleset.
 *
 * <p>Parsing/transport is deliberately separate. A future JSON or generated
 * loader must produce a fully normalized InfinityRuleset and call this gate
 * before application use.</p>
 */
public final class RulesetAdmission {
    public static final String AUTHORITY_REPOSITORY = "swginfinity/public";

    private final String requiredCommit;
    private final int requiredServerId;

    public RulesetAdmission(String requiredCommit, int requiredServerId) {
        if (requiredCommit == null || !requiredCommit.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException(
                    "requiredCommit must be a 40-character Git SHA");
        }
        if (requiredServerId <= 0) {
            throw new IllegalArgumentException("requiredServerId must be > 0");
        }
        this.requiredCommit = requiredCommit.toLowerCase();
        this.requiredServerId = requiredServerId;
    }

    public InfinityRuleset admit(InfinityRuleset ruleset) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        RulesetValidator.validate(ruleset);

        if (!AUTHORITY_REPOSITORY.equals(ruleset.getManifest().getRepository())) {
            throw new IllegalStateException(
                    "Ruleset repository is not the configured Infinity authority");
        }
        if (!requiredCommit.equalsIgnoreCase(ruleset.getManifest().getCommit())) {
            throw new IllegalStateException(
                    "Ruleset commit mismatch: required=" + requiredCommit
                    + " actual=" + ruleset.getManifest().getCommit());
        }
        if (ruleset.getManifest().getSwgAideServerId() != requiredServerId) {
            throw new IllegalStateException(
                    "Ruleset server mismatch: required=" + requiredServerId
                    + " actual=" + ruleset.getManifest().getSwgAideServerId());
        }
        return ruleset;
    }

    public String getRequiredCommit() {
        return requiredCommit;
    }

    public int getRequiredServerId() {
        return requiredServerId;
    }
}
