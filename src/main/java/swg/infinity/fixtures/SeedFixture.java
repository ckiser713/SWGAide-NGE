package swg.infinity.fixtures;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable source-backed seed fixture describing one Infinity craft scenario
 * with the canonical numeric inputs extracted from the pinned source tree.
 *
 * <p>Seeds contain only inputs — schematic structure, resource stats,
 * component definitions, experiment weights. Expected outputs are not pinned
 * here; the parity corpus (T6) computes them via the engine and compares.</p>
 *
 * <p>{@code provenance} is always {@code source-pinned}; the SHA and source
 * paths are mandatory. Hand-drafted values are not admissible.</p>
 */
public final class SeedFixture {

    private final String id;
    private final String sourceSha;
    private final String sourcePath;
    private final String schematicId;
    private final String family;
    private final String subcategory;
    private final String name;
    private final int complexity;
    private final int size;
    private final String xpType;
    private final int xp;
    private final String assemblySkill;
    private final String experimentingSkill;
    private final String customizationSkill;
    private final String targetTemplate;
    private final List<SeedSlot> slots;
    private final Map<String, Integer> resourceStats;
    private final List<SeedExperimentGroup> experimentGroups;

    public SeedFixture(
            String id,
            String sourceSha,
            String sourcePath,
            String schematicId,
            String family,
            String subcategory,
            String name,
            int complexity,
            int size,
            String xpType,
            int xp,
            String assemblySkill,
            String experimentingSkill,
            String customizationSkill,
            String targetTemplate,
            List<SeedSlot> slots,
            Map<String, Integer> resourceStats,
            List<SeedExperimentGroup> experimentGroups) {
        if (id == null) throw new NullPointerException("id");
        if (sourceSha == null) throw new NullPointerException("sourceSha");
        if (sourcePath == null) throw new NullPointerException("sourcePath");
        if (schematicId == null) throw new NullPointerException("schematicId");
        if (family == null) throw new NullPointerException("family");
        if (subcategory == null) throw new NullPointerException("subcategory");
        if (name == null) throw new NullPointerException("name");
        if (xpType == null) throw new NullPointerException("xpType");
        if (assemblySkill == null) throw new NullPointerException("assemblySkill");
        if (experimentingSkill == null) throw new NullPointerException("experimentingSkill");
        if (customizationSkill == null) throw new NullPointerException("customizationSkill");
        if (targetTemplate == null) throw new NullPointerException("targetTemplate");
        if (slots == null) throw new NullPointerException("slots");
        if (resourceStats == null) throw new NullPointerException("resourceStats");
        if (experimentGroups == null) throw new NullPointerException("experimentGroups");
        this.id = id;
        this.sourceSha = sourceSha;
        this.sourcePath = sourcePath;
        this.schematicId = schematicId;
        this.family = family;
        this.subcategory = subcategory;
        this.name = name;
        this.complexity = complexity;
        this.size = size;
        this.xpType = xpType;
        this.xp = xp;
        this.assemblySkill = assemblySkill;
        this.experimentingSkill = experimentingSkill;
        this.customizationSkill = customizationSkill;
        this.targetTemplate = targetTemplate;
        this.slots = Collections.unmodifiableList(
                new java.util.ArrayList<SeedSlot>(slots));
        this.resourceStats = Collections.unmodifiableMap(
                new java.util.LinkedHashMap<String, Integer>(resourceStats));
        this.experimentGroups = Collections.unmodifiableList(
                new java.util.ArrayList<SeedExperimentGroup>(experimentGroups));
    }

    public String getId() { return id; }
    public String getSourceSha() { return sourceSha; }
    public String getSourcePath() { return sourcePath; }
    public String getSchematicId() { return schematicId; }
    public String getFamily() { return family; }
    public String getSubcategory() { return subcategory; }
    public String getName() { return name; }
    public int getComplexity() { return complexity; }
    public int getSize() { return size; }
    public String getXpType() { return xpType; }
    public int getXp() { return xp; }
    public String getAssemblySkill() { return assemblySkill; }
    public String getExperimentingSkill() { return experimentingSkill; }
    public String getCustomizationSkill() { return customizationSkill; }
    public String getTargetTemplate() { return targetTemplate; }
    public List<SeedSlot> getSlots() { return slots; }
    public Map<String, Integer> getResourceStats() { return resourceStats; }
    public List<SeedExperimentGroup> getExperimentGroups() { return experimentGroups; }

    public static final class SeedSlot {
        private final int index;
        private final String title;
        private final String resourceType;
        private final int quantity;
        private final int contribution;
        private final int slotKind;

        public SeedSlot(int index, String title, String resourceType,
                        int quantity, int contribution, int slotKind) {
            if (title == null) throw new NullPointerException("title");
            if (resourceType == null) throw new NullPointerException("resourceType");
            this.index = index;
            this.title = title;
            this.resourceType = resourceType;
            this.quantity = quantity;
            this.contribution = contribution;
            this.slotKind = slotKind;
        }

        public int getIndex() { return index; }
        public String getTitle() { return title; }
        public String getResourceType() { return resourceType; }
        public int getQuantity() { return quantity; }
        public int getContribution() { return contribution; }
        public int getSlotKind() { return slotKind; }
    }

    public static final class SeedExperimentGroup {
        private final String attribute;
        private final String group;
        private final double minValue;
        private final double maxValue;
        private final int precision;

        public SeedExperimentGroup(String attribute, String group,
                                   double minValue, double maxValue,
                                   int precision) {
            if (attribute == null) throw new NullPointerException("attribute");
            if (group == null) throw new NullPointerException("group");
            this.attribute = attribute;
            this.group = group;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.precision = precision;
        }

        public String getAttribute() { return attribute; }
        public String getGroup() { return group; }
        public double getMinValue() { return minValue; }
        public double getMaxValue() { return maxValue; }
        public int getPrecision() { return precision; }
    }
}
