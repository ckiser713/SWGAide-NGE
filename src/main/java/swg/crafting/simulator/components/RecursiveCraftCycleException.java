package swg.crafting.simulator.components;

import java.util.Collections;
import java.util.List;

/**
 * Typed exception thrown when recursive crafting evaluation encounters a
 * cycle in the crafted-component graph.
 *
 * <p>A depth bound is provided as defense in depth but is not the primary
 * validity gate; the DAG cycle check performed by
 * {@link RecursiveCraftCycleDetector} is. Any cycle, regardless of depth,
 * raises this exception.</p>
 */
public final class RecursiveCraftCycleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> cyclePath;

    public RecursiveCraftCycleException(List<String> cyclePath) {
        super("Recursive crafting cycle detected: "
                + String.valueOf(cyclePath));
        if (cyclePath == null) {
            throw new NullPointerException("cyclePath");
        }
        this.cyclePath = Collections.unmodifiableList(
                new java.util.ArrayList<String>(cyclePath));
    }

    public List<String> getCyclePath() {
        return cyclePath;
    }
}
