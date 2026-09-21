package swg.infinity.contracts;
import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.RulesetManifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.rules.CraftingRuleset;

/**
 * Immutable normalized SWG Infinity ruleset.
 *
 * <p>InfinityRuleset is the first concrete server module implementing the
 * provider-neutral CraftingRuleset identity. Rich Infinity-specific schematic
 * and coverage contracts remain in this module.</p>
 */
public final class InfinityRuleset implements CraftingRuleset {
    private final RulesetManifest manifest;
    private final Map<String, SchematicDefinition> schematics;
    private final Map<String, CoverageRecord> coverage;

    public InfinityRuleset(
            RulesetManifest manifest,
            List<SchematicDefinition> schematics,
            List<CoverageRecord> coverage) {
        if (manifest == null) throw new NullPointerException("manifest");
        if (schematics == null) throw new NullPointerException("schematics");
        if (coverage == null) throw new NullPointerException("coverage");
        this.manifest = manifest;

        Map<String, SchematicDefinition> byId =
                new LinkedHashMap<String, SchematicDefinition>();
        for (SchematicDefinition schematic : schematics) {
            if (schematic == null) throw new NullPointerException("schematic");
            if (byId.put(schematic.getId(), schematic) != null) {
                throw new IllegalArgumentException(
                        "Duplicate schematic id: " + schematic.getId());
            }
        }
        this.schematics = Collections.unmodifiableMap(byId);

        Map<String, CoverageRecord> coverageById =
                new LinkedHashMap<String, CoverageRecord>();
        for (CoverageRecord record : coverage) {
            if (record == null) throw new NullPointerException("coverage record");
            if (coverageById.put(record.getSchematicId(), record) != null) {
                throw new IllegalArgumentException(
                        "Duplicate coverage id: " + record.getSchematicId());
            }
        }
        this.coverage = Collections.unmodifiableMap(coverageById);
    }

    public RulesetManifest getManifest() { return manifest; }
    public SchematicDefinition getSchematic(String id) { return schematics.get(id); }
    public CoverageRecord getCoverage(String id) { return coverage.get(id); }

    public List<SchematicDefinition> getSchematics() {
        return Collections.unmodifiableList(
                new ArrayList<SchematicDefinition>(schematics.values()));
    }

    @Override
    public String getRulesRepository() {
        return manifest.getRepository();
    }

    @Override
    public String getRulesCommit() {
        return manifest.getCommit();
    }

    @Override
    public String getRulesetHash() {
        return manifest.getRulesetHash();
    }

    @Override
    public int getTargetServerId() {
        return manifest.getSwgAideServerId();
    }
}
