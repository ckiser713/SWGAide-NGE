package swg.infinity.component;
import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.components.ComponentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.scenario.AttributeState;
import swg.infinity.engine.CraftState;

/**
 * Converts a completed component craft into the exact component DTO consumed by
 * parent craft scenarios.
 */
public final class CraftedComponentFactory {

    public ComponentInstance create(
            String id,
            String templateId,
            String serial,
            CraftState completedComponentCraft) {
        if (completedComponentCraft == null) {
            throw new NullPointerException("completedComponentCraft");
        }

        int uses = 1;
        List<ComponentProperty> properties = new ArrayList<ComponentProperty>();

        for (Map.Entry<String, AttributeState> entry :
                completedComponentCraft.getAttributes().entrySet()) {
            String name = entry.getKey();
            AttributeState attribute = entry.getValue();

            if ("useCount".equals(name)) {
                int sourceUses = (int) attribute.getCurrentValue();
                if (sourceUses > 1) uses = sourceUses;
                continue;
            }

            properties.add(new ComponentProperty(
                    name,
                    attribute.getCurrentValue(),
                    attribute.getProperty().getPrecision(),
                    attribute.getProperty().getGroup(),
                    attribute.getProperty().isHidden(),
                    attribute.getProperty().getProvenance()));
        }

        return new ComponentInstance(
                id,
                templateId,
                serial,
                uses,
                ComponentOrigin.CRAFTED,
                properties);
    }
}
