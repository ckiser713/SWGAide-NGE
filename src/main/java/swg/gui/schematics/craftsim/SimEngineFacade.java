package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import swg.crafting.schematics.SWGSchematic;
import swg.crafting.schematics.SWGResourceSlot;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.component.CraftedComponentFactory;
import swg.infinity.engine.CraftState;
import swg.infinity.engine.InfinityCraftEngine;
import swg.infinity.engine.ResourceSlotAssignment;

/**
 * Thin wrapper around the Infinity craft engine that the Crafting Simulator
 * tab calls. Headless-safe: when no schematic is supplied, returns a
 * representative synthetic result so the GUI can be exercised without
 * requiring a populated native SWGAide resource inventory.
 *
 * <p>Production wiring would translate {@link SWGResourceSlot} entries into
 * {@link ResourceSlotAssignment} entries using observed native resource
 * stats. This class keeps that mapping stubbed; the engine itself is the
 * production path and runs the deterministic arithmetic.</p>
 */
public final class SimEngineFacade {
    private SimEngineFacade() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Runs the Infinity engine against the supplied schematic + resources.
     * Returns a deterministic {@link CraftResult} suitable for rendering
     * by the GUI.
     */
    public static CraftResult run(
            SWGSchematic schematic,
            List<ResourceSlotAssignment> resources,
            String exoticComponentName,
            int recursionDepth,
            String experimentGroup,
            int experimentPoints) {
        InfinityCraftEngine engine = new InfinityCraftEngine();

        List<ResourceSlotAssignment> safeResources =
                resources == null
                        ? Collections.<ResourceSlotAssignment>emptyList()
                        : resources;

        // Recursive crafted component round-trip: build one synthetic
        // crafted component per recursion depth using CraftedComponentFactory.
        List<ComponentSlotAssignment> components =
                new ArrayList<ComponentSlotAssignment>();
        if (exoticComponentName != null && !exoticComponentName.isEmpty()
                && recursionDepth > 0) {
            CraftedComponentFactory factory = new CraftedComponentFactory();
            // Each recursion level creates a synthetic sub-craft whose
            // attributes become a ComponentInstance on the parent. This
            // exercises ComponentInstance round-trip per the plan.
            for (int d = 0; d < recursionDepth; ++d) {
                String id = exoticComponentName + "-r" + d;
                CraftState sub = engine.execute(new CraftScenario(
                        null, Collections.<ResourceSlotAssignment>emptyList(),
                        Collections.<ComponentSlotAssignment>emptyList(),
                        CraftOutcomeTier.GREAT,
                        Collections.<ExperimentStep>emptyList())).getCraftState();
                components.add(new ComponentSlotAssignment(d,
                        Collections.singletonList(
                                new swg.crafting.simulator.components.ComponentUse(
                                        factory.create(id, id, id, sub),
                                        1))));
            }
        }

        List<ExperimentStep> experiments =
                new ArrayList<ExperimentStep>();
        if (experimentGroup != null && !experimentGroup.isEmpty()
                && experimentPoints > 0) {
            experiments.add(new ExperimentStep(
                    experimentGroup, experimentPoints,
                    CraftOutcomeTier.GOOD));
        }

        CraftScenario scenario = new CraftScenario(
                null, safeResources, components,
                CraftOutcomeTier.GREAT, experiments);

        return engine.execute(scenario);
    }

    /** Renders a {@link CraftResult} as a multi-line textual summary. */
    public static String renderResult(CraftResult result) {
        if (result == null) return "(no result)";
        StringBuilder sb = new StringBuilder();
        sb.append("Functional fields:\n");
        sb.append("  state coverage: ")
                .append(result.getCraftState().getEvidenceState())
                .append('\n');
        sb.append("Attributes:\n");
        for (Map.Entry<String, ?> e :
                result.getCraftState().getAttributes().entrySet()) {
            sb.append("  ").append(e.getKey())
                    .append(" = ").append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    /** Renders a delta between two builds on functional fields. */
    public static String compare(CraftResult a, CraftResult b) {
        if (a == null || b == null) return "(two builds required)";
        StringBuilder sb = new StringBuilder();
        sb.append("Compare: A vs B\n");
        for (Map.Entry<String, ?> e :
                a.getCraftState().getAttributes().entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ")
                    .append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    /** Renders a per-step arithmetic trace for the supplied result. */
    public static String explain(CraftResult result, SWGSchematic schematic) {
        if (result == null) return "(no result)";
        StringBuilder sb = new StringBuilder();
        sb.append("Explain: per-step trace\n");
        sb.append("  schematic: ")
                .append(schematic == null ? "(synthetic)" : schematic.getName())
                .append('\n');
        sb.append("  attributes computed: ")
                .append(result.getCraftState().getAttributes().size())
                .append('\n');
        return sb.toString();
    }
}
