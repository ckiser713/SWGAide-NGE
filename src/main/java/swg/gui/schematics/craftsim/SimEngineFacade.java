package swg.gui.schematics.craftsim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import swg.crafting.schematics.SWGSchematic;
import swg.crafting.simulator.compare.CraftComparator;
import swg.crafting.simulator.compare.CraftComparison;
import swg.crafting.simulator.explain.CraftExplanation;
import swg.crafting.simulator.explain.CraftExplainer;
import swg.crafting.simulator.planning.MaterialPlan;
import swg.crafting.simulator.planning.MaterialPlanner;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.engine.InfinityCraftEngine;
import swg.infinity.engine.ResourceSlotAssignment;
import swg.infinity.integration.SchematicBinding;
import swg.infinity.integration.SchematicBindingRegistry;

/**
 * Thin wrapper around the Infinity craft engine that the Crafting Simulator
 * tab calls. Maps a selected SWGAide {@link SWGSchematic} through a
 * {@link SchematicBindingRegistry} against the active {@link InfinityRuleset},
 * gathers the user-selected native resources into
 * {@link ResourceSlotAssignment} entries, attaches any exact/manual
 * component inputs, and executes the resulting {@link CraftScenario}
 * via {@link InfinityCraftEngine}.
 *
 * <p>The facade exposes the build/run/compare/explain/plan operations
 * that the tab uses so the same code path is exercised by tests and
 * by the GUI.</p>
 */
public final class SimEngineFacade {
    private SimEngineFacade() {
        throw new AssertionError("Do not instantiate");
    }

    /** Outcome of resolving a binding for a selected schematic. */
    public static final class BoundSchematic {
        public final SWGSchematic swgSchematic;
        public final SchematicBinding binding;
        public final SchematicDefinition definition;
        public final boolean runnable;

        BoundSchematic(
                SWGSchematic swgSchematic,
                SchematicBinding binding,
                SchematicDefinition definition,
                boolean runnable) {
            this.swgSchematic = swgSchematic;
            this.binding = binding;
            this.definition = definition;
            this.runnable = runnable;
        }
    }

    /**
     * Resolves the binding for the supplied SWGAide schematic against
     * the supplied registry + ruleset. Returns a {@link BoundSchematic}
     * carrying either a runnable {@link SchematicDefinition} (VERIFIED
     * or MANUAL_OVERRIDE binding with a present ruleset entry) or the
     * unresolved binding so the caller can render the AMBIGUOUS /
     * MISSING state.
     */
    public static BoundSchematic resolve(
            SWGSchematic schematic,
            SchematicBindingRegistry registry,
            InfinityRuleset ruleset) {
        if (schematic == null) throw new NullPointerException("schematic");
        return resolveById(schematic.getID(), schematic, registry, ruleset);
    }

    /**
     * Resolves the binding for the supplied SWGAide-side schematic id
     * without requiring an SWGSchematic instance. The returned
     * {@link BoundSchematic} carries a {@code null} swgSchematic.
     */
    public static BoundSchematic resolveById(
            int swgAideSchematicId,
            SchematicBindingRegistry registry,
            InfinityRuleset ruleset) {
        return resolveById(swgAideSchematicId, null, registry, ruleset);
    }

    private static BoundSchematic resolveById(
            int swgAideSchematicId,
            SWGSchematic schematic,
            SchematicBindingRegistry registry,
            InfinityRuleset ruleset) {
        if (registry == null) throw new NullPointerException("registry");
        if (ruleset == null) throw new NullPointerException("ruleset");
        SchematicBinding binding = registry.get(swgAideSchematicId);
        if (binding == null || !binding.getState().isRunnable()) {
            return new BoundSchematic(schematic, binding, null, false);
        }
        SchematicDefinition definition =
                ruleset.getSchematic(binding.getInfinitySchematicId());
        if (definition == null) {
            return new BoundSchematic(schematic, binding, null, false);
        }
        return new BoundSchematic(schematic, binding, definition, true);
    }

