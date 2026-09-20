package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import swg.infinity.engine.CraftResult;
import swg.infinity.processor.FunctionalItemResult;

/** Deterministic final-item comparison; no separate scoring formula exists. */
public final class CraftComparator {

    public CraftComparison compare(
            CraftResult baseline,
            CraftResult candidate) {
        if (baseline == null) throw new NullPointerException("baseline");
        if (candidate == null) throw new NullPointerException("candidate");

        FunctionalItemResult left = baseline.getFunctionalResult();
        FunctionalItemResult right = candidate.getFunctionalResult();

        if (!left.getProcessorId().equals(right.getProcessorId())) {
            throw new IllegalArgumentException(
                    "Cannot compare different processors: "
                    + left.getProcessorId() + " vs " + right.getProcessorId());
        }

        Set<String> orderedKeys = new LinkedHashSet<String>();
        orderedKeys.addAll(left.getValues().keySet());
        orderedKeys.addAll(right.getValues().keySet());

        List<ResultDelta> deltas = new ArrayList<ResultDelta>();
        for (String key : orderedKeys) {
            deltas.add(new ResultDelta(key, left.get(key), right.get(key)));
        }
        return new CraftComparison(deltas);
    }
}
