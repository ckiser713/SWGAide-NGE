package swg.infinity.analysis;
import swg.crafting.simulator.compare.CraftComparison;

import swg.crafting.simulator.scenario.CraftResult;
import swg.infinity.engine.ResourceInput;

/** Accepted or rejected result for one candidate resource substitution. */
public final class ResourceCandidateEvaluation {
    private final ResourceInput resource;
    private final CraftResult result;
    private final CraftComparison comparison;
    private final String rejection;

    private ResourceCandidateEvaluation(
            ResourceInput resource,
            CraftResult result,
            CraftComparison comparison,
            String rejection) {
        if (resource == null) throw new NullPointerException("resource");
        this.resource = resource;
        this.result = result;
        this.comparison = comparison;
        this.rejection = rejection == null ? "" : rejection;
    }

    public static ResourceCandidateEvaluation accepted(
            ResourceInput resource,
            CraftResult result,
            CraftComparison comparison) {
        if (result == null) throw new NullPointerException("result");
        if (comparison == null) throw new NullPointerException("comparison");
        return new ResourceCandidateEvaluation(resource, result, comparison, "");
    }

    public static ResourceCandidateEvaluation rejected(
            ResourceInput resource,
            String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        return new ResourceCandidateEvaluation(resource, null, null, reason);
    }

    public ResourceInput getResource() { return resource; }
    public CraftResult getResult() { return result; }
    public CraftComparison getComparison() { return comparison; }
    public String getRejection() { return rejection; }
    public boolean isAccepted() { return result != null; }
}
