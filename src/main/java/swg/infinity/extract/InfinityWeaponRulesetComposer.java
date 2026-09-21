package swg.infinity.extract;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.extract.WeaponTangibleTemplateExtractor.WeaponObjectTemplate;

/**
 * Composes draft-schematic extraction with tangible weapon-template extraction
 * into the effective immutable Infinity weapon ruleset used at runtime.
 *
 * <p>The composer never infers EXACT coverage. Callers must explicitly provide
 * the schematic IDs whose complete weapon path has independent accepted parity
 * evidence. Enriched-but-unadmitted weapons remain PARTIAL.</p>
 */
public final class InfinityWeaponRulesetComposer {

    public InfinityRuleset compose(
            InfinityRuleset draftRuleset,
            Map<String, WeaponObjectTemplate> tangibleTemplates,
            Set<String> exactSchematicIds) {
        if (draftRuleset == null) throw new NullPointerException("draftRuleset");
        if (tangibleTemplates == null) throw new NullPointerException("tangibleTemplates");
        if (exactSchematicIds == null) throw new NullPointerException("exactSchematicIds");

        Set<String> admitted = Collections.unmodifiableSet(
                new LinkedHashSet<String>(exactSchematicIds));
        List<SchematicDefinition> schematics =
                new ArrayList<SchematicDefinition>();
        List<CoverageRecord> coverage =
                new ArrayList<CoverageRecord>();

        for (SchematicDefinition draft : draftRuleset.getSchematics()) {
            WeaponObjectTemplate tangible =
                    tangibleTemplates.get(draft.getTargetTemplate());
            if (tangible == null
                    || tangible.getExperimentalProperties().isEmpty()) {
                schematics.add(draft);
                CoverageRecord existing =
                        draftRuleset.getCoverage(draft.getId());
                coverage.add(existing == null
                        ? partial(draft.getId(), false)
                        : existing);
                continue;
            }

            SchematicDefinition effective = new SchematicDefinition(
                    draft.getId(),
                    draft.getDisplayName(),
                    draft.getDraftTemplate(),
                    draft.getTargetTemplate(),
                    draft.getLaboratory(),
                    draft.getAssemblySkill(),
                    draft.getExperimentationSkill(),
                    draft.getSlots(),
                    tangible.getExperimentalProperties(),
                    "weapon",
                    draft.getProvenance());
            schematics.add(effective);

            boolean exact = admitted.contains(draft.getId());
            coverage.add(exact
                    ? exact(draft.getId())
                    : partial(draft.getId(), true));
        }

        RulesetManifest sourceManifest = draftRuleset.getManifest();
        String hash = hash(schematics);
        RulesetManifest manifest = new RulesetManifest(
                sourceManifest.getSchemaVersion(),
                sourceManifest.getRepository(),
                sourceManifest.getCommit(),
                sourceManifest.getExtractorVersion() + "+weapon-tangible-v1",
                hash,
                sourceManifest.getSwgAideServerId());

        return new InfinityRuleset(manifest, schematics, coverage);
    }

    private CoverageRecord exact(String id) {
        return new CoverageRecord(
                id,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                EvidenceState.EXACT,
                Collections.singletonList("weapon-functional:" + id));
    }

    private CoverageRecord partial(String id, boolean enriched) {
        List<String> fixtures = enriched
                ? Collections.singletonList("weapon-tangible-extracted:" + id)
                : Collections.<String>emptyList();
        return new CoverageRecord(
                id,
                enriched ? EvidenceState.EXACT : EvidenceState.PARTIAL,
                EvidenceState.PARTIAL,
                EvidenceState.PARTIAL,
                EvidenceState.PARTIAL,
                fixtures);
    }

    private String hash(List<SchematicDefinition> schematics) {
        StringBuilder canonical = new StringBuilder();
        for (SchematicDefinition schematic : schematics) {
            canonical.append(schematic.getId()).append('|')
                    .append(schematic.getDraftTemplate()).append('|')
                    .append(schematic.getTargetTemplate()).append('|')
                    .append(schematic.getProcessorId()).append('\n');
            for (ExperimentalProperty property : schematic.getProperties()) {
                canonical.append(property.getAttribute()).append('|')
                        .append(property.getGroup()).append('|')
                        .append(property.getMinValue()).append('|')
                        .append(property.getMaxValue()).append('|')
                        .append(property.getPrecision()).append('|')
                        .append(property.getCombineType().name()).append('|');
                for (PropertyWeight weight : property.getWeights()) {
                    canonical.append(weight.getStat().name()).append(':')
                            .append(weight.getRawWeight()).append(':')
                            .append(weight.getNormalizedWeight()).append(',');
                }
                canonical.append('\n');
            }
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    canonical.toString().getBytes("UTF-8"));
            StringBuilder out = new StringBuilder();
            for (byte value : bytes) {
                out.append(String.format("%02x", value & 0xff));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash effective ruleset", e);
        }
    }
}
