package swg.crafting.simulator.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Compatible native resource candidates for one raw-resource requirement. */
public final class ResourceCandidateSet {
    private final ResourceRequirement requirement;
    private final List<ResourceSnapshot> candidates;

    public ResourceCandidateSet(
            ResourceRequirement requirement,
            List<ResourceSnapshot> candidates) {
        if (requirement == null) throw new NullPointerException("requirement");
        if (candidates == null) throw new NullPointerException("candidates");
        this.requirement = requirement;
        this.candidates = Collections.unmodifiableList(
                new ArrayList<ResourceSnapshot>(candidates));
    }

    public ResourceRequirement getRequirement() { return requirement; }
    public List<ResourceSnapshot> getCandidates() { return candidates; }
}
