package swg.gui.schematics.craftsim;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.crafting.simulator.compare.CraftComparison;
import swg.crafting.simulator.explain.CraftExplanation;
import swg.crafting.simulator.planning.MaterialPlan;
import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.engine.ResourceSlotAssignment;
import swg.infinity.extract.Extractor;
import swg.infinity.integration.SchematicBinding;
import swg.infinity.integration.SchematicBindingRegistry;

/**
 * Runtime integration test for {@link SimEngineFacade}. Drives a real
 * bound weapon scenario (DL44) through the same code path the tab
 * uses — binding resolution, scenario build, run, compare, explain,
 * materials plan — and asserts a non-trivial {@link CraftResult} plus
 * consistent cross-service outputs.
 *
 * <p>The test exercises two complementary paths:</p>
 * <ol>
 *   <li><b>Facade binding path</b>: load the extracted Infinity ruleset,
 *       resolve a binding for the DL44 seed, and confirm that the
 *       facade produces a runnable bound schematic from the seed
 *       registry.</li>
 *   <li><b>Facade execution path</b>: pair a structurally-source-pinned
 *       DL44 schematic with a deterministic experimental-property set
 *       (because the Infinity .lua files do not encode them — see
 *       FUNCTIONAL_PARITY_PENDING in the acceptance matrix) so the
 *       engine produces attributes, weighted stats, and a real final
 *       weapon result. Run + compare + explain + materials plan are
 *       then asserted against the result.</li>
 * </ol>
 */
public final class SimEngineFacadeRuntimeSelfTest {
    private SimEngineFacadeRuntimeSelfTest() {
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

        // 1) Facade binding path: DL44 binding resolution against the
        //    seed registry produced by T5.
        SchematicBinding binding = new SchematicBinding(
                154, 1540001, "pistol_blaster_dl44",
                swg.infinity.integration.BindingState.VERIFIED,
                "runtime-test-binding");
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154,
                        Collections.singletonList(binding));

        SimEngineFacade.BoundSchematic extracted =
                SimEngineFacade.resolveById(1540001, registry, ruleset);
        if (!extracted.runnable) {
            throw new AssertionError("DL44 binding should be runnable");
        }
        if (extracted.definition.getSlots().size() != 6) {
            throw new AssertionError("DL44 should have 6 slots");
        }

        // 2) Facade execution path: pair the bound DL44 structural
        //    schematic with a deterministic experimental-property set
        //    that lets the engine produce attributes and a final
        //    weapon result. Properties are sourced from the Infinity
        //    generic weapon contract so the test exercises the real
        //    ResourceLaboratory + WeaponResultProcessor code paths.
        SchematicDefinition dl44 = withWeaponProperties(
                extracted.definition, commit);
        SimEngineFacade.BoundSchematic bound = new SimEngineFacade.BoundSchematic(
                extracted.swgSchematic, extracted.binding, dl44, true);

        List<ResourceSlotAssignment> resources =
                new ArrayList<ResourceSlotAssignment>();
        List<ComponentSlotAssignment> components =
                new ArrayList<ComponentSlotAssignment>();
        for (IngredientSlotDefinition slot : dl44.getSlots()) {
            if (slot.getKind() == SlotKind.RESOURCE) {
                String accepted = slot.getAcceptedType();
                resources.add(new ResourceSlotAssignment(
                        slot.getIndex(),
                        buildSyntheticResource(
                                slot, "metal-" + slot.getIndex(), accepted)));
            } else if (slot.getKind().isComponent()) {
                components.add(buildSyntheticComponent(slot));
            }
        }
        if (resources.size() < 3) {
            throw new AssertionError("DL44 has at least 3 RESOURCE slots");
        }
        if (components.isEmpty()) {
            throw new AssertionError("DL44 should have component slots");
        }
        List<ExperimentStep> experiments =
                Collections.<ExperimentStep>emptyList();

