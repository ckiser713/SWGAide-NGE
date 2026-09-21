package swg.crafting.simulator.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.components.ComponentInstance;
import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.components.ComponentUse;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.engine.ResourceSlotAssignment;
import swg.infinity.contracts.ResourceStat;

/**
 * Versioned simulator scenario for {@code *.craftsim.json} storage. Each
 * scenario pins the server identity, rules provider, ruleset commit, and
 * ruleset hash; deserialization rejects scenarios whose hash does not
 * match the caller-supplied expected hash.
 *
 * <p>The on-disk format stores a full deterministic snapshot: server id,
 * server name, rules provider id, ruleset commit and SHA-256, schematic
 * id, assembly outcome, resource slot assignments (including captured
 * stats, origin, and available quantity), component slot assignments
 * (instance id, template id, serial, uses, origin, and properties), and
 * experiment steps. The full stat snapshot means saved scenarios remain
 * reproducible even if a resource is no longer in SWGAide's active set.</p>
 *
 * <p>Storage format version is incremented whenever the on-disk format
 * changes; older versions migrate through {@link CraftsimMigration}.</p>
 *
 * <p>This class does not touch legacy {@code SWGAide.DAT}; scenarios live
 * under {@code <baseDir>/scenarios/<id>.craftsim.json} as a separate,
 * isolated store.</p>
 */
public final class CraftsimScenario {

    /** Current on-disk format version. */
    public static final int CURRENT_VERSION = 2;

    /** Maximum allowed length for a scenario id; longer ids are rejected. */
    public static final int MAX_ID_LENGTH = 96;

    /** Server identity metadata. */
    public static final class ServerInfo {
        private final int serverId;
        private final String serverName;
        public ServerInfo(int serverId, String serverName) {
            if (serverId <= 0) {
                throw new IllegalArgumentException("serverId must be > 0");
            }
            this.serverId = serverId;
            this.serverName = serverName == null ? "" : serverName;
        }
        public int getServerId() { return serverId; }
        public String getServerName() { return serverName; }
    }

    /** Captured resource snapshot for one raw-resource schematic slot. */
    public static final class SavedResource {
        private final int slotIndex;
        private final String name;
        private final String resourceType;
        private final Map<ResourceStat, Integer> stats;
        private final ResourceOrigin origin;
        private final long availableQuantity;

        public SavedResource(
                int slotIndex,
                String name,
                String resourceType,
                Map<ResourceStat, Integer> stats,
                ResourceOrigin origin,
                long availableQuantity) {
            if (slotIndex < 0) {
                throw new IllegalArgumentException("slotIndex must be >= 0");
            }
            if (name == null) throw new NullPointerException("name");
            if (resourceType == null) {
                throw new NullPointerException("resourceType");
            }
            if (stats == null) throw new NullPointerException("stats");
            EnumMap<ResourceStat, Integer> copied =
                    new EnumMap<ResourceStat, Integer>(ResourceStat.class);
            for (Map.Entry<ResourceStat, Integer> e : stats.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                int v = e.getValue().intValue();
                if (v < 0) v = 0;
                if (v > 1000) v = 1000;
                copied.put(e.getKey(), Integer.valueOf(v));
            }
            this.slotIndex = slotIndex;
            this.name = name;
            this.resourceType = resourceType;
            this.stats = Collections.unmodifiableMap(copied);
            this.origin = origin == null ? ResourceOrigin.MANUAL : origin;
            this.availableQuantity = availableQuantity < -1L
                    ? -1L : availableQuantity;
        }

        public int getSlotIndex() { return slotIndex; }
        public String getName() { return name; }
        public String getResourceType() { return resourceType; }
        public Map<ResourceStat, Integer> getStats() { return stats; }
        public ResourceOrigin getOrigin() { return origin; }
        public long getAvailableQuantity() { return availableQuantity; }

        public ResourceInput toInput() {
            // Class names are reconstructed from the resource type for
            // ResourceLaboratory type matching.
            java.util.Set<String> classNames = new java.util.HashSet<String>();
            classNames.add(resourceType);
            return new ResourceInput(
                    name,
                    resourceType,
                    Collections.unmodifiableSet(classNames),
                    stats,
                    availableQuantity,
                    origin);
        }
    }

