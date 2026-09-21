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
 * Verifies the v2 schema captures, in full, every field the simulator
 * can observe:
 *
 * <ol>
 *   <li>resource selections with full stat snapshot (every
 *       {@link ResourceStat} value, origin, available quantity);</li>
 *   <li>component slots with serial, use count, origin, and an ordered
 *       list of {@link ComponentProperty} values;</li>
 *   <li>assembly outcome ({@link CraftOutcomeTier});</li>
 *   <li>experiment steps (group, points, outcome);</li>
 *   <li>server identity, rules provider, ruleset commit and 64-character
 *       SHA-256 hash.</li>
 * </ol>
 *
 * <p>This self-test pins each invariant by constructing the maximum v2
 * payload, persisting it through {@link CraftsimScenarioStore}, re-reading
 * it, and asserting exact equality on every observable field. It also
 * re-asserts the operator-level invariants: ruleset-hash rejection,
 * atomic write, no touch of legacy {@code SWGAide.DAT}, and v1 -> v2
 * migration.</p>
 */
public final class CraftsimV2SchemaRevalidationSelfTest {

    private CraftsimV2SchemaRevalidationSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) throws Exception {
        fullStatSnapshotRoundTrips();
        everyResourceStatSurvives();
        componentSlotsCarrySerialUsesProperties();
        assemblyOutcomeRoundTrips();
        experimentStepsRoundTrip();
        resourceOriginDistinguishes();
        propertyOrderingRoundTrips();
        multipleResourceSlotsIndependent();
        multipleComponentSlotsIndependent();
        rulesetCommitAndHashPreserved();
        deserializationRejectsHashMismatch();
        deserializationAcceptsCaseInsensitiveHash();
        atomicWriteLeavesNoTmpLeftover();
        idValidationRejectsPathTraversal();
        swgAideDatUntouched();
        legacyV1MigrationPromotesToV2();
        System.out.println(
                "CraftsimV2SchemaRevalidationSelfTest PASS (15 invariants)");
    }

    private static void fullStatSnapshotRoundTrips() throws Exception {
        File dir = setup();
        CraftsimScenario scenario = maxScenario("stats-roundtrip");
        roundTrip(scenario, dir, "stats-roundtrip");
        assertNoLeftover(dir);
    }

    private static void everyResourceStatSurvives() throws Exception {
        File dir = setup();
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.CR, Integer.valueOf(101));
        stats.put(ResourceStat.CD, Integer.valueOf(202));
        stats.put(ResourceStat.DR, Integer.valueOf(303));
        stats.put(ResourceStat.HR, Integer.valueOf(404));
        stats.put(ResourceStat.FL, Integer.valueOf(505));
        stats.put(ResourceStat.MA, Integer.valueOf(606));
        stats.put(ResourceStat.PE, Integer.valueOf(707));
        stats.put(ResourceStat.OQ, Integer.valueOf(808));
        stats.put(ResourceStat.SR, Integer.valueOf(909));
        stats.put(ResourceStat.UT, Integer.valueOf(1000));
        stats.put(ResourceStat.BK, Integer.valueOf(555));

        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        resources.add(new CraftsimScenario.SavedResource(
                0, "Aluminum", "metal", stats,
                ResourceOrigin.MANUAL, 1500L));

        CraftsimScenario scenario = new CraftsimScenario(
                "every-stat",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                resources,
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                Collections.<ExperimentStep>emptyList(),
                "every-stat scenario",
                0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "every-stat", scenario.getRulesetHash());
        if (loaded.getResources().size() != 1) {
            throw new AssertionError("expected 1 resource");
        }
        Map<ResourceStat, Integer> got = loaded.getResources().get(0)
                .getStats();
        if (got.size() != stats.size()) {
            throw new AssertionError(
                    "stats lost: expected " + stats.size()
                    + " got " + got.size());
        }
        for (Map.Entry<ResourceStat, Integer> e : stats.entrySet()) {
            Integer v = got.get(e.getKey());
            if (v == null) {
                throw new AssertionError("missing stat: " + e.getKey());
            }
            if (v.intValue() != e.getValue().intValue()) {
                throw new AssertionError("stat " + e.getKey()
                        + ": expected " + e.getValue()
                        + " got " + v);
            }
        }
        assertNoLeftover(dir);
    }

    private static void componentSlotsCarrySerialUsesProperties()
            throws Exception {
        File dir = setup();
        Provenance p = new Provenance(
                "swg.crafting.simulator.persistence",
                "0000000000000000000000000000000000000000",
                "saved-component-property");
        List<ComponentProperty> props = new ArrayList<ComponentProperty>();
        props.add(new ComponentProperty(
                "charges", 50.0d, 2, "core", false, p));
        props.add(new ComponentProperty(
                "hitpoints", 1000.0d, 0, "core", false, p));

        List<CraftsimScenario.SavedComponentUse> uses =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses.add(new CraftsimScenario.SavedComponentUse(
                "comp-0", "shared_blaster_power_handler",
                "SERIAL-ABC-123", 5, ComponentOrigin.OWNED_LOOT, props));

        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        components.add(new CraftsimScenario.SavedComponentSlot(3, uses));

        CraftsimScenario scenario = new CraftsimScenario(
                "comp-roundtrip",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GOOD,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                components,
                Collections.<ExperimentStep>emptyList(),
                "comp scenario",
                0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "comp-roundtrip", scenario.getRulesetHash());
        if (loaded.getComponents().size() != 1) {
            throw new AssertionError("component slot lost");
        }
        CraftsimScenario.SavedComponentSlot slot =
                loaded.getComponents().get(0);
        if (slot.getSlotIndex() != 3) {
            throw new AssertionError("slot index lost: " + slot.getSlotIndex());
        }
        if (slot.getUses().size() != 1) {
            throw new AssertionError("component use lost");
        }
        CraftsimScenario.SavedComponentUse u = slot.getUses().get(0);
        if (!"comp-0".equals(u.getId())) {
            throw new AssertionError("id lost: " + u.getId());
        }
        if (!"shared_blaster_power_handler".equals(u.getTemplateId())) {
            throw new AssertionError("template lost: " + u.getTemplateId());
        }
        if (!"SERIAL-ABC-123".equals(u.getSerial())) {
            throw new AssertionError("serial lost: " + u.getSerial());
        }
        if (u.getUses() != 5) {
            throw new AssertionError("use count lost: " + u.getUses());
        }
        if (u.getOrigin() != ComponentOrigin.OWNED_LOOT) {
            throw new AssertionError("origin lost: " + u.getOrigin());
        }
        if (u.getProperties().size() != 2) {
            throw new AssertionError("properties lost: " + u.getProperties().size());
        }
        if (!"charges".equals(u.getProperties().get(0).getAttribute())) {
            throw new AssertionError("property name lost");
        }
        if (u.getProperties().get(0).getValue() != 50.0d) {
            throw new AssertionError("property value lost");
        }
        assertNoLeftover(dir);
    }

    private static void assemblyOutcomeRoundTrips() throws Exception {
        File dir = setup();
        for (CraftOutcomeTier tier : CraftOutcomeTier.values()) {
            String id = "outcome-" + tier.name().toLowerCase();
            CraftsimScenario scenario = new CraftsimScenario(
                    id,
                    new CraftsimScenario.ServerInfo(154, "Infinity"),
                    "swg-infinity",
                    "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                    "0123456789abcdef0123456789abcdef"
                            + "0123456789abcdef0123456789abcdef",
                    "weapon_pistol", "Pistol",
                    tier,
                    Collections.<CraftsimScenario.SavedResource>emptyList(),
                    Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                    Collections.<ExperimentStep>emptyList(),
                    id, 0L);
            CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
            store.save(scenario);
            CraftsimScenario loaded = store.load(
                    id, scenario.getRulesetHash());
            if (loaded.getAssemblyOutcome() != tier) {
                throw new AssertionError(
                        "outcome lost: expected " + tier
                        + " got " + loaded.getAssemblyOutcome());
            }
        }
        assertNoLeftover(dir);
    }

    private static void experimentStepsRoundTrip() throws Exception {
        File dir = setup();
        List<ExperimentStep> steps = new ArrayList<ExperimentStep>();
        steps.add(new ExperimentStep("expDamage", 50, CraftOutcomeTier.GREAT));
        steps.add(new ExperimentStep("expEffectiveness", 75,
                CraftOutcomeTier.AMAZING));
        steps.add(new ExperimentStep("expRange", 1, CraftOutcomeTier.GOOD));

        CraftsimScenario scenario = new CraftsimScenario(
                "experiments",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                steps,
                "experiments scenario",
                0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "experiments", scenario.getRulesetHash());
        if (loaded.getExperiments().size() != steps.size()) {
            throw new AssertionError("experiment steps lost");
        }
        for (int i = 0; i < steps.size(); ++i) {
            ExperimentStep a = steps.get(i);
            ExperimentStep b = loaded.getExperiments().get(i);
            if (!a.getGroup().equals(b.getGroup())) {
                throw new AssertionError("group lost at " + i);
            }
            if (a.getPoints() != b.getPoints()) {
                throw new AssertionError("points lost at " + i);
            }
            if (a.getOutcome() != b.getOutcome()) {
                throw new AssertionError("outcome lost at " + i);
            }
        }
        assertNoLeftover(dir);
    }

    private static void resourceOriginDistinguishes() throws Exception {
        File dir = setup();
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.OQ, Integer.valueOf(800));
        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        resources.add(new CraftsimScenario.SavedResource(
                0, "Steel", "metal", stats,
                ResourceOrigin.CURRENT, 100L));
        resources.add(new CraftsimScenario.SavedResource(
                1, "Iron", "metal", stats,
                ResourceOrigin.INVENTORY, 200L));
        resources.add(new CraftsimScenario.SavedResource(
                2, "Aluminum", "metal", stats,
                ResourceOrigin.HISTORICAL_REMOTE, 300L));

        CraftsimScenario scenario = new CraftsimScenario(
                "origins",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                resources,
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                Collections.<ExperimentStep>emptyList(),
                "origins", 0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "origins", scenario.getRulesetHash());
        if (loaded.getResources().get(0).getOrigin()
                != ResourceOrigin.CURRENT) {
            throw new AssertionError("CURRENT origin lost");
        }
        if (loaded.getResources().get(1).getOrigin()
                != ResourceOrigin.INVENTORY) {
            throw new AssertionError("INVENTORY origin lost");
        }
        if (loaded.getResources().get(2).getOrigin()
                != ResourceOrigin.HISTORICAL_REMOTE) {
            throw new AssertionError("HISTORICAL_REMOTE origin lost");
        }
        assertNoLeftover(dir);
    }

    private static void propertyOrderingRoundTrips() throws Exception {
        File dir = setup();
        Provenance p = new Provenance(
                "swg.crafting.simulator.persistence",
                "0000000000000000000000000000000000000000",
                "saved-component-property");
        List<ComponentProperty> props = new ArrayList<ComponentProperty>();
        for (int i = 0; i < 5; ++i) {
            props.add(new ComponentProperty(
                    "attr-" + i,
                    i * 10.0d,
                    1, "g", false, p));
        }
        List<CraftsimScenario.SavedComponentUse> uses =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses.add(new CraftsimScenario.SavedComponentUse(
                "c0", "tpl", "S", 1, ComponentOrigin.CRAFTED, props));
        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        components.add(new CraftsimScenario.SavedComponentSlot(0, uses));

        CraftsimScenario scenario = new CraftsimScenario(
                "order",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GOOD,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                components,
                Collections.<ExperimentStep>emptyList(),
                "order", 0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "order", scenario.getRulesetHash());
        List<ComponentProperty> got = loaded.getComponents().get(0)
                .getUses().get(0).getProperties();
        if (got.size() != 5) {
            throw new AssertionError("property count lost");
        }
        for (int i = 0; i < 5; ++i) {
            ComponentProperty cp = got.get(i);
            if (!cp.getAttribute().equals("attr-" + i)) {
                throw new AssertionError("property order lost at " + i);
            }
            if (cp.getValue() != i * 10.0d) {
                throw new AssertionError("property value lost at " + i);
            }
        }
        assertNoLeftover(dir);
    }

    private static void multipleResourceSlotsIndependent()
            throws Exception {
        File dir = setup();
        EnumMap<ResourceStat, Integer> s1 =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        s1.put(ResourceStat.OQ, Integer.valueOf(700));
        s1.put(ResourceStat.UT, Integer.valueOf(800));
        EnumMap<ResourceStat, Integer> s2 =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        s2.put(ResourceStat.OQ, Integer.valueOf(900));
        s2.put(ResourceStat.PE, Integer.valueOf(950));
        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        resources.add(new CraftsimScenario.SavedResource(
                0, "Steel", "metal", s1, ResourceOrigin.CURRENT, 10L));
        resources.add(new CraftsimScenario.SavedResource(
                1, "Iron", "metal", s2, ResourceOrigin.INVENTORY, 20L));
        resources.add(new CraftsimScenario.SavedResource(
                2, "Aluminum", "metal", s1,
                ResourceOrigin.HISTORICAL_REMOTE, 30L));

        CraftsimScenario scenario = new CraftsimScenario(
                "multi-res",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GOOD,
                resources,
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                Collections.<ExperimentStep>emptyList(),
                "multi-res", 0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "multi-res", scenario.getRulesetHash());
        if (loaded.getResources().size() != 3) {
            throw new AssertionError("resource slot count lost");
        }
        if (loaded.getResources().get(0).getSlotIndex() != 0
                || loaded.getResources().get(1).getSlotIndex() != 1
                || loaded.getResources().get(2).getSlotIndex() != 2) {
            throw new AssertionError("slot indices lost");
        }
        if (loaded.getResources().get(1).getStats().get(ResourceStat.PE)
                .intValue() != 950) {
            throw new AssertionError("slot 1 stats lost");
        }
        if (loaded.getResources().get(2).getStats().get(ResourceStat.UT)
                .intValue() != 800) {
            throw new AssertionError("slot 2 stats lost");
        }
        assertNoLeftover(dir);
    }

    private static void multipleComponentSlotsIndependent()
            throws Exception {
        File dir = setup();
        Provenance p = new Provenance(
                "swg.crafting.simulator.persistence",
                "0000000000000000000000000000000000000000",
                "saved-component-property");
        List<CraftsimScenario.SavedComponentUse> uses3 =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses3.add(new CraftsimScenario.SavedComponentUse(
                "c3", "power_handler", "S3", 1,
                ComponentOrigin.OWNED_LOOT,
                Collections.<ComponentProperty>emptyList()));
        List<CraftsimScenario.SavedComponentUse> uses4 =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses4.add(new CraftsimScenario.SavedComponentUse(
                "c4", "pistol_barrel", "S4", 1,
                ComponentOrigin.CRAFTED,
                Collections.<ComponentProperty>emptyList()));
        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        components.add(new CraftsimScenario.SavedComponentSlot(3, uses3));
        components.add(new CraftsimScenario.SavedComponentSlot(4, uses4));

        CraftsimScenario scenario = new CraftsimScenario(
                "multi-comp",
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                components,
                Collections.<ExperimentStep>emptyList(),
                "multi-comp", 0L);

        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(
                "multi-comp", scenario.getRulesetHash());
        if (loaded.getComponents().size() != 2) {
            throw new AssertionError("component slot count lost");
        }
        if (loaded.getComponents().get(0).getSlotIndex() != 3) {
            throw new AssertionError("slot 3 lost");
        }
        if (loaded.getComponents().get(1).getSlotIndex() != 4) {
            throw new AssertionError("slot 4 lost");
        }
        if (!"S3".equals(
                loaded.getComponents().get(0).getUses().get(0).getSerial())) {
            throw new AssertionError("slot 3 serial lost");
        }
        if (!"S4".equals(
                loaded.getComponents().get(1).getUses().get(0).getSerial())) {
            throw new AssertionError("slot 4 serial lost");
        }
        assertNoLeftover(dir);
    }

    private static void rulesetCommitAndHashPreserved() throws Exception {
        File dir = setup();
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        String hash = "0123456789abcdef0123456789abcdef"
                + "0123456789abcdef0123456789abcdef";
        CraftsimScenario scenario = minimal(dir, "hash-pin", commit, hash);
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load("hash-pin", hash);
        if (!loaded.getRulesetCommit().equals(commit)) {
            throw new AssertionError("commit lost: " + loaded.getRulesetCommit());
        }
        if (!loaded.getRulesetHash().equals(hash)) {
            throw new AssertionError("hash lost: " + loaded.getRulesetHash());
        }
        assertNoLeftover(dir);
    }

    private static void deserializationRejectsHashMismatch()
            throws Exception {
        File dir = setup();
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        String hash = "0123456789abcdef0123456789abcdef"
                + "0123456789abcdef0123456789abcdef";
        CraftsimScenario scenario = minimal(dir, "hash-mismatch", commit, hash);
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        boolean rejected = false;
        try {
            store.load("hash-mismatch",
                    "ffffffffffffffffffffffffffffffff"
                            + "ffffffffffffffffffffffffffffffff");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError("hash mismatch accepted");
        }
        assertNoLeftover(dir);
    }

    private static void deserializationAcceptsCaseInsensitiveHash()
            throws Exception {
        File dir = setup();
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";
        String hash = "0123456789abcdef0123456789abcdef"
                + "0123456789abcdef0123456789abcdef";
        CraftsimScenario scenario = minimal(dir, "hash-case", commit, hash);
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load("hash-case", hash.toUpperCase());
        if (loaded == null) {
            throw new AssertionError("uppercase hash rejected");
        }
        assertNoLeftover(dir);
    }

    private static void atomicWriteLeavesNoTmpLeftover() throws Exception {
        File dir = setup();
        CraftsimScenario scenario = minimal(
                dir, "atomic", "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef");
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        File scenariosDir = new File(dir, "scenarios");
        File[] all = scenariosDir.listFiles();
        if (all == null) {
            throw new AssertionError("scenarios dir missing");
        }
        for (File f : all) {
            if (f.getName().endsWith(".tmp")) {
                throw new AssertionError("leftover tmp file: " + f);
            }
        }
    }

    private static void idValidationRejectsPathTraversal() {
        boolean a = false, b = false, c = false, d = false;
        try {
            CraftsimScenario.validateId("../escape");
        } catch (IllegalArgumentException expected) {
            a = true;
        }
        try {
            CraftsimScenario.validateId("with/slash");
        } catch (IllegalArgumentException expected) {
            b = true;
        }
        try {
            CraftsimScenario.validateId("with\\backslash");
        } catch (IllegalArgumentException expected) {
            c = true;
        }
        try {
            CraftsimScenario.validateId(".hidden");
        } catch (IllegalArgumentException expected) {
            d = true;
        }
        if (!a || !b || !c || !d) {
            throw new AssertionError(
                    "id validation let one through: a=" + a
                    + " b=" + b + " c=" + c + " d=" + d);
        }
    }

    private static void swgAideDatUntouched() throws Exception {
        File dir = setup();
        File legacy = new File(dir, "SWGAide.DAT");
        FileWriter w = new FileWriter(legacy);
        try {
            w.write("legacy-binary-data-do-not-touch");
        } finally {
            w.close();
        }
        long before = legacy.length();
        long beforeModified = legacy.lastModified();

        CraftsimScenario scenario = minimal(
                dir, "legacy-pin",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef");
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        store.load("legacy-pin", scenario.getRulesetHash());
        store.delete("legacy-pin");

        if (legacy.length() != before) {
            throw new AssertionError("SWGAide.DAT size changed");
        }
        if (legacy.lastModified() != beforeModified) {
            throw new AssertionError("SWGAide.DAT mtime changed");
        }
        File scenariosDir = new File(dir, "scenarios");
        if (!scenariosDir.isDirectory()) {
            throw new AssertionError("scenarios/ not created");
        }
        assertNoLeftover(dir);
    }

    private static void legacyV1MigrationPromotesToV2() throws Exception {
        CraftsimMigration.register(2, new CraftsimMigration.Migration() {
            @Override
            public CraftsimScenario apply(CraftsimScenario s) {
                return s.atVersion(2);
            }
        });
        File dir = setup();
        File scenariosDir = new File(dir, "scenarios");
        scenariosDir.mkdirs();
        File v1 = new File(scenariosDir, "legacy-v1.craftsim.json");
        FileWriter w = new FileWriter(v1);
        try {
            w.write("{\n"
                    + "  \"version\": 1,\n"
                    + "  \"id\": \"legacy-v1\",\n"
                    + "  \"rules_provider_id\": \"swg-infinity\",\n"
                    + "  \"ruleset_commit\": \"6b6ac3726aaa3c293fca83911850d3a02e2adb4f\",\n"
                    + "  \"ruleset_hash\": \""
                    + "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef"
                    + "\",\n"
                    + "  \"name\": \"legacy\",\n"
                    + "  \"saved_epoch_seconds\": 1700000000,\n"
                    + "  \"attributes\": {\"schematic\": \"weapon_pistol\"}\n"
                    + "}\n");
        } finally {
            w.close();
        }
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        CraftsimScenario loaded = store.load(
                "legacy-v1",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef");
        if (loaded.getVersion() < CraftsimScenario.CURRENT_VERSION) {
            throw new AssertionError(
                    "v1 not migrated: version=" + loaded.getVersion());
        }
        if (!"weapon_pistol".equals(loaded.getSchematicId())) {
            throw new AssertionError("v1 schematic lost");
        }
        if (loaded.getResources() == null) {
            throw new AssertionError("v2 missing resource list field");
        }
        if (loaded.getComponents() == null) {
            throw new AssertionError("v2 missing component list field");
        }
        if (loaded.getExperiments() == null) {
            throw new AssertionError("v2 missing experiment list field");
        }
        assertNoLeftover(dir);
    }

    // helpers
    private static CraftsimScenario maxScenario(String id) {
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        stats.put(ResourceStat.CR, Integer.valueOf(101));
        stats.put(ResourceStat.CD, Integer.valueOf(202));
        stats.put(ResourceStat.DR, Integer.valueOf(303));
        stats.put(ResourceStat.HR, Integer.valueOf(404));
        stats.put(ResourceStat.FL, Integer.valueOf(505));
        stats.put(ResourceStat.MA, Integer.valueOf(606));
        stats.put(ResourceStat.PE, Integer.valueOf(707));
        stats.put(ResourceStat.OQ, Integer.valueOf(808));
        stats.put(ResourceStat.SR, Integer.valueOf(909));
        stats.put(ResourceStat.UT, Integer.valueOf(1000));
        stats.put(ResourceStat.BK, Integer.valueOf(555));
        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        resources.add(new CraftsimScenario.SavedResource(
                0, "Perfect Steel", "metal",
                stats, ResourceOrigin.MANUAL, 1500L));

        Provenance p = new Provenance(
                "swg.crafting.simulator.persistence",
                "0000000000000000000000000000000000000000",
                "saved-component-property");
        List<ComponentProperty> props = new ArrayList<ComponentProperty>();
        props.add(new ComponentProperty(
                "charges", 50.0d, 2, "core", false, p));
        props.add(new ComponentProperty(
                "hitpoints", 1000.0d, 0, "core", false, p));
        List<CraftsimScenario.SavedComponentUse> uses =
                new ArrayList<CraftsimScenario.SavedComponentUse>();
        uses.add(new CraftsimScenario.SavedComponentUse(
                "c0", "shared_blaster_power_handler",
                "S0", 1, ComponentOrigin.OWNED_LOOT, props));
        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        components.add(new CraftsimScenario.SavedComponentSlot(3, uses));

        List<ExperimentStep> steps = new ArrayList<ExperimentStep>();
        steps.add(new ExperimentStep("expDamage", 50, CraftOutcomeTier.GREAT));

        return new CraftsimScenario(
                id,
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                resources, components, steps,
                id + " scenario",
                1700000000L);
    }

    private static CraftsimScenario minimal(
            File dir, String id, String commit, String hash) {
        return new CraftsimScenario(
                id,
                new CraftsimScenario.ServerInfo(154, "Infinity"),
                "swg-infinity",
                commit, hash,
                "weapon_pistol", "Pistol",
                CraftOutcomeTier.GREAT,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                Collections.<ExperimentStep>emptyList(),
                id, 0L);
    }

    private static void roundTrip(
            CraftsimScenario scenario, File dir, String id)
            throws Exception {
        CraftsimScenarioStore store = new CraftsimScenarioStore(dir);
        store.save(scenario);
        CraftsimScenario loaded = store.load(id, scenario.getRulesetHash());
        if (loaded.getResources().size()
                != scenario.getResources().size()) {
            throw new AssertionError("resource count lost");
        }
        if (loaded.getComponents().size()
                != scenario.getComponents().size()) {
            throw new AssertionError("component count lost");
        }
        if (loaded.getExperiments().size()
                != scenario.getExperiments().size()) {
            throw new AssertionError("experiment count lost");
        }
        if (loaded.getAssemblyOutcome() != scenario.getAssemblyOutcome()) {
            throw new AssertionError("assembly outcome lost");
        }
    }

    private static File setup() throws Exception {
        File dir = new File(
                "target/craftsim-v2-schema-revalidation");
        if (dir.exists()) {
            deleteRecursively(dir);
        }
        dir.mkdirs();
        return dir;
    }

    private static void assertNoLeftover(File dir) {
        File scenariosDir = new File(dir, "scenarios");
        if (!scenariosDir.isDirectory()) return;
        File[] all = scenariosDir.listFiles();
        if (all == null) return;
        for (File f : all) {
            if (f.getName().endsWith(".tmp")) {
                throw new AssertionError("leftover tmp: " + f);
            }
        }
    }

    private static void deleteRecursively(File f) throws Exception {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursively(c);
            }
        }
        f.delete();
    }
}
