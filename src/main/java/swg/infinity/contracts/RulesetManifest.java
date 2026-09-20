package swg.infinity.contracts;

import java.util.Objects;

/** Immutable identity for one extracted Infinity ruleset. */
public final class RulesetManifest {
    private final int schemaVersion;
    private final String repository;
    private final String commit;
    private final String extractorVersion;
    private final String rulesetHash;
    private final int swgAideServerId;

    public RulesetManifest(
            int schemaVersion,
            String repository,
            String commit,
            String extractorVersion,
            String rulesetHash,
            int swgAideServerId) {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be >= 1");
        }
        if (swgAideServerId <= 0) {
            throw new IllegalArgumentException("swgAideServerId must be > 0");
        }
        this.schemaVersion = schemaVersion;
        this.repository = requireText(repository, "repository");
        this.commit = requireText(commit, "commit");
        this.extractorVersion = requireText(extractorVersion, "extractorVersion");
        this.rulesetHash = requireText(rulesetHash, "rulesetHash");
        this.swgAideServerId = swgAideServerId;
    }

    public int getSchemaVersion() { return schemaVersion; }
    public String getRepository() { return repository; }
    public String getCommit() { return commit; }
    public String getExtractorVersion() { return extractorVersion; }
    public String getRulesetHash() { return rulesetHash; }
    public int getSwgAideServerId() { return swgAideServerId; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RulesetManifest)) return false;
        RulesetManifest that = (RulesetManifest) other;
        return schemaVersion == that.schemaVersion
                && swgAideServerId == that.swgAideServerId
                && repository.equals(that.repository)
                && commit.equals(that.commit)
                && extractorVersion.equals(that.extractorVersion)
                && rulesetHash.equals(that.rulesetHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaVersion, repository, commit, extractorVersion,
                rulesetHash, swgAideServerId);
    }
}
