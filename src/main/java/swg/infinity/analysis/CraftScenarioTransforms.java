package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.List;

import swg.infinity.engine.CraftScenario;
import swg.infinity.engine.ResourceInput;
import swg.infinity.engine.ResourceSlotAssignment;

/** Immutable scenario transformations used by comparison/sensitivity analysis. */
public final class CraftScenarioTransforms {
    private CraftScenarioTransforms() {
        throw new AssertionError("Do not instantiate");
    }

    public static CraftScenario withResource(
            CraftScenario source,
            int slotIndex,
            ResourceInput replacement) {
        if (source == null) throw new NullPointerException("source");
        if (replacement == null) throw new NullPointerException("replacement");
        if (slotIndex < 0) throw new IllegalArgumentException("slotIndex must be >= 0");

        List<ResourceSlotAssignment> assignments =
                new ArrayList<ResourceSlotAssignment>();
        boolean replaced = false;
        for (ResourceSlotAssignment assignment : source.getResources()) {
            if (assignment.getSlotIndex() == slotIndex) {
                if (replaced) {
                    throw new IllegalStateException(
                            "Source scenario has duplicate resource slot " + slotIndex);
                }
                assignments.add(new ResourceSlotAssignment(slotIndex, replacement));
                replaced = true;
            } else {
                assignments.add(assignment);
            }
        }
        if (!replaced) {
            assignments.add(new ResourceSlotAssignment(slotIndex, replacement));
        }

        return new CraftScenario(
                source.getSchematic(),
                assignments,
                source.getComponents(),
                source.getAssemblyOutcome(),
                source.getExperiments());
    }
}
