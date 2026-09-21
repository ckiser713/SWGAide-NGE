package swg.crafting.simulator.persistence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Versioned simulator scenario for {@code *.craftsim.json} storage. Each
 * scenario pins the rules provider, the ruleset commit, and the ruleset
 * hash; deserialization rejects scenarios whose hash does not match the
 * caller-supplied expected hash.
 *
 * <p>Storage format version is incremented whenever the on-disk format
 * changes; older versions migrate through {@link CraftsimMigration}.</p>
 *
 * <p>This class does not touch legacy {@code SWGAide.DAT}; scenarios live
 * under {@code ~/.swgaide/craftsim/scenarios/} as a separate, isolated
 * store.</p>
 */
public final class CraftsimScenario {

    /** Current on-disk format version. */
    public static final int CURRENT_VERSION = 1;

    private final int version;
    private final String id;
    private final String rulesProviderId;
    private final String rulesetCommit;
    private final String rulesetHash;
    private final String name;
    private final long savedEpochSeconds;
    private final Map<String, String> attributes;

    public CraftsimScenario(
            String id,
            String rulesProviderId,
            String rulesetCommit,
            String rulesetHash,
            String name,
            long savedEpochSeconds,
            Map<String, String> attributes) {
        if (id == null) throw new NullPointerException("id");
        if (rulesProviderId == null) throw new NullPointerException("rulesProviderId");
        if (rulesetCommit == null || rulesetCommit.length() != 40) {
            throw new IllegalArgumentException(
                    "rulesetCommit must be a 40-character Git SHA");
        }
        if (rulesetHash == null || rulesetHash.length() != 64) {
            throw new IllegalArgumentException(
                    "rulesetHash must be a 64-character SHA-256");
        }
        if (name == null) throw new NullPointerException("name");
        if (attributes == null) throw new NullPointerException("attributes");
        this.version = CURRENT_VERSION;
        this.id = id;
        this.rulesProviderId = rulesProviderId;
        this.rulesetCommit = rulesetCommit.toLowerCase();
        this.rulesetHash = rulesetHash.toLowerCase();
        this.name = name;
        this.savedEpochSeconds = savedEpochSeconds;
        this.attributes = Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(attributes));
    }

    private CraftsimScenario(int version, CraftsimScenario base) {
        this.version = version;
        this.id = base.id;
        this.rulesProviderId = base.rulesProviderId;
        this.rulesetCommit = base.rulesetCommit;
        this.rulesetHash = base.rulesetHash;
        this.name = base.name;
        this.savedEpochSeconds = base.savedEpochSeconds;
        this.attributes = base.attributes;
    }

    public int getVersion() { return version; }
    public String getId() { return id; }
    public String getRulesProviderId() { return rulesProviderId; }
    public String getRulesetCommit() { return rulesetCommit; }
    public String getRulesetHash() { return rulesetHash; }
    public String getName() { return name; }
    public long getSavedEpochSeconds() { return savedEpochSeconds; }
    public Map<String, String> getAttributes() { return attributes; }

    /** Returns a copy of this scenario at the supplied version. */
    public CraftsimScenario atVersion(int newVersion) {
        if (newVersion < 1) {
            throw new IllegalArgumentException("newVersion must be >= 1");
        }
        return new CraftsimScenario(newVersion, this);
    }
}