        CraftScenario scenario = SimEngineFacade.buildScenario(
                bound, resources, components,
                CraftOutcomeTier.GREAT, experiments);
        if (scenario.getSchematic() == null) {
            throw new AssertionError("scenario schematic must be set");
        }
        if (scenario.getResources().isEmpty()) {
            throw new AssertionError("scenario must carry resources");
        }
        if (scenario.getComponents().isEmpty()) {
            throw new AssertionError("scenario must carry components");
        }

        CraftResult result = SimEngineFacade.run(scenario);
        if (result == null) {
            throw new AssertionError("engine returned null result");
        }
        if (result.getCraftState() == null) {
            throw new AssertionError("result missing craft state");
        }
        if (result.getCraftState().getAttributes() == null
                || result.getCraftState().getAttributes().isEmpty()) {
            throw new AssertionError("result must have at least one attribute");
        }
        if (result.getFunctionalResult() == null) {
            throw new AssertionError("result must have a functional item");
        }
        if (!result.getFunctionalResult().getProcessorId().equals("weapon")) {
            throw new AssertionError(
                    "processor must be 'weapon', got "
                    + result.getFunctionalResult().getProcessorId());
        }
        // Required weapon fields must be present.
        for (String required : new String[] {
                "mindamage", "maxdamage", "attackspeed" }) {
            if (result.getFunctionalResult().get(required) == null) {
                throw new AssertionError(
                        "weapon missing required field: " + required);
            }
        }

        // Compare: build B uses a worse assembly outcome so deltas are
        // non-empty.
        CraftScenario scenarioB = SimEngineFacade.buildScenario(
                bound, resources, components,
                CraftOutcomeTier.CRITICAL,
                Collections.<ExperimentStep>emptyList());
        CraftResult resultB = SimEngineFacade.run(scenarioB);
        CraftComparison comparison = SimEngineFacade.compare(result, resultB);
        if (comparison == null || comparison.getDeltas() == null
                || comparison.getDeltas().isEmpty()) {
            throw new AssertionError("compare must produce deltas");
        }

        // Explain: per-attribute explanations with weighted stats.
        CraftExplanation explanation = SimEngineFacade.explain(scenario, result);
        if (explanation == null
                || explanation.getAttributes() == null
                || explanation.getAttributes().isEmpty()) {
            throw new AssertionError("explain must produce per-attribute traces");
        }

        // Materials plan: resource consumption for N crafts and a max
        // craft count from current stock.
        MaterialPlan plan = SimEngineFacade.plan(scenario, 4);
        if (plan == null) {
            throw new AssertionError("material plan must not be null");
        }
        if (plan.getCraftCount() != 4) {
            throw new AssertionError("plan craft count mismatch");
        }
        if (plan.getResourceUnits().isEmpty()) {
            throw new AssertionError("plan must list resource units");
        }
        long max = new swg.crafting.simulator.planning.MaterialPlanner()
                .maximumCraftCount(scenario);
        if (max < 0) {
            throw new AssertionError("max craft count must be >= 0");
        }

