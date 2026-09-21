package swg.infinity.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;

/**
 * Builds SWGAide-to-Infinity schematic bindings using the
 * multi-signal structural {@link BindingFingerprint}.
 *
 * <p>Behaviour per SWGAide catalog entry:</p>
 * <ul>
 *   <li>Exactly one Infinity schematic aligns with the SWGAide
 *       fingerprint on all primary signals → {@link BindingState#VERIFIED}.</li>
 *   <li>Multiple Infinity schematics align → {@link BindingState#AMBIGUOUS}.
 *       The evidence field lists every candidate id so the operator
 *       can disambiguate by override.</li>
 *   <li>No Infinity schematic aligns → {@link BindingState#MISSING}.</li>
 * </ul>
 *
 * <p>The output ordering follows the catalog's iteration order.
 * Bindings are immutable; consumers should treat them as records.</p>
 */
public final class BindingBuilder {

    private BindingBuilder() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Computes the structural fingerprint for an Infinity schematic.
     */
    public static BindingFingerprint fingerprintOf(
            SchematicDefinition definition) {
        if (definition == null) throw new NullPointerException("definition");
        List<String> kinds = new ArrayList<String>();
        for (IngredientSlotDefinition slot : definition.getSlots()) {
            if (slot == null) continue;
            SlotKind k = slot.getKind();
            if (k != null) kinds.add(k.name());
        }
        List<String> groups = new ArrayList<String>();
        // Infinity rules do not include experiment-group titles in the
        // contract; an empty list is the correct structural fingerprint.
        return BindingFingerprint.compute(
                definition.getSlots().size(), kinds, groups,
                definition.getTargetTemplate());
    }

    /**
     * Computes the structural fingerprint for a SWGAide catalog entry.
     */
    public static BindingFingerprint fingerprintOf(
            SchematicCatalogEntry entry) {
        if (entry == null) throw new NullPointerException("entry");
        return BindingFingerprint.compute(
                entry.getResourceSlotCount(),
                entry.getResourceSlotKinds(),
                entry.getExperimentGroupTitles(),
                entry.getTargetTemplateHint());
    }

    /**
     * Builds a binding for each catalog entry against the supplied
     * Infinity ruleset.
     */
    public static List<SchematicBinding> build(
            List<SchematicCatalogEntry> catalog,
            InfinityRuleset ruleset) {
        if (catalog == null) throw new NullPointerException("catalog");
        if (ruleset == null) throw new NullPointerException("ruleset");

        // Pre-compute Infinity fingerprints, ordered for determinism.
        Map<String, BindingFingerprint> infinityFingerprints =
                new LinkedHashMap<String, BindingFingerprint>();
        for (SchematicDefinition def : ruleset.getSchematics()) {
            if (def == null || def.getId() == null) continue;
            infinityFingerprints.put(
                    def.getId(), fingerprintOf(def));
        }

        List<SchematicBinding> bindings = new ArrayList<SchematicBinding>();
        for (SchematicCatalogEntry entry : catalog) {
            if (entry == null) continue;
            BindingFingerprint target = fingerprintOf(entry);
            List<String> aligned = new ArrayList<String>();
            for (Map.Entry<String, BindingFingerprint> cand
                    : infinityFingerprints.entrySet()) {
                if (target.alignsWith(cand.getValue())) {
                    aligned.add(cand.getKey());
                }
            }
            Collections.sort(aligned);
            if (aligned.size() == 1) {
                bindings.add(new SchematicBinding(
                        entry.getSwgAideServerId(),
                        entry.getSwgAideSchematicId(),
                        aligned.get(0),
                        BindingState.VERIFIED,
                        "multi-signal fingerprint match"));
            } else if (aligned.size() > 1) {
                String evidence = "ambiguous candidates="
                        + aligned.toString()
                        + "; fingerprint=" + target.getHash().substring(0, 16);
                bindings.add(new SchematicBinding(
                        entry.getSwgAideServerId(),
                        entry.getSwgAideSchematicId(),
                        "",
                        BindingState.AMBIGUOUS,
                        evidence));
            } else {
                bindings.add(new SchematicBinding(
                        entry.getSwgAideServerId(),
                        entry.getSwgAideSchematicId(),
                        "",
                        BindingState.MISSING,
                        "fingerprint=" + target.getHash().substring(0, 16)
                        + " matched no Infinity schematic"));
            }
        }
        return bindings;
    }
}
