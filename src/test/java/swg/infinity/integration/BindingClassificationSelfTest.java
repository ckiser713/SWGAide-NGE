package swg.infinity.integration;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.contracts.RulesetManifest;

/**
 * Exercises the multi-signal structural binding classification:
 * <ul>
 *   <li>VERIFIED when exactly one Infinity schematic aligns;</li>
 *   <li>AMBIGUOUS when multiple align (evidence lists candidates);</li>
 *   <li>MISSING when none align;</li>
 *   <li>MANUAL_OVERRIDE requires evidence + operatorSignature;</li>
 *   <li>{@link BindingState#AMBIGUOUS} is never runnable in the
 *       runtime registry.</li>
 *   <li>{@link BindingWriter} emits JSON admitted=false when any
 *       non-runnable binding is present, and the schema-required
 *       fields are present in every binding record.</li>
 * </ul>
 */
public final class BindingClassificationSelfTest {
    private BindingClassificationSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) {
        testVerifiedSingleMatch();
        testAmbiguousMultiMatch();
        testMissingNoMatch();
        testManualOverrideRequiresEvidenceAndSignature();
        testAmbiguousNeverRunnable();
        testWriterEmitsAllRequiredFields();
        testWriterFlagsNonRunnableAsNotAdmitted();
        System.out.println("BindingClassificationSelfTest PASS");
    }

    private static InfinityRuleset makeRuleset() {
        Provenance prov = new Provenance(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "MMOCoreORB/bin/scripts/object/draft_schematic/weapon");
        // Two structurally identical schematics to force AMBIGUOUS.
        SchematicDefinition a = new SchematicDefinition(
                "pistol_blaster_dl44", "object_draft_schematic_weapon_pistol_blaster_dl44",
                "pistol_blaster_dl44",
                "object/weapon/ranged/pistol/pistol_dl44.iff",
                LaboratoryType.RESOURCE,
                "weapon", "weapon",
                Arrays.asList(
                        slot(0, SlotKind.RESOURCE, "steel", 100, 100, prov),
                        slot(1, SlotKind.RESOURCE, "steel", 100, 100, prov),
                        slot(2, SlotKind.RESOURCE, "copper", 50, 80, prov),
                        slot(3, SlotKind.RESOURCE, "polymer", 25, 60, prov),
                        slot(4, SlotKind.MIXED_COMPONENT, "scope", 1, 100, prov),
                        slot(5, SlotKind.IDENTICAL_COMPONENT, "barrel", 1, 100, prov)),
                Collections.<swg.infinity.contracts.ExperimentalProperty>emptyList(),
                "weapon-result-processor", prov);
        SchematicDefinition b = new SchematicDefinition(
                "pistol_blaster_dl44_clone", "object_draft_schematic_weapon_pistol_blaster_dl44_clone",
                "pistol_blaster_dl44_clone",
                "object/weapon/ranged/pistol/pistol_dl44.iff",
                LaboratoryType.RESOURCE,
                "weapon", "weapon",
                Arrays.asList(
                        slot(0, SlotKind.RESOURCE, "steel", 100, 100, prov),
                        slot(1, SlotKind.RESOURCE, "steel", 100, 100, prov),
                        slot(2, SlotKind.RESOURCE, "copper", 50, 80, prov),
                        slot(3, SlotKind.RESOURCE, "polymer", 25, 60, prov),
                        slot(4, SlotKind.MIXED_COMPONENT, "scope", 1, 100, prov),
                        slot(5, SlotKind.IDENTICAL_COMPONENT, "barrel", 1, 100, prov)),
                Collections.<swg.infinity.contracts.ExperimentalProperty>emptyList(),
                "weapon-result-processor", prov);
        SchematicDefinition different = new SchematicDefinition(
                "rifle_berserker", "object_draft_schematic_weapon_rifle_berserker",
                "rifle_berserker",
                "object/weapon/ranged/rifle/rifle_berserker.iff",
                LaboratoryType.RESOURCE,
                "weapon", "weapon",
                Arrays.asList(
                        slot(0, SlotKind.RESOURCE, "steel", 100, 100, prov),
                        slot(1, SlotKind.RESOURCE, "aluminum", 50, 80, prov),
                        slot(2, SlotKind.RESOURCE, "polymer", 25, 60, prov),
                        slot(3, SlotKind.MIXED_COMPONENT, "scope", 1, 100, prov)),
                Collections.<swg.infinity.contracts.ExperimentalProperty>emptyList(),
                "weapon-result-processor", prov);
        List<SchematicDefinition> schematics = new ArrayList<SchematicDefinition>();
        schematics.add(a);
        schematics.add(b);
        schematics.add(different);
        List<CoverageRecord> coverage = new ArrayList<CoverageRecord>();
        for (SchematicDefinition s : schematics) {
            coverage.add(new CoverageRecord(s.getId(),
                    EvidenceState.SIMULATED,
                    EvidenceState.UNSUPPORTED,
                    EvidenceState.UNSUPPORTED,
                    EvidenceState.UNSUPPORTED,
                    new ArrayList<String>()));
        }
        RulesetManifest mfst = new RulesetManifest(
                1, "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "T5-test-ruleset",
                "fake-ruleset-hash", 154);
        return new InfinityRuleset(mfst, schematics, coverage);
    }

    private static IngredientSlotDefinition slot(
            int index, SlotKind kind, String acceptedType,
            int quantity, double contribution, Provenance prov) {
        return new IngredientSlotDefinition(
                index, "slot_" + index, kind, acceptedType,
                quantity, contribution, prov);
    }

    private static void testVerifiedSingleMatch() {
        InfinityRuleset ruleset = makeRuleset();
        // Only one rule has these kinds in this order.
        SchematicCatalogEntry entry = SchematicCatalogEntry.basic(
                154, 1001, "Berserker Rifle",
                4, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "MIXED_COMPONENT"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Collections.singletonList(entry), ruleset);
        if (bindings.size() != 1) {
            throw new AssertionError("expected 1 binding, got " + bindings.size());
        }
        SchematicBinding b = bindings.get(0);
        if (b.getState() != BindingState.VERIFIED) {
            throw new AssertionError("expected VERIFIED, got " + b.getState());
        }
        if (!"rifle_berserker".equals(b.getInfinitySchematicId())) {
            throw new AssertionError(
                    "expected rifle_berserker, got "
                    + b.getInfinitySchematicId());
        }
    }

    private static void testAmbiguousMultiMatch() {
        InfinityRuleset ruleset = makeRuleset();
        // Same fingerprint as both pistol_blaster_dl44 and pistol_blaster_dl44_clone.
        SchematicCatalogEntry entry = SchematicCatalogEntry.basic(
                154, 1002, "DL44 (clone)",
                6, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "RESOURCE",
                        "MIXED_COMPONENT", "IDENTICAL_COMPONENT"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Collections.singletonList(entry), ruleset);
        if (bindings.size() != 1) {
            throw new AssertionError("expected 1 binding, got " + bindings.size());
        }
        SchematicBinding b = bindings.get(0);
        if (b.getState() != BindingState.AMBIGUOUS) {
            throw new AssertionError("expected AMBIGUOUS, got " + b.getState());
        }
        if (!b.getEvidence().contains("pistol_blaster_dl44_clone")) {
            throw new AssertionError("evidence missing clone candidate");
        }
        if (b.getInfinitySchematicId() == null
                || !b.getInfinitySchematicId().isEmpty()) {
            throw new AssertionError("AMBIGUOUS must not pin an Infinity id");
        }
    }

    private static void testMissingNoMatch() {
        InfinityRuleset ruleset = makeRuleset();
        SchematicCatalogEntry entry = SchematicCatalogEntry.basic(
                154, 1003, "Mystery Schematic",
                7, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "RESOURCE",
                        "RESOURCE", "RESOURCE", "MIXED_COMPONENT"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Collections.singletonList(entry), ruleset);
        SchematicBinding b = bindings.get(0);
        if (b.getState() != BindingState.MISSING) {
            throw new AssertionError("expected MISSING, got " + b.getState());
        }
    }

    private static void testManualOverrideRequiresEvidenceAndSignature() {
        boolean rejectedEmptyEvidence = false;
        try {
            new BindingOverride(154, 2000, "pistol_blaster_dl44",
                    "", "operator1@example");
        } catch (IllegalArgumentException expected) {
            rejectedEmptyEvidence = true;
        }
        if (!rejectedEmptyEvidence) {
            throw new AssertionError("empty evidence accepted on override");
        }
        boolean rejectedEmptySignature = false;
        try {
            new BindingOverride(154, 2000, "pistol_blaster_dl44",
                    "matched by hand", "");
        } catch (IllegalArgumentException expected) {
            rejectedEmptySignature = true;
        }
        if (!rejectedEmptySignature) {
            throw new AssertionError("empty signature accepted on override");
        }
        BindingOverride ok = new BindingOverride(
                154, 2000, "pistol_blaster_dl44",
                "matched by hand", "operator1@example");
        SchematicBinding b = ok.toBinding();
        if (b.getState() != BindingState.MANUAL_OVERRIDE) {
            throw new AssertionError("override state must be MANUAL_OVERRIDE");
        }
        if (!b.getEvidence().contains("operator1@example")) {
            throw new AssertionError("evidence must include operator");
        }
    }

    private static void testAmbiguousNeverRunnable() {
        InfinityRuleset ruleset = makeRuleset();
        SchematicCatalogEntry ambiguous = SchematicCatalogEntry.basic(
                154, 3000, "DL44 (clone)",
                6, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "RESOURCE",
                        "MIXED_COMPONENT", "IDENTICAL_COMPONENT"));
        SchematicCatalogEntry verified = SchematicCatalogEntry.basic(
                154, 3001, "Berserker Rifle",
                4, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "MIXED_COMPONENT"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Arrays.asList(ambiguous, verified), ruleset);
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154, bindings);
        // Even though the registry accepts AMBIGUOUS records, none of
        // them must be runnable; only VERIFIED may resolve.
        SchematicBinding got = registry.get(3000);
        if (got.getState().isRunnable()) {
            throw new AssertionError("AMBIGUOUS is runnable");
        }
        SchematicBinding ok = registry.get(3001);
        if (!ok.getState().isRunnable()) {
            throw new AssertionError("VERIFIED should be runnable");
        }
        boolean admitted = BindingWriter.isFullyRunnable(bindings);
        if (admitted) {
            throw new AssertionError("registry with AMBIGUOUS must not be admitted");
        }
    }

    private static void testWriterEmitsAllRequiredFields() {
        InfinityRuleset ruleset = makeRuleset();
        SchematicCatalogEntry verified = SchematicCatalogEntry.basic(
                154, 4000, "Berserker Rifle",
                4, Arrays.asList(
                        "RESOURCE", "RESOURCE", "RESOURCE", "MIXED_COMPONENT"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Collections.singletonList(verified), ruleset);
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154, bindings);
        StringWriter w = new StringWriter();
        try {
            BindingWriter.write(registry,
                    "6b6ac3726aaa3c293fca83911850d3a02e2adb4f", w);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        String json = w.toString();
        for (String required : new String[] {
                "\"schemaVersion\": 1",
                "\"serverId\": 154",
                "\"rulesetCommit\": \"6b6ac3726aaa3c293fca83911850d3a02e2adb4f\"",
                "\"swgAideSchematicId\": 4000",
                "\"infinitySchematicId\": \"rifle_berserker\"",
                "\"state\": \"VERIFIED\"",
                "\"evidence\": \"multi-signal fingerprint match\"",
                "\"admitted\": true" }) {
            if (!json.contains(required)) {
                throw new AssertionError(
                        "writer output missing required field: " + required);
            }
        }
    }

    private static void testWriterFlagsNonRunnableAsNotAdmitted() {
        InfinityRuleset ruleset = makeRuleset();
        SchematicCatalogEntry missing = SchematicCatalogEntry.basic(
                154, 5000, "Mystery",
                99, Arrays.asList("RESOURCE"));
        List<SchematicBinding> bindings = BindingBuilder.build(
                Collections.singletonList(missing), ruleset);
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154, bindings);
        StringWriter w = new StringWriter();
        try {
            BindingWriter.write(registry,
                    "6b6ac3726aaa3c293fca83911850d3a02e2adb4f", w);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        String json = w.toString();
        if (!json.contains("\"admitted\": false")) {
            throw new AssertionError("expected admitted=false");
        }
        if (!json.contains("\"state\": \"MISSING\"")) {
            throw new AssertionError("expected MISSING state in output");
        }
    }
}