    /** Captured component snapshot for one component schematic slot. */
    public static final class SavedComponentUse {
        private final String id;
        private final String templateId;
        private final String serial;
        private final int uses;
        private final ComponentOrigin origin;
        private final List<ComponentProperty> properties;

        public SavedComponentUse(
                String id,
                String templateId,
                String serial,
                int uses,
                ComponentOrigin origin,
                List<ComponentProperty> properties) {
            if (id == null) throw new NullPointerException("id");
            if (templateId == null) {
                throw new NullPointerException("templateId");
            }
            if (uses < 1) throw new IllegalArgumentException("uses must be >= 1");
            if (origin == null) throw new NullPointerException("origin");
            if (properties == null) {
                throw new NullPointerException("properties");
            }
            this.id = id;
            this.templateId = templateId;
            this.serial = serial == null ? "" : serial;
            this.uses = uses;
            this.origin = origin;
            this.properties = Collections.unmodifiableList(
                    new ArrayList<ComponentProperty>(properties));
        }

        public String getId() { return id; }
        public String getTemplateId() { return templateId; }
        public String getSerial() { return serial; }
        public int getUses() { return uses; }
        public ComponentOrigin getOrigin() { return origin; }
        public List<ComponentProperty> getProperties() { return properties; }

        public ComponentInstance toInstance() {
            return new ComponentInstance(
                    id, templateId, serial, uses, origin, properties);
        }

        public ComponentUse toUse() {
            return new ComponentUse(toInstance(), 1);
        }
    }

    /** Captured component slot — the slot index plus its uses. */
    public static final class SavedComponentSlot {
        private final int slotIndex;
        private final List<SavedComponentUse> uses;

        public SavedComponentSlot(int slotIndex, List<SavedComponentUse> uses) {
            if (slotIndex < 0) {
                throw new IllegalArgumentException("slotIndex must be >= 0");
            }
            if (uses == null) throw new NullPointerException("uses");
            this.slotIndex = slotIndex;
            this.uses = Collections.unmodifiableList(
                    new ArrayList<SavedComponentUse>(uses));
        }

        public int getSlotIndex() { return slotIndex; }
        public List<SavedComponentUse> getUses() { return uses; }

        public ComponentSlotAssignment toAssignment() {
            List<ComponentUse> out =
                    new ArrayList<ComponentUse>(uses.size());
            for (SavedComponentUse u : uses) out.add(u.toUse());
            return new ComponentSlotAssignment(
                    slotIndex,
                    Collections.unmodifiableList(out));
        }
    }

    private final int version;
    private final String id;
    private final ServerInfo server;
    private final String rulesProviderId;
    private final String rulesetCommit;
    private final String rulesetHash;
    private final String schematicId;
    private final String schematicName;
    private final CraftOutcomeTier assemblyOutcome;
    private final List<SavedResource> resources;
    private final List<SavedComponentSlot> components;
    private final List<ExperimentStep> experiments;
    private final String name;
    private final long savedEpochSeconds;

    public CraftsimScenario(
            String id,
            ServerInfo server,
            String rulesProviderId,
            String rulesetCommit,
            String rulesetHash,
            String schematicId,
            String schematicName,
            CraftOutcomeTier assemblyOutcome,
            List<SavedResource> resources,
            List<SavedComponentSlot> components,
            List<ExperimentStep> experiments,
            String name,
            long savedEpochSeconds) {
        validateId(id);
        if (server == null) throw new NullPointerException("server");
        if (rulesProviderId == null) {
            throw new NullPointerException("rulesProviderId");
        }
        if (rulesetCommit == null || rulesetCommit.length() != 40) {
            throw new IllegalArgumentException(
                    "rulesetCommit must be a 40-character Git SHA");
        }
        if (rulesetHash == null || rulesetHash.length() != 64) {
            throw new IllegalArgumentException(
                    "rulesetHash must be a 64-character SHA-256");
        }
        if (schematicId == null) {
            throw new NullPointerException("schematicId");
        }
        if (assemblyOutcome == null) {
            throw new NullPointerException("assemblyOutcome");
        }
        if (resources == null) throw new NullPointerException("resources");
        if (components == null) throw new NullPointerException("components");
        if (experiments == null) throw new NullPointerException("experiments");
        if (name == null) throw new NullPointerException("name");
        this.version = CURRENT_VERSION;
        this.id = id;
        this.server = server;
        this.rulesProviderId = rulesProviderId;
        this.rulesetCommit = rulesetCommit.toLowerCase();
        this.rulesetHash = rulesetHash.toLowerCase();
        this.schematicId = schematicId;
        this.schematicName = schematicName == null ? "" : schematicName;
        this.assemblyOutcome = assemblyOutcome;
        this.resources = Collections.unmodifiableList(
                new ArrayList<SavedResource>(resources));
        this.components = Collections.unmodifiableList(
                new ArrayList<SavedComponentSlot>(components));
        this.experiments = Collections.unmodifiableList(
                new ArrayList<ExperimentStep>(experiments));
        this.name = name;
        this.savedEpochSeconds = savedEpochSeconds;
    }