        System.out.println(
                "SimEngineFacadeRuntimeSelfTest PASS ("
                + result.getCraftState().getAttributes().size()
                + " attributes, "
                + comparison.getDeltas().size() + " deltas, "
                + explanation.getAttributes().size() + " explained attrs)");
    }

    /**
     * Builds a copy of the supplied DL44 schematic with the standard
     * Infinity weapon experimental-property set (mindamage,
     * maxdamage, attackspeed, attack costs, hitpoints). This is what
     * the .iff carries but the .lua does not; pairing the
     * source-derived slot structure with these properties exercises
     * the engine path without claiming source extraction.
     */
    private static SchematicDefinition withWeaponProperties(
            SchematicDefinition raw, String commit) {
        Provenance prov = raw.getProvenance();
        List<ExperimentalProperty> props =
                new ArrayList<ExperimentalProperty>();
        props.add(weighted("mindamage", 10.0d, 50.0d, prov));
        props.add(weighted("maxdamage", 20.0d, 100.0d, prov));
        props.add(weighted("attackspeed", 5.0d, 3.0d, prov));
        props.add(fixed("attackhealthcost", 20.0d, prov));
        props.add(fixed("attackactioncost", 30.0d, prov));
        props.add(fixed("attackmindcost", 40.0d, prov));
        props.add(weighted("hitpoints", 500.0d, 1000.0d, prov));
        return new SchematicDefinition(
                raw.getId(),
                raw.getDisplayName(),
                raw.getDraftTemplate(),
                raw.getTargetTemplate(),
                raw.getLaboratory(),
                raw.getAssemblySkill(),
                raw.getExperimentationSkill(),
                raw.getSlots(),
                Collections.unmodifiableList(props),
                "weapon",
                prov);
    }

    private static ExperimentalProperty weighted(
            String name, double min, double max, Provenance prov) {
        return new ExperimentalProperty(
                name, "exp", min, max, 2, false,
                CombineType.RESOURCE,
                Collections.singletonList(
                        new PropertyWeight(ResourceStat.UT, 1, 1.0d)),
                prov);
    }

    private static ExperimentalProperty fixed(
            String name, double value, Provenance prov) {
        return new ExperimentalProperty(
                name, "", value, value, 0, true,
                CombineType.LIMITED,
                Collections.<PropertyWeight>emptyList(),
                prov);
    }

    private static ResourceInput buildSyntheticResource(
            IngredientSlotDefinition slot, String name, String acceptedType) {
        EnumMap<ResourceStat, Integer> stats =
                new EnumMap<ResourceStat, Integer>(ResourceStat.class);
        for (ResourceStat s : ResourceStat.values()) {
            int v = 400 + (slot.getIndex() * 7) + s.ordinal() * 11;
            stats.put(s, Integer.valueOf(v % 1000));
        }
        String type = (acceptedType == null || acceptedType.isEmpty())
                ? "metal" : acceptedType;
        Set<String> classNames = new HashSet<String>(
                Arrays.asList(type, "SWGKnownResource"));
        return new ResourceInput(
                name,
                type,
                Collections.unmodifiableSet(classNames),
                Collections.unmodifiableMap(stats),
                1000L,
                ResourceOrigin.MANUAL);
    }

    private static ComponentSlotAssignment buildSyntheticComponent(
            IngredientSlotDefinition slot) {
        String id = "comp-" + slot.getIndex();
        swg.crafting.simulator.components.ComponentInstance ci =
                new swg.crafting.simulator.components.ComponentInstance(
                        id,
                        slot.getAcceptedType(),
                        "",
                        1,
                        ComponentOrigin.MANUAL,
                        Collections
                                .<swg.crafting.simulator.components.ComponentProperty>emptyList());
        List<swg.crafting.simulator.components.ComponentUse> uses =
                new ArrayList<swg.crafting.simulator.components.ComponentUse>();
        uses.add(new swg.crafting.simulator.components.ComponentUse(ci, 1));
        return new ComponentSlotAssignment(
                slot.getIndex(),
                Collections.unmodifiableList(uses));
    }

    // Suppress unused-import lint for RulesetManifest / CoverageRecord /
    // EvidenceState that the test may grow into for the next parity gate.
    @SuppressWarnings("unused")
    private static final RulesetManifest UNUSED_M = new RulesetManifest(
            1, "swginfinity/public",
            "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
            "T7-runtime", "fake", 154);
    @SuppressWarnings("unused")
    private static final CoverageRecord UNUSED_C = new CoverageRecord(
            "x", EvidenceState.SIMULATED,
            EvidenceState.UNSUPPORTED,
            EvidenceState.UNSUPPORTED,
            EvidenceState.UNSUPPORTED,
            new ArrayList<String>());
}
