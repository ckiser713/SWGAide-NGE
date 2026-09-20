package swg.infinity.engine;

import swg.infinity.component.ComponentCombiner;
import swg.infinity.processor.FunctionalItemResult;
import swg.infinity.processor.ProcessorRegistry;
import swg.infinity.processor.ResultProcessor;

/**
 * Deterministic orchestration boundary for one Infinity craft scenario.
 *
 * <p>Order mirrors the generic Resource Laboratory source path: resource and
 * custom-ingredient weighted initialization/assembly, component stat
 * application, experimentation, then final object processing.</p>
 */
public final class InfinityCraftEngine {
    private final ResourceLaboratory resourceLaboratory;
    private final ComponentCombiner componentCombiner;
    private final ProcessorRegistry processors;

    public InfinityCraftEngine() {
        this(new ResourceLaboratory(), new ComponentCombiner(), new ProcessorRegistry());
    }

    public InfinityCraftEngine(
            ResourceLaboratory resourceLaboratory,
            ComponentCombiner componentCombiner,
            ProcessorRegistry processors) {
        if (resourceLaboratory == null) throw new NullPointerException("resourceLaboratory");
        if (componentCombiner == null) throw new NullPointerException("componentCombiner");
        if (processors == null) throw new NullPointerException("processors");
        this.resourceLaboratory = resourceLaboratory;
        this.componentCombiner = componentCombiner;
        this.processors = processors;
    }

    public CraftResult execute(CraftScenario scenario) {
        if (scenario == null) throw new NullPointerException("scenario");

        CraftState state = resourceLaboratory.initialize(
                scenario.getSchematic(),
                scenario.getResources(),
                scenario.getComponents(),
                scenario.getAssemblyOutcome());

        state = componentCombiner.apply(state, scenario.getComponents());

        for (ExperimentStep step : scenario.getExperiments()) {
            state = resourceLaboratory.experiment(
                    state,
                    step.getGroup(),
                    step.getPoints(),
                    step.getOutcome());
        }

        ResultProcessor processor =
                processors.require(scenario.getSchematic().getProcessorId());
        FunctionalItemResult functional = processor.process(state);
        return new CraftResult(state, functional);
    }
}