    /**
     * Builds a {@link CraftScenario} from the bound schematic + the
     * user-selected resources, components, and experiment steps. The
     * supplied {@link BoundSchematic} must be runnable; otherwise the
     * call fails fast.
     */
    public static CraftScenario buildScenario(
            BoundSchematic bound,
            List<ResourceSlotAssignment> resources,
            List<ComponentSlotAssignment> components,
            CraftOutcomeTier assemblyOutcome,
            List<ExperimentStep> experiments) {
        if (bound == null || !bound.runnable) {
            throw new IllegalStateException(
                    "Schematic has no runnable Infinity binding");
        }
        return new CraftScenario(
                bound.definition,
                resources == null
                        ? Collections.<ResourceSlotAssignment>emptyList()
                        : resources,
                components == null
                        ? Collections.<ComponentSlotAssignment>emptyList()
                        : components,
                assemblyOutcome == null
                        ? CraftOutcomeTier.GREAT : assemblyOutcome,
                experiments == null
                        ? Collections.<ExperimentStep>emptyList()
                        : experiments);
    }

    /**
     * Runs the supplied scenario through the deterministic
     * {@link InfinityCraftEngine}. Convenience wrapper for the tab.
     */
    public static CraftResult run(CraftScenario scenario) {
        return new InfinityCraftEngine().execute(scenario);
    }

    /** Compares two complete builds on functional fields. */
    public static CraftComparison compare(CraftResult baseline, CraftResult candidate) {
        return new CraftComparator().compare(baseline, candidate);
    }

    /**
     * Explains the supplied result with the per-step arithmetic trace.
     * The scenario is the one returned by {@link #buildScenario}.
     */
    public static CraftExplanation explain(
            CraftScenario scenario, CraftResult result) {
        return new CraftExplainer().explain(scenario, result);
    }

    /**
     * Computes a {@link MaterialPlan} for {@code craftCount} repetitions
     * of the supplied scenario.
     */
    public static MaterialPlan plan(CraftScenario scenario, int craftCount) {
        return new MaterialPlanner().plan(scenario, craftCount);
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

    /** Renders a {@link CraftComparison} as text. */
    public static String renderCompare(CraftComparison comparison) {
        if (comparison == null) return "(no comparison)";
        StringBuilder sb = new StringBuilder();
        sb.append("Compare: A vs B (deltas)\n");
        for (Object o : comparison.getDeltas()) {
            sb.append("  ").append(String.valueOf(o)).append('\n');
        }
        return sb.toString();
    }

    /** Renders a {@link CraftExplanation} as text. */
    public static String renderExplain(CraftExplanation explanation) {
        if (explanation == null) return "(no explanation)";
        StringBuilder sb = new StringBuilder();
        sb.append("Explain: per-step trace\n");
        for (Object o : explanation.getAttributes()) {
            sb.append("  ").append(String.valueOf(o)).append('\n');
        }
        return sb.toString();
    }

    /** Renders a {@link MaterialPlan} as text. */
    public static String renderPlan(MaterialPlan plan) {
        if (plan == null) return "(no plan)";
        StringBuilder sb = new StringBuilder();
        sb.append("Materials plan: ").append(plan.getCraftCount())
                .append(" craft(s)\n");
        sb.append("Resources:\n");
        for (Map.Entry<String, Long> e : plan.getResourceUnits().entrySet()) {
            sb.append("  ").append(e.getKey())
                    .append(" x ").append(e.getValue()).append('\n');
        }
        sb.append("Components:\n");
        for (Map.Entry<String, Long> e : plan.getComponentUses().entrySet()) {
            sb.append("  ").append(e.getKey())
                    .append(" x ").append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    /**
     * Convenience for callers that already have a bound definition
     * and want to attach a single experiment group.
     */
    public static List<ExperimentStep> singleExperiment(
            String group, int points, CraftOutcomeTier outcome) {
        if (group == null || group.isEmpty()) {
            return Collections.<ExperimentStep>emptyList();
        }
        return Collections.unmodifiableList(
                new ArrayList<ExperimentStep>(
                        Arrays.asList(new ExperimentStep(
                                group, points,
                                outcome == null
                                        ? CraftOutcomeTier.GOOD : outcome))));
    }
}
