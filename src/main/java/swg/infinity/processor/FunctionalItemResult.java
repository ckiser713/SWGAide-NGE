package swg.infinity.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.EvidenceState;

/** Immutable functional result after object-specific post-processing. */
public final class FunctionalItemResult {
    private final String processorId;
    private final EvidenceState evidenceState;
    private final Map<String, Double> values;
    private final List<String> warnings;

    public FunctionalItemResult(
            String processorId,
            EvidenceState evidenceState,
            Map<String, Double> values,
            List<String> warnings) {
        if (processorId == null || processorId.trim().isEmpty()) {
            throw new IllegalArgumentException("processorId must not be blank");
        }
        if (evidenceState == null) throw new NullPointerException("evidenceState");
        if (values == null) throw new NullPointerException("values");
        if (warnings == null) throw new NullPointerException("warnings");
        this.processorId = processorId;
        this.evidenceState = evidenceState;
        this.values = Collections.unmodifiableMap(
                new LinkedHashMap<String, Double>(values));
        this.warnings = Collections.unmodifiableList(
                new ArrayList<String>(warnings));
    }

    public String getProcessorId() { return processorId; }
    public EvidenceState getEvidenceState() { return evidenceState; }
    public Map<String, Double> getValues() { return values; }
    public List<String> getWarnings() { return warnings; }

    public Double get(String key) {
        return values.get(key);
    }
}
