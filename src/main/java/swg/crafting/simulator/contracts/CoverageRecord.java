package swg.crafting.simulator.contracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Machine-readable coverage status for one schematic path. */
public final class CoverageRecord {
    private final String schematicId;
    private final EvidenceState extraction;
    private final EvidenceState laboratory;
    private final EvidenceState components;
    private final EvidenceState processor;
    private final List<String> fixtureIds;

    public CoverageRecord(
            String schematicId,
            EvidenceState extraction,
            EvidenceState laboratory,
            EvidenceState components,
            EvidenceState processor,
            List<String> fixtureIds) {
        if (schematicId == null || schematicId.trim().isEmpty()) {
            throw new IllegalArgumentException("schematicId must not be blank");
        }
        if (extraction == null || laboratory == null
                || components == null || processor == null) {
            throw new NullPointerException("coverage evidence state");
        }
        if (fixtureIds == null) throw new NullPointerException("fixtureIds");
        this.schematicId = schematicId;
        this.extraction = extraction;
        this.laboratory = laboratory;
        this.components = components;
        this.processor = processor;
        this.fixtureIds = Collections.unmodifiableList(
                new ArrayList<String>(fixtureIds));
    }

    public String getSchematicId() { return schematicId; }
    public EvidenceState getExtraction() { return extraction; }
    public EvidenceState getLaboratory() { return laboratory; }
    public EvidenceState getComponents() { return components; }
    public EvidenceState getProcessor() { return processor; }
    public List<String> getFixtureIds() { return fixtureIds; }

    public EvidenceState overall() {
        EvidenceState[] states = { extraction, laboratory, components, processor };
        for (EvidenceState state : states) {
            if (state == EvidenceState.UNKNOWN) return EvidenceState.UNKNOWN;
            if (state == EvidenceState.UNSUPPORTED) return EvidenceState.UNSUPPORTED;
            if (state == EvidenceState.PARTIAL) return EvidenceState.PARTIAL;
            if (state == EvidenceState.SIMULATED) return EvidenceState.SIMULATED;
        }
        for (EvidenceState state : states) {
            if (state == EvidenceState.EXACT_WITH_FORCED_OUTCOME) {
                return EvidenceState.EXACT_WITH_FORCED_OUTCOME;
            }
        }
        return EvidenceState.EXACT;
    }
}
