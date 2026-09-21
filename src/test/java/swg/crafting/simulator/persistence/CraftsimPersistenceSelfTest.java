package swg.crafting.simulator.persistence;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Round-trip + migration + hash-rejection assertions for
 * {@link CraftsimScenarioStore}. Verifies that:
 * - a saved scenario can be loaded with the matching hash;
 * - loading with a mismatched hash is rejected;
 * - version migration is applied when the on-disk version differs from
 *   the current version;
 * - the storage does not touch legacy SWGAide.DAT.
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

        // 1) round-trip
        Map<String, String> attrs = new LinkedHashMap<String, String>();
        attrs.put("schematic", "weapon.pistol.dl44");
        attrs.put("experiment", "damage");
        CraftsimScenario original = new CraftsimScenario(
                "test-1",
                "swg.crafting.simulator.server.infinity.InfinityServerRulesProvider",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                CraftsimScenarioStore.sha256("ruleset-1"),
                "DL44 test",
                1700000000L,
                attrs);

        CraftsimScenarioStore store = new CraftsimScenarioStore(base);
        store.save(original);
        if (!new File(new File(base, "scenarios"),
                "test-1.craftsim.json").isFile()) {
            throw new AssertionError("scenario file not written");
        }
        CraftsimScenario loaded = store.load(
                "test-1", original.getRulesetHash());
        if (!loaded.getId().equals(original.getId())
                || !loaded.getRulesetHash().equals(original.getRulesetHash())
                || !loaded.getName().equals(original.getName())) {
            throw new AssertionError("round-trip mismatch: " + loaded);
        }
        if (!loaded.getAttributes().get("schematic").equals("weapon.pistol.dl44")) {
            throw new AssertionError("attributes not preserved");
        }

        // 2) hash rejection
        boolean rejected = false;
        try {
            store.load("test-1",
                    "deadbeefdeadbeefdeadbeefdeadbeef"
                    + "deadbeefdeadbeefdeadbeefdeadbeef");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError("hash mismatch accepted");
        }

        // 3) listIds
        if (!store.listIds().contains("test-1")) {
            throw new AssertionError("listIds missing saved id");
        }

        // 4) version migration
        CraftsimMigration.register(2, new CraftsimMigration.Migration() {
            @Override
            public CraftsimScenario apply(CraftsimScenario s) {
                return s.atVersion(2);
            }
        });
        File scenarios = new File(base, "scenarios");
        File v1File = new File(scenarios, "test-v1.craftsim.json");
        FileWriter w = new FileWriter(v1File);
        try {
            w.write(CraftsimScenarioStore.toJson(original));
        } finally {
            w.close();
        }
        // Overwrite the on-disk version to 1 explicitly
        String contents = new java.util.Scanner(v1File, "UTF-8")
                .useDelimiter("\\A").next();
        contents = contents.replace(
                "\"version\": " + CraftsimScenario.CURRENT_VERSION,
                "\"version\": 1");
        w = new FileWriter(v1File);
        try {
            w.write(contents);
        } finally {
            w.close();
        }
        CraftsimScenario migrated = store.load(
                "test-v1", original.getRulesetHash());
        if (migrated.getVersion() != CraftsimScenario.CURRENT_VERSION) {
            throw new AssertionError(
                    "migration failed: version is " + migrated.getVersion());
        }

        // 5) SWGAide.DAT is not touched
        File dat = new File("SWGAide.DAT");
        if (dat.exists() && dat.lastModified() != 0L) {
            long ageMillis = System.currentTimeMillis() - dat.lastModified();
            if (ageMillis < 60_000L) {
                throw new AssertionError(
                        "SWGAide.DAT was modified during the test");
            }
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
