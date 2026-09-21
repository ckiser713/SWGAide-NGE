package swg.infinity.contracts;
import swg.crafting.simulator.contracts.Provenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source-derived immutable schematic definition used by the Infinity engine.
 *
 * <p>SWGAide schematic IDs are deliberately not canonical identity. Adapter
 * bindings map a source schematic identity to SWGAide separately.</p>
 */
public final class SchematicDefinition {
    private final String id;
    private final String displayName;
    private final String draftTemplate;
    private final String targetTemplate;
    private final LaboratoryType laboratory;
    private final String assemblySkill;
    private final String experimentationSkill;
    private final List<IngredientSlotDefinition> slots;
    private final List<ExperimentalProperty> properties;
    private final String processorId;
    private final Provenance provenance;

    public SchematicDefinition(
            String id,
            String displayName,
            String draftTemplate,
            String targetTemplate,
            LaboratoryType laboratory,
            String assemblySkill,
            String experimentationSkill,
            List<IngredientSlotDefinition> slots,
            List<ExperimentalProperty> properties,
            String processorId,
            Provenance provenance) {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.draftTemplate = requireText(draftTemplate, "draftTemplate");
        this.targetTemplate = requireText(targetTemplate, "targetTemplate");
        if (laboratory == null) throw new NullPointerException("laboratory");
        this.laboratory = laboratory;
        this.assemblySkill = assemblySkill == null ? "" : assemblySkill;
        this.experimentationSkill =
                experimentationSkill == null ? "" : experimentationSkill;
        if (slots == null) throw new NullPointerException("slots");
        if (properties == null) throw new NullPointerException("properties");
        this.slots = Collections.unmodifiableList(
                new ArrayList<IngredientSlotDefinition>(slots));
        this.properties = Collections.unmodifiableList(
                new ArrayList<ExperimentalProperty>(properties));
        this.processorId = requireText(processorId, "processorId");
        if (provenance == null) throw new NullPointerException("provenance");
        this.provenance = provenance;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDraftTemplate() { return draftTemplate; }
    public String getTargetTemplate() { return targetTemplate; }
    public LaboratoryType getLaboratory() { return laboratory; }
    public String getAssemblySkill() { return assemblySkill; }
    public String getExperimentationSkill() { return experimentationSkill; }
    public List<IngredientSlotDefinition> getSlots() { return slots; }
    public List<ExperimentalProperty> getProperties() { return properties; }
    public String getProcessorId() { return processorId; }
    public Provenance getProvenance() { return provenance; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
