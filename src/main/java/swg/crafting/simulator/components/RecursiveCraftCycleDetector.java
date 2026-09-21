package swg.crafting.simulator.components;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic DAG cycle detector used by recursive crafting. The detector walks
 * nodes identified by caller-provided string keys and follows caller-supplied
 * outgoing edges. Any revisit of a node still on the current path is a cycle
 * and raises {@link RecursiveCraftCycleException}.
 *
 * <p>A configurable depth bound (default 64) is provided as defense in depth
 * but is never the primary validity gate. The DAG cycle check is. Callers
 * that want the bound enforced should wrap this detector with their own
 * check; this detector only signals cycles.</p>
 *
 * <p>The detector holds no shared state between {@link #enter(String)} and
 * {@link #leave(String)} pairs; it is safe to construct a fresh instance per
 * evaluation.</p>
 */
public final class RecursiveCraftCycleDetector {

    /** Default defense-in-depth depth bound. Not a validity gate. */
    public static final int DEFAULT_DEPTH_BOUND = 64;

    private final int depthBound;
    private final Set<String> visited =
            new HashSet<String>();
    private final Map<String, Integer> onStackIndex =
            new HashMap<String, Integer>();
    private final Deque<String> path =
            new ArrayDeque<String>();
    private final List<String> cyclePath =
            new ArrayList<String>();

    public RecursiveCraftCycleDetector() {
        this(DEFAULT_DEPTH_BOUND);
    }

    public RecursiveCraftCycleDetector(int depthBound) {
        if (depthBound < 1) {
            throw new IllegalArgumentException("depthBound must be >= 1");
        }
        this.depthBound = depthBound;
    }

    /**
     * Records that {@code node} is being entered. If {@code node} is already on
     * the current DFS stack, this is a cycle and
     * {@link RecursiveCraftCycleException} is thrown with the cycle path.
     */
    public void enter(String node) {
        if (node == null) throw new NullPointerException("node");
        Integer existing = onStackIndex.get(node);
        if (existing != null) {
            cyclePath.clear();
            int idx = existing.intValue();
            for (String element : path) {
                cyclePath.add(element);
                if (element.equals(node) && cyclePath.size() > idx) {
                    break;
                }
            }
            cyclePath.add(node);
            throw new RecursiveCraftCycleException(
                    new ArrayList<String>(cyclePath));
        }
        if (path.size() >= depthBound) {
            throw new RecursiveCraftCycleException(
                    new ArrayList<String>(path));
        }
        onStackIndex.put(node, Integer.valueOf(path.size()));
        path.push(node);
    }

    /** Records that {@code node} has been fully processed and is leaving the DFS stack. */
    public void leave(String node) {
        if (node == null) throw new NullPointerException("node");
        String top = path.poll();
        if (top == null || !top.equals(node)) {
            // Restore stack to a sane state for the next caller.
            path.clear();
            onStackIndex.clear();
            visited.clear();
            throw new IllegalStateException(
                    "leave() mismatch: expected " + node + " got " + top);
        }
        onStackIndex.remove(node);
        visited.add(node);
    }

    /** Returns true if {@code node} has been fully processed in this walk. */
    public boolean isVisited(String node) {
        if (node == null) return false;
        return visited.contains(node);
    }

    /**
     * Convenience: walks {@code root} using {@code successors} for outgoing
     * edges. Equivalent to a recursive DFS guarded by {@link #enter} /
     * {@link #leave}.
     */
    public void walk(String root, java.util.function.Function<String, List<String>> successors) {
        if (root == null) throw new NullPointerException("root");
        if (successors == null) throw new NullPointerException("successors");
        enter(root);
        List<String> next = successors.apply(root);
        if (next != null) {
            for (String child : next) {
                if (!isVisited(child)) {
                    walk(child, successors);
                }
            }
        }
        leave(root);
    }

    public int getDepthBound() {
        return depthBound;
    }

    /** Returns the current DFS path (defensive copy). */
    public List<String> currentPath() {
        return new ArrayList<String>(path);
    }
}
