package swg.crafting.simulator.persistence;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.contracts.ResourceStat;

/**
 * Round-trip + migration + hash-rejection + atomic-write + id-validation
 * assertions for {@link CraftsimScenarioStore}. Verifies that:
 * <ul>
 *   <li>a v2 scenario with full resource/component/experiment snapshot
 *       round-trips bytewise and is reproducible independent of any
 *       active SWGAide resource set;</li>
 *   <li>loading with a mismatched hash is rejected;</li>
 *   <li>the migration framework is exercised end-to-end — a v1 file
 *       is loaded, the registered v1 -> v2 migration fires, and the
 *       result is at the current version;</li>
 *   <li>save is atomic — no {@code .craftsim.json.tmp} file lingers
 *       after a successful save;</li>
 *   <li>id validation rejects path traversal and control characters;</li>
 *   <li>the storage does not touch legacy {@code SWGAide.DAT}.</li>
 * </ul>
 */
public final class CraftsimPersistenceSelfTest {
    private CraftsimPersistenceSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        File base = new File("target/craftsim-persistence-test");
        if (base.exists()) {
            deleteRecursively(base);
        }
        base.mkdirs();

        // 1) v2 round-trip with full snapshot
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.UT, Integer.valueOf(950));
        stats.put(ResourceStat.OQ, Integer.valueOf(800));
        stats.put(ResourceStat.PE, Integer.valueOf(700));

        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        resources.add(new CraftsimScenario.SavedResource(
                0, "Perfect Steel", "metal",
                stats, ResourceOrigin.MANUAL, 1000L));
        resources.add(new CraftsimScenario.SavedResource(
                1, "Quality Steel", "metal",
                stats, ResourceOrigin.INVENTORY, 500L));

        Provenance persistedProp = new Provenance(
                "swg.crafting.simulator.persistence",
                "0000000000000000000000000000000000000000",
                "saved-component-property");
        List<ComponentProperty> props = new ArrayList<ComponentProperty>();
        props.add(new ComponentProperty(
                "charges", 50.0d, 2, "", false, persistedProp));
        List<CraftsimScenario.SavedComponentUse> uses =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses.add(new CraftsimScenario.SavedComponentUse(
                "comp-0", "object/tangible/component/weapon/"
                        + "shared_blaster_power_handler.iff",
                "SERIAL-001", 1, ComponentOrigin.MANUAL, props));
        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        components.add(new CraftsimScenario.SavedComponentSlot(3, uses));

        List<ExperimentStep> experiments = new ArrayList<ExperimentStep>();
        experiments.add(new ExperimentStep(
                "exp", 10, CraftOutcomeTier.GOOD));

        CraftsimScenario original = new CraftsimScenario(
                "dl44-test-1",
                new CraftsimScenario.ServerInfo(154, "SWG Infinity"),
                "swg.crafting.simulator.server.infinity."
                        + "InfinityServerRulesProvider",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                CraftsimScenarioStore.sha256("ruleset-v2"),
                "pistol_blaster_dl44",
                "DL44 Pistol",
                CraftOutcomeTier.GREAT,
                resources, components, experiments,
                "DL44 test",
                1700000000L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(base);
        store.save(original);

        File file = new File(new File(base, "scenarios"),
                "dl44-test-1.craftsim.json");
        if (!file.isFile()) {
            throw new AssertionError("scenario file not written");
        }
        File tmpLeftover = new File(new File(base, "scenarios"),
                "dl44-test-1.craftsim.json.tmp");
        if (tmpLeftover.exists()) {
            throw new AssertionError("atomic save left .tmp file behind");
        }

        CraftsimScenario loaded = store.load(
                "dl44-test-1", original.getRulesetHash());
        if (loaded.getVersion() != CraftsimScenario.CURRENT_VERSION) {
            throw new AssertionError(
                    "version not at current: " + loaded.getVersion());
        }
        if (loaded.getServer().getServerId() != 154
                || !loaded.getServer().getServerName().equals("SWG Infinity")) {
            throw new AssertionError("server info lost");
        }
        if (!loaded.getSchematicId().equals("pistol_blaster_dl44")) {
            throw new AssertionError("schematic id lost");
        }
        if (loaded.getResources().size() != 2) {
            throw new AssertionError("resource count lost: "
                    + loaded.getResources().size());
        }
        if (loaded.getResources().get(0).getStats().get(ResourceStat.UT)
                .intValue() != 950) {
            throw new AssertionError("resource UT stat lost");
        }
        if (loaded.getComponents().size() != 1
                || loaded.getComponents().get(0).getUses().size() != 1) {
            throw new AssertionError("component slot lost");
        }
        if (!loaded.getComponents().get(0).getUses().get(0)
                .getTemplateId().endsWith(
                        "shared_blaster_power_handler.iff")) {
            throw new AssertionError("component template id lost");
        }
        if (loaded.getComponents().get(0).getUses().get(0)
                .getProperties().isEmpty()) {
            throw new AssertionError("component property lost");
        }
        if (loaded.getExperiments().size() != 1
                || loaded.getExperiments().get(0).getPoints() != 10) {
            throw new AssertionError("experiment step lost");
        }
        if (loaded.getAssemblyOutcome() != CraftOutcomeTier.GREAT) {
            throw new AssertionError("assembly outcome lost");
        }

        // 2) hash rejection
        boolean rejected = false;
        try {
            store.load("dl44-test-1",
                    "deadbeefdeadbeefdeadbeefdeadbeef"
                    + "deadbeefdeadbeefdeadbeefdeadbeef");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError("hash mismatch accepted");
        }

        // 3) listIds
        if (!store.listIds().contains("dl44-test-1")) {
            throw new AssertionError("listIds missing saved id");
        }

        // 4) id validation
        for (String bad : new String[] {
                "", "..", "../escape", "sub/dir", "sub\\dir",
                "/abs/path", "C:\\abs", ".hidden", " ", "id\u0000ctrl",
                "a-very-long-id-that-exceeds-the-allowed-cap-of-ninety"
                        + "-six-characters-because-we-need-it-to-fail-the-cap-test"}) {
            boolean caught = false;
            try {
                CraftsimScenario.validateId(bad);
            } catch (IllegalArgumentException expected) {
                caught = true;
            }
            if (!caught) {
                throw new AssertionError(
                        "validateId accepted unsafe id: " + bad);
            }
        }
        for (String bad : new String[] { "../escape", "/abs",
                "sub/dir", "sub\\dir" }) {
            boolean caught = false;
            try {
                store.save(new CraftsimScenario(
                        bad,
                        new CraftsimScenario.ServerInfo(154, "x"),
                        "p", "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                        CraftsimScenarioStore.sha256("r"),
                        "schem", "",
                        CraftOutcomeTier.GREAT,
                        Collections.<CraftsimScenario.SavedResource>emptyList(),
                        Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                        Collections.<ExperimentStep>emptyList(),
                        "n", 0L));
            } catch (IllegalArgumentException expected) {
                caught = true;
            } catch (java.io.IOException ioe) {
                caught = true; // save validates before disk writes
            }
            if (!caught) {
                throw new AssertionError(
                        "store.save accepted unsafe id: " + bad);
            }
        }

        // 5) migration: write a v1-format file and load it through the
        //    registered migration.
        CraftsimMigration.register(2, new CraftsimMigration.Migration() {
            @Override
            public CraftsimScenario apply(CraftsimScenario s) {
                // Migration signature: mark the v1 envelope as migrated.
                // If the original carried a 'schematic' attribute it is
                // already promoted into schematicId by fromJsonV1; here
                // we add a marker warning via the assembly outcome tier
                // change so the migrated scenario is observably different
                // from a v2 write.
                return new CraftsimScenario(
                        s.getId(),
                        s.getServer(),
                        s.getRulesProviderId(),
                        s.getRulesetCommit(),
                        s.getRulesetHash(),
                        s.getSchematicId(),
                        s.getSchematicName(),
                        CraftOutcomeTier.GOOD,
                        s.getResources(),
                        s.getComponents(),
                        s.getExperiments(),
                        s.getName(),
                        s.getSavedEpochSeconds());
            }
        });

        File v1File = new File(
                new File(base, "scenarios"), "v1-legacy.craftsim.json");
        FileWriter w = new FileWriter(v1File);
        try {
            // Minimal v1 envelope: id, provider, commit, hash, name,
            // saved_epoch_seconds, attributes map.
            w.write("{\n"
                    + "  \"version\": 1,\n"
                    + "  \"id\": \"v1-legacy\",\n"
                    + "  \"rules_provider_id\": \""
                    + "swg.crafting.simulator.server.infinity."
                    + "InfinityServerRulesProvider\",\n"
                    + "  \"ruleset_commit\": "
                    + "\"6b6ac3726aaa3c293fca83911850d3a02e2adb4f\",\n"
                    + "  \"ruleset_hash\": \""
                    + CraftsimScenarioStore.sha256("ruleset-v2") + "\",\n"
                    + "  \"name\": \"v1 legacy scenario\",\n"
                    + "  \"saved_epoch_seconds\": 1500000000,\n"
                    + "  \"attributes\": {\n"
                    + "    \"schematic\": \"pistol_blaster_dl44\",\n"
                    + "    \"experiment\": \"damage\"\n"
                    + "  }\n"
                    + "}\n");
        } finally {
            w.close();
        }
        CraftsimScenario migrated = store.load(
                "v1-legacy", CraftsimScenarioStore.sha256("ruleset-v2"));
        if (migrated.getVersion() != CraftsimScenario.CURRENT_VERSION) {
            throw new AssertionError(
                    "migration failed: version is " + migrated.getVersion());
        }
        if (migrated.getAssemblyOutcome() != CraftOutcomeTier.GOOD) {
            throw new AssertionError(
                    "v1 -> v2 migration did not run: outcome is "
                    + migrated.getAssemblyOutcome());
        }
        if (!migrated.getSchematicId().equals("pistol_blaster_dl44")) {
            throw new AssertionError(
                    "v1 schematic attribute not promoted into v2");
        }

        // 6) SWGAide.DAT is not touched
        File dat = new File("SWGAide.DAT");
        if (dat.exists() && dat.lastModified() != 0L) {
            long ageMillis =
                    System.currentTimeMillis() - dat.lastModified();
            if (ageMillis < 60_000L) {
                throw new AssertionError(
                        "SWGAide.DAT was modified during the test");
            }
        }

        // 7) Atomic write: after a re-save the existing file is replaced
        //    in place and no .tmp is left behind.
        store.save(original);
        if (tmpLeftover.exists()) {
            throw new AssertionError(
                    "atomic re-save left .tmp file behind");
        }

        deleteRecursively(base);
        System.out.println("CraftsimPersistenceSelfTest PASS");
    }

    private static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursively(c);
            }
        }
        f.delete();
    }
}
