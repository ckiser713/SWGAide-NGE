package swg.crafting.simulator.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/** Dependency-free tests for explicit per-slot X/Y/Z candidate selection. */
public final class ResourceCandidateMatcherSelfTest {
    private ResourceCandidateMatcherSelfTest() {
    }

    public static void main(String[] args) {
        ResourceRequirement metal = new ResourceRequirement(
                0, "frame", "metal", 20);

        ResourceSnapshot steelA = resource("A", "steel", "metal");
        ResourceSnapshot steelB = resource("B", "steel", "metal");
        ResourceSnapshot gas = resource("Gas", "gas", "gas");

        java.util.List<ResourceCandidateSet> sets =
                new ResourceCandidateMatcher().match(
                        Collections.singletonList(metal),
                        Arrays.asList(steelB, gas, steelA));

        if (sets.size() != 1 || sets.get(0).getCandidates().size() != 2) {
            throw new AssertionError("metal candidate filtering failed");
        }
        if (!"A".equals(sets.get(0).getCandidates().get(0).getName())
                || !"B".equals(
                    sets.get(0).getCandidates().get(1).getName())) {
            throw new AssertionError("candidate ordering is not deterministic");
        }

        System.out.println("ResourceCandidateMatcherSelfTest PASS");
    }

    private static ResourceSnapshot resource(
            String name, String type, String parent) {
        return new ResourceSnapshot(
                154,
                name,
                type,
                type,
                new LinkedHashSet<String>(Arrays.asList(type, parent)),
                new LinkedHashSet<String>(Arrays.asList(type, parent)),
                new LinkedHashMap<String, Integer>(),
                -1L,
                ResourceSource.CURRENT);
    }
}
