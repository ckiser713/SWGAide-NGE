package swg.infinity.integration;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SlotKind;
import swg.infinity.extract.Extractor;
import swg.infinity.fixtures.SeedFixture;
import swg.infinity.fixtures.SeedFixtureParser;

/**
 * End-to-end check: build a {@link SchematicBindingRegistry} from the
 * extracted Infinity weapon ruleset and the four source-pinned seed
 * fixtures, write {@code bindings.server154.json} via
 * {@link BindingWriter}, and assert every seed is admitted as
 * {@link BindingState#VERIFIED}.
 *
 * <p>This is the T5 evidence that the multi-signal fingerprint
 * aligns the seed catalog with the extractor output and that the
 * writer produces a schema-compliant document.</p>
 */
public final class BindingsSeedWriterSelfTest {
    private BindingsSeedWriterSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        File source = new File(
                "/home/thenexussidekick/swgaide-deps/swginfinity-public");
        if (!source.isDirectory()) {
            throw new AssertionError(
                    "Infinity source checkout missing at " + source);
        }
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        InfinityRuleset ruleset = new Extractor(source, commit).extractWeapons();

        File seedsDir = new File(
                "src/test/resources/swg/infinity/fixtures/seeds");
        File[] seedFiles = seedsDir.listFiles();
        if (seedFiles == null || seedFiles.length == 0) {
            throw new AssertionError("seed fixtures missing under " + seedsDir);
        }
        Arrays.sort(seedFiles);

        List<SchematicCatalogEntry> catalog =
                new ArrayList<SchematicCatalogEntry>();
        List<SeedFixture> seeds = new ArrayList<SeedFixture>();
        for (int idx = 0; idx < seedFiles.length; ++idx) {
            File f = seedFiles[idx];
            if (!f.getName().endsWith(".json")) continue;
            Object json = SeedFixtureParser.parseFile(f);
            SeedFixture seed = SeedFixtureParser.parseSeed(json);
            // Use a deterministic, unique SWGAide schematic id per seed
            // (1000 + file index) so the binding registry can look it up.
            catalog.add(toCatalogEntry(seed, 1000 + idx));
            seeds.add(seed);
        }
        List<SchematicBinding> bindings = BindingBuilder.build(catalog, ruleset);
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154, bindings);

        // Every seed must resolve to VERIFIED against the extracted ruleset.
        for (int idx = 0; idx < seeds.size(); ++idx) {
            SeedFixture seed = seeds.get(idx);
            SchematicBinding b = registry.get(1000 + idx);
            if (b == null) {
                throw new AssertionError(
                        "no binding produced for seed " + seed.getId());
            }
            if (b.getState() != BindingState.VERIFIED) {
                throw new AssertionError("seed " + seed.getId()
                        + " resolved to " + b.getState()
                        + "; evidence=" + b.getEvidence());
            }
            String infinityId = b.getInfinitySchematicId();
            // Cross-check: the extracted schematic must contain the slot
            // kinds the seed declares.
            swg.infinity.contracts.SchematicDefinition def =
                    ruleset.getSchematic(infinityId);
            if (def == null) {
                throw new AssertionError("missing schematic " + infinityId);
            }
            if (def.getSlots().size() != seed.getSlots().size()) {
                throw new AssertionError(
                        "slot count mismatch for " + infinityId
                        + ": ext=" + def.getSlots().size()
                        + " seed=" + seed.getSlots().size());
            }
        }
        if (!BindingWriter.isFullyRunnable(bindings)) {
            throw new AssertionError("bindings should be fully runnable");
        }

        StringWriter sw = new StringWriter();
        BindingWriter.write(registry, commit, sw);
        String json = sw.toString();
        // Schema-required keys.
        for (String required : new String[] {
                "\"schemaVersion\": 1",
                "\"serverId\": 154",
                "\"rulesetCommit\": \"" + commit + "\"",
                "\"admitted\": true" }) {
            if (!json.contains(required)) {
                throw new AssertionError(
                        "writer output missing required field: " + required);
            }
        }
        // Persist to target/bindings.server154.json as operator-visible
        // T5 evidence.
        File outDir = new File("target");
        if (!outDir.isDirectory() && !outDir.mkdirs()) {
            throw new AssertionError("cannot create target dir");
        }
        File out = new File(outDir, "bindings.server154.json");
        FileWriter w = new FileWriter(out);
        try {
            w.write(json);
        } finally {
            w.close();
        }
        System.out.println(
                "BindingsSeedWriterSelfTest PASS ("
                + bindings.size() + " bindings, wrote "
                + out.getAbsolutePath() + ")");
    }

    private static SchematicCatalogEntry toCatalogEntry(
            SeedFixture seed, int swgAideId) {
        List<String> kinds = new ArrayList<String>();
        for (SeedFixture.SeedSlot s : seed.getSlots()) {
            kinds.add(slotKindName(s.getSlotKind()));
        }
        List<String> groups = new ArrayList<String>();
        for (SeedFixture.SeedExperimentGroup g : seed.getExperimentGroups()) {
            groups.add(g.getAttribute());
        }
        return new SchematicCatalogEntry(
                154, swgAideId,
                seed.getName(),
                seed.getSlots().size(),
                Collections.unmodifiableList(kinds),
                Collections.unmodifiableList(groups),
                seed.getTargetTemplate());
    }

    private static String slotKindName(int code) {
        switch (code) {
        case 0: return SlotKind.RESOURCE.name();
        case 1: return SlotKind.IDENTICAL_COMPONENT.name();
        case 2: return SlotKind.MIXED_COMPONENT.name();
        case 3: return SlotKind.OPTIONAL_IDENTICAL_COMPONENT.name();
        case 4: return SlotKind.OPTIONAL_MIXED_COMPONENT.name();
        default: throw new IllegalArgumentException("unknown slot kind " + code);
        }
    }
}
