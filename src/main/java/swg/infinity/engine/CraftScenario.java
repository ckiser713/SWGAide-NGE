package swg.infinity.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.infinity.component.ComponentSlotAssignment;
import swg.infinity.contracts.SchematicDefinition;

/** Immutable deterministic craft scenario supplied to InfinityCraftEngine. */
public final class CraftScenario {
    private final SchematicDefinition schematic;
    private final List<ResourceSlotAssignment> resources;
    private final List<ComponentSlotAssignment> components;
    private final CraftOutcomeTier assemblyOutcome;
    private final List<ExperimentStep> experiments;

    public CraftScenario(
            SchematicDefinition schematic,
            List<ResourceSlotAssignment> resources,
            List<ComponentSlotAssignment> components,
            CraftOutcomeTier assemblyOutcome,
            List<ExperimentStep> experiments) {
        if (schematic == null) throw new NullPointerException("schematic");
        if (resources == null) throw new NullPointerException("resources");
        if (components == null) throw new NullPointerException("components");
        if (assemblyOutcome == null) throw new NullPointerException("assemblyOutcome");
        if (experiments == null) throw new NullPointerException("experiments");
        this.schematic = schematic;
        this.resources = Collections.unmodifiableList(
                new ArrayList<ResourceSlotAssignment>(resources));
        this.components = Collections.unmodifiableList(
                new ArrayList<ComponentSlotAssignment>(components));
        this.assemblyOutcome = assemblyOutcome;
        this.experiments = Collections.unmodifiableList(
                new ArrayList<ExperimentStep>(experiments));
    }

    public SchematicDefinition getSchematic() { return schematic; }
    public List<ResourceSlotAssignment> getResources() { return resources; }
    public List<ComponentSlotAssignment> getComponents() { return components; }
    public CraftOutcomeTier getAssemblyOutcome() { return assemblyOutcome; }
    public List<ExperimentStep> getExperiments() { return experiments; }
}
