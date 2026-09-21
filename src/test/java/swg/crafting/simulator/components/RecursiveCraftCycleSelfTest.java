package swg.crafting.simulator.components;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Verifies the generic recursive-crafting cycle detector:
 * - acyclic graphs complete without exception;
 * - direct self-cycles raise {@link RecursiveCraftCycleException};
 * - longer cycles raise {@link RecursiveCraftCycleException} with a non-empty path;
 * - the defense-in-depth depth bound is reached but the cycle check is
 *   authoritative: any revisit raises an exception, regardless of depth.
 */
public final class RecursiveCraftCycleSelfTest {
    private RecursiveCraftCycleSelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    public static void main(String[] args) {
        // 1) acyclic graph completes cleanly
        RecursiveCraftCycleDetector detector =
                new RecursiveCraftCycleDetector();
        detector.walk("root", node -> {
            if ("root".equals(node)) return Arrays.asList("a", "b");
            if ("a".equals(node))    return Collections.singletonList("c");
            if ("b".equals(node))    return Collections.emptyList();
            if ("c".equals(node))    return Collections.emptyList();
            return Collections.emptyList();
        });
        if (!detector.isVisited("root")) {
            throw new AssertionError("root should be visited after walk");
        }

        // 2) direct self-cycle raises the typed exception
        boolean selfRejected = false;
        RecursiveCraftCycleDetector selfDetector =
                new RecursiveCraftCycleDetector();
        try {
            selfDetector.walk("a", node -> Collections.singletonList("a"));
        } catch (RecursiveCraftCycleException expected) {
            selfRejected = true;
            if (expected.getCyclePath().isEmpty()) {
                throw new AssertionError("cycle path must not be empty");
            }
        }
        if (!selfRejected) {
            throw new AssertionError("direct self-cycle accepted");
        }

        // 3) longer cycle: a -> b -> c -> a
        boolean cycleRejected = false;
        RecursiveCraftCycleDetector cycleDetector =
                new RecursiveCraftCycleDetector();
        try {
            cycleDetector.walk("a", node -> {
                if ("a".equals(node)) return Collections.singletonList("b");
                if ("b".equals(node)) return Collections.singletonList("c");
                if ("c".equals(node)) return Collections.singletonList("a");
                return Collections.emptyList();
            });
        } catch (RecursiveCraftCycleException expected) {
            cycleRejected = true;
            List<String> path = expected.getCyclePath();
            if (path.size() < 2) {
                throw new AssertionError(
                        "cycle path should contain at least 2 nodes");
            }
        }
        if (!cycleRejected) {
            throw new AssertionError("a->b->c->a cycle accepted");
        }

        // 4) defense-in-depth depth bound is enforced at the default
        RecursiveCraftCycleDetector deep =
                new RecursiveCraftCycleDetector();
        boolean boundRejected = false;
        try {
            deep.walk("n0", node -> {
                int depth = Integer.parseInt(node.substring(1));
                if (depth >= 200) return Collections.emptyList();
                return Collections.singletonList("n" + (depth + 1));
            });
        } catch (RecursiveCraftCycleException expected) {
            boundRejected = true;
        }
        if (!boundRejected) {
            throw new AssertionError("depth bound not enforced");
        }

        System.out.println("RecursiveCraftCycleSelfTest PASS");
    }
}
