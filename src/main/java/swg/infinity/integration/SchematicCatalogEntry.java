package swg.infinity.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provider-neutral description of a SWGAide-side schematic used as
 * input to {@link BindingBuilder}. Decouples binding logic from the
 * native {@code SWGSchematic} class so the binding pipeline can be
 * exercised headlessly in tests.
 */
public final class SchematicCatalogEntry {
    private final int swgAideServerId;
    private final int swgAideSchematicId;
    private final String name;
    private final int resourceSlotCount;
    private final List<String> resourceSlotKinds;
    private final List<String> experimentGroupTitles;
    private final String targetTemplateHint;

    public SchematicCatalogEntry(
            int swgAideServerId,
            int swgAideSchematicId,
            String name,
            int resourceSlotCount,
            List<String> resourceSlotKinds,
            List<String> experimentGroupTitles,
            String targetTemplateHint) {
        if (swgAideServerId <= 0) {
            throw new IllegalArgumentException("swgAideServerId must be > 0");
        }
        if (swgAideSchematicId <= 0) {
            throw new IllegalArgumentException(
                    "swgAideSchematicId must be > 0");
        }
        this.swgAideServerId = swgAideServerId;
        this.swgAideSchematicId = swgAideSchematicId;
        this.name = name == null ? "" : name;
        this.resourceSlotCount = Math.max(0, resourceSlotCount);
        this.resourceSlotKinds = Collections.unmodifiableList(
                new ArrayList<String>(
                        resourceSlotKinds == null
                                ? Collections.<String>emptyList()
                                : resourceSlotKinds));
        this.experimentGroupTitles = Collections.unmodifiableList(
                new ArrayList<String>(
                        experimentGroupTitles == null
                                ? Collections.<String>emptyList()
                                : experimentGroupTitles));
        this.targetTemplateHint = targetTemplateHint == null
                ? "" : targetTemplateHint.trim();
    }

    public int getSwgAideServerId() { return swgAideServerId; }
    public int getSwgAideSchematicId() { return swgAideSchematicId; }
    public String getName() { return name; }
    public int getResourceSlotCount() { return resourceSlotCount; }
    public List<String> getResourceSlotKinds() { return resourceSlotKinds; }
    public List<String> getExperimentGroupTitles() { return experimentGroupTitles; }
    public String getTargetTemplateHint() { return targetTemplateHint; }

    /**
     * Convenience constructor for the common case where the catalog
     * entry has no experiment-group data and no target-template hint.
     */
    public static SchematicCatalogEntry basic(
            int swgAideServerId,
            int swgAideSchematicId,
            String name,
            int resourceSlotCount,
            List<String> resourceSlotKinds) {
        return new SchematicCatalogEntry(
                swgAideServerId, swgAideSchematicId, name,
                resourceSlotCount, resourceSlotKinds,
                Collections.<String>emptyList(), null);
    }
}
