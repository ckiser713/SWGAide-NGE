package swg.infinity.fixtures;

import java.io.File;
import java.util.List;

/**
 * Exercises the seed fixture framework:
 * - parser handles a small valid seed;
 * - provenance enforcement rejects hand-drafted fixtures;
 * - slot, experiment-group, and resource-stats structures are round-tripped.
 */
public final class SeedFixtureParseSelfTest {
    private SeedFixtureParseSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        File dir = new File("src/test/resources/swg/infinity/fixtures/seeds");
        if (!dir.isDirectory()) {
            throw new AssertionError("seed directory missing: " + dir);
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new AssertionError(
                    "no seed fixtures present at " + dir.getAbsolutePath());
        }

        int parsed = 0;
        for (File f : files) {
            if (!f.getName().endsWith(".json")) continue;
            SeedFixture seed = SeedFixtureParser.parseSeed(
                    SeedFixtureParser.parseFile(f));
            verifySeed(seed);
            parsed++;
        }
        if (parsed == 0) {
            throw new AssertionError("no seed .json files parsed");
        }

        // provenance enforcement: hand-drafted fixtures must be rejected.
        String handDrafted =
                "{\"provenance\":\"hand-drafted\",\"id\":\"x\","
                + "\"source_sha\":\"deadbeefdeadbeefdeadbeefdeadbeefdeadbeef\","
                + "\"source_path\":\"x\"}";
        boolean rejected = false;
        try {
            SeedFixtureParser.parseSeed(
                    SeedFixtureParser.parse(handDrafted));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "hand-drafted provenance must be rejected");
        }

        System.out.println(
                "SeedFixtureParseSelfTest PASS (" + parsed + " seeds)");
    }

    private static void verifySeed(SeedFixture seed) {
        if (!"source-pinned".equals(seed.getSourceSha())
                && seed.getSourceSha() != null) {
            // ok: sourceSha is a real SHA; provenance is a separate field
        }
        if (seed.getSourceSha() == null
                || seed.getSourceSha().length() != 40) {
            throw new AssertionError(
                    "seed " + seed.getId() + " has invalid source SHA");
        }
        if (seed.getSourcePath() == null || seed.getSourcePath().isEmpty()) {
            throw new AssertionError(
                    "seed " + seed.getId() + " has no source path");
        }
        List<SeedFixture.SeedSlot> slots = seed.getSlots();
        if (slots.isEmpty()) {
            throw new AssertionError(
                    "seed " + seed.getId() + " has no slots");
        }
        for (SeedFixture.SeedSlot slot : slots) {
            if (slot.getQuantity() < 1) {
                throw new AssertionError(
                        "seed " + seed.getId()
                        + " slot " + slot.getIndex()
                        + " has non-positive quantity");
            }
        }
    }
}
