package swg.crafting.simulator.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable ordered comparison of two completed craft results. */
public final class CraftComparison {
    private final List<ResultDelta> deltas;

    public CraftComparison(List<ResultDelta> deltas) {
        if (deltas == null) throw new NullPointerException("deltas");
        this.deltas = Collections.unmodifiableList(
                new ArrayList<ResultDelta>(deltas));
    }

    public List<ResultDelta> getDeltas() {
        return deltas;
    }

    public ResultDelta get(String attribute) {
        for (ResultDelta delta : deltas) {
            if (delta.getAttribute().equals(attribute)) return delta;
        }
        return null;
    }
}
