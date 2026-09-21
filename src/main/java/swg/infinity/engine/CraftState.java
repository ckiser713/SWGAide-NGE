package swg.infinity.engine;
import swg.crafting.simulator.scenario.AttributeState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.SchematicDefinition;

/** Immutable deterministic state of one craft calculation. */
public final class CraftState {
    private final SchematicDefinition schematic;
    private final EvidenceState evidenceState;
    private final Map<String, AttributeState> attributes;
    private final List<String> warnings;

    public CraftState(
            SchematicDefinition schematic,
            EvidenceState evidenceState,
            Map<String, AttributeState> attributes,
            List<String> warnings) {
        if (schematic == null) throw new NullPointerException("schematic");
        if (evidenceState == null) throw new NullPointerException("evidenceState");
        if (attributes == null) throw new NullPointerException("attributes");
        if (warnings == null) throw new NullPointerException("warnings");
        this.schematic = schematic;
        this.evidenceState = evidenceState;
        this.attributes = Collections.unmodifiableMap(
                new LinkedHashMap<String, AttributeState>(attributes));
        this.warnings = Collections.unmodifiableList(
                new ArrayList<String>(warnings));
    }

    public SchematicDefinition getSchematic() { return schematic; }
    public EvidenceState getEvidenceState() { return evidenceState; }
    public Map<String, AttributeState> getAttributes() { return attributes; }
    public List<String> getWarnings() { return warnings; }

    public AttributeState getAttribute(String name) {
        return attributes.get(name);
    }

    CraftState replaceAttributes(Map<String, AttributeState> replacement) {
        return new CraftState(schematic, evidenceState, replacement, warnings);
    }

    /**
     * Returns a new state with replacement calculation data.
     *
     * <p>Public so independent engine subpackages such as component processors
     * can remain isolated without mutating this object.</p>
     */
    public CraftState withCalculationData(
            Map<String, AttributeState> replacement,
            List<String> replacementWarnings) {
        return new CraftState(
                schematic, evidenceState, replacement, replacementWarnings);
    }
}