    private CraftsimScenario(int version, CraftsimScenario base) {
        this.version = version;
        this.id = base.id;
        this.server = base.server;
        this.rulesProviderId = base.rulesProviderId;
        this.rulesetCommit = base.rulesetCommit;
        this.rulesetHash = base.rulesetHash;
        this.schematicId = base.schematicId;
        this.schematicName = base.schematicName;
        this.assemblyOutcome = base.assemblyOutcome;
        this.resources = base.resources;
        this.components = base.components;
        this.experiments = base.experiments;
        this.name = base.name;
        this.savedEpochSeconds = base.savedEpochSeconds;
    }

    /**
     * Validates a scenario id. Rejects empty, too-long, or path-traversal
     * ids that could escape the scenarios/ directory.
     */
    public static void validateId(String id) {
        if (id == null) throw new NullPointerException("id");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id must not be empty");
        }
        if (id.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException(
                    "id must not exceed " + MAX_ID_LENGTH + " chars");
        }
        if (id.contains("..") || id.contains("/") || id.contains("\\")) {
            throw new IllegalArgumentException(
                    "id must not contain path separators or '..'");
        }
        if (id.startsWith(".") || id.startsWith(" ")) {
            throw new IllegalArgumentException(
                    "id must not start with '.' or space");
        }
        // Reject NUL and control chars
        for (int i = 0; i < id.length(); ++i) {
            char c = id.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                throw new IllegalArgumentException(
                        "id contains control character");
            }
        }
    }

    public int getVersion() { return version; }
    public String getId() { return id; }
    public ServerInfo getServer() { return server; }
    public String getRulesProviderId() { return rulesProviderId; }
    public String getRulesetCommit() { return rulesetCommit; }
    public String getRulesetHash() { return rulesetHash; }
    public String getSchematicId() { return schematicId; }
    public String getSchematicName() { return schematicName; }
    public CraftOutcomeTier getAssemblyOutcome() { return assemblyOutcome; }
    public List<SavedResource> getResources() { return resources; }
    public List<SavedComponentSlot> getComponents() { return components; }
    public List<ExperimentStep> getExperiments() { return experiments; }
    public String getName() { return name; }
    public long getSavedEpochSeconds() { return savedEpochSeconds; }

    /** Returns a copy of this scenario at the supplied version. */
    public CraftsimScenario atVersion(int newVersion) {
        if (newVersion < 1) {
            throw new IllegalArgumentException("newVersion must be >= 1");
        }
        return new CraftsimScenario(newVersion, this);
    }

    /** Convenience: list of {@link ResourceSlotAssignment} for the engine. */
    public List<ResourceSlotAssignment> toResourceAssignments() {
        List<ResourceSlotAssignment> out =
                new ArrayList<ResourceSlotAssignment>(resources.size());
        for (SavedResource r : resources) {
            out.add(new ResourceSlotAssignment(r.slotIndex, r.toInput()));
        }
        return out;
    }

    /** Convenience: list of {@link ComponentSlotAssignment} for the engine. */
    public List<ComponentSlotAssignment> toComponentAssignments() {
        List<ComponentSlotAssignment> out =
                new ArrayList<ComponentSlotAssignment>(components.size());
        for (SavedComponentSlot c : components) {
            out.add(c.toAssignment());
        }
        return out;
    }

    /** Returns the legacy v1 attributes map reconstructed from this scenario. */
    public Map<String, String> legacyAttributes() {
        Map<String, String> out = new LinkedHashMap<String, String>();
        out.put("schematic", schematicId);
        out.put("experiment", experiments.isEmpty()
                ? "" : experiments.get(0).getGroup());
        return out;
    }
}
