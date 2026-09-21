package swg.infinity.analysis;
import swg.crafting.simulator.explain.CraftScenarioTransforms;
import swg.crafting.simulator.compare.CraftComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.infinity.contracts.InfinityRuleset;
import swg.crafting.simulator.scenario.CraftResult;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.infinity.engine.InfinityCraftService;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.UnsupportedInfinityRuleException;

/**
 * Evaluates X/Y/Z resource substitutions through the same exact craft service
 * used by ordinary calculations.
 */
public final class ResourceCandidateAnalyzer {
    private final InfinityCraftService service;
    private final CraftComparator comparator;

    public ResourceCandidateAnalyzer() {
        this(new InfinityCraftService(), new CraftComparator());
    }

    public ResourceCandidateAnalyzer(
            InfinityCraftService service,
            CraftComparator comparator) {
        if (service == null) throw new NullPointerException("service");
        if (comparator == null) throw new NullPointerException("comparator");
        this.service = service;
        this.comparator = comparator;
    }

    public List<ResourceCandidateEvaluation> evaluate(
            InfinityRuleset ruleset,
            CraftScenario baseline,
            int slotIndex,
            List<ResourceInput> candidates) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        if (baseline == null) throw new NullPointerException("baseline");
        if (candidates == null) throw new NullPointerException("candidates");

        CraftResult baselineResult = service.executeExact(ruleset, baseline);
        List<ResourceCandidateEvaluation> results =
                new ArrayList<ResourceCandidateEvaluation>();

        for (ResourceInput candidate : candidates) {
            if (candidate == null) throw new NullPointerException("candidate");
            try {
                CraftScenario scenario =
                        CraftScenarioTransforms.withResource(
                                baseline, slotIndex, candidate);
                CraftResult candidateResult =
                        service.executeExact(ruleset, scenario);
                results.add(ResourceCandidateEvaluation.accepted(
                        candidate,
                        candidateResult,
                        comparator.compare(baselineResult, candidateResult)));
            } catch (IllegalArgumentException e) {
                results.add(ResourceCandidateEvaluation.rejected(
                        candidate, e.getMessage()));
            } catch (IllegalStateException e) {
                results.add(ResourceCandidateEvaluation.rejected(
                        candidate, e.getMessage()));
            } catch (UnsupportedInfinityRuleException e) {
                results.add(ResourceCandidateEvaluation.rejected(
                        candidate, e.getMessage()));
            }
        }

        return Collections.unmodifiableList(results);
    }
}
