package swg.infinity.component;

import swg.crafting.simulator.components.ComponentInstance;
import swg.crafting.simulator.components.RecursiveCraftCycleDetector;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.engine.InfinityCraftService;

/**
 * Executes one nested component craft through the same exact-coverage service
 * as a top-level craft and converts its completed CraftState into a reusable
 * ComponentInstance for the parent scenario.
 *
 * <p>Callers share one {@link RecursiveCraftCycleDetector} across the nested
 * evaluation graph. The detector is the authoritative cycle gate; its depth
 * bound is defense in depth.</p>
 */
public final class RecursiveComponentCraftService {
    private final InfinityCraftService craftService;
    private final CraftedComponentFactory componentFactory;

    public RecursiveComponentCraftService() {
        this(new InfinityCraftService(), new CraftedComponentFactory());
    }

    RecursiveComponentCraftService(
            InfinityCraftService craftService,
            CraftedComponentFactory componentFactory) {
        if (craftService == null) throw new NullPointerException("craftService");
        if (componentFactory == null) throw new NullPointerException("componentFactory");
        this.craftService = craftService;
        this.componentFactory = componentFactory;
    }

    public ComponentInstance craft(
            String nodeId,
            String componentId,
            String templateId,
            String serial,
            InfinityRuleset ruleset,
            CraftScenario scenario,
            RecursiveCraftCycleDetector detector) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (detector == null) throw new NullPointerException("detector");

        detector.enter(nodeId);
        try {
            CraftResult result =
                    craftService.executeExact(ruleset, scenario);
            return componentFactory.create(
                    componentId,
                    templateId,
                    serial,
                    result.getCraftState());
        } finally {
            detector.leave(nodeId);
        }
    }
}
