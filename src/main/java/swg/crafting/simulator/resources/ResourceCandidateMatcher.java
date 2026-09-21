package swg.crafting.simulator.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Provider-neutral candidate matching by resource-class ancestry.
 *
 * <p>No craft-value ranking happens here. The UI may order candidates by name
 * while Compare/optimizer paths must run complete craft scenarios through the
 * active server module.</p>
 */
public final class ResourceCandidateMatcher {

    public List<ResourceCandidateSet> match(
            List<ResourceRequirement> requirements,
            List<ResourceSnapshot> resources) {
        if (requirements == null) throw new NullPointerException("requirements");
        if (resources == null) throw new NullPointerException("resources");

        List<ResourceCandidateSet> out =
                new ArrayList<ResourceCandidateSet>(requirements.size());

        for (ResourceRequirement requirement : requirements) {
            if (requirement == null) throw new NullPointerException("requirement");
            List<ResourceSnapshot> candidates =
                    new ArrayList<ResourceSnapshot>();
            for (ResourceSnapshot resource : resources) {
                if (resource != null
                        && resource.matchesAcceptedType(
                                requirement.getAcceptedType())) {
                    candidates.add(resource);
                }
            }
            Collections.sort(
                    candidates,
                    new Comparator<ResourceSnapshot>() {
                        @Override
                        public int compare(
                                ResourceSnapshot left,
                                ResourceSnapshot right) {
                            return left.getName()
                                    .compareToIgnoreCase(right.getName());
                        }
                    });
            out.add(new ResourceCandidateSet(requirement, candidates));
        }

        return Collections.unmodifiableList(out);
    }
}
