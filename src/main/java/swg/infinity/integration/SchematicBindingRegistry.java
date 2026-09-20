package swg.infinity.integration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.schematics.SWGSchematic;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SchematicDefinition;

/**
 * Fail-closed registry for SWGAide-to-Infinity schematic bindings.
 */
public final class SchematicBindingRegistry {
    private final int serverId;
    private final Map<Integer, SchematicBinding> bindings;

    public SchematicBindingRegistry(int serverId, List<SchematicBinding> bindings) {
        if (serverId <= 0) throw new IllegalArgumentException("serverId must be > 0");
        if (bindings == null) throw new NullPointerException("bindings");
        this.serverId = serverId;

        Map<Integer, SchematicBinding> indexed =
                new LinkedHashMap<Integer, SchematicBinding>();
        for (SchematicBinding binding : bindings) {
            if (binding == null) throw new NullPointerException("binding");
            if (binding.getSwgAideServerId() != serverId) {
                throw new IllegalArgumentException(
                        "Binding server mismatch: " + binding.getSwgAideServerId());
            }
            Integer key = Integer.valueOf(binding.getSwgAideSchematicId());
            if (indexed.put(key, binding) != null) {
                throw new IllegalArgumentException(
                        "Duplicate SWGAide schematic binding: " + key);
            }
        }
        this.bindings = Collections.unmodifiableMap(indexed);
    }

    public SchematicBinding get(int swgAideSchematicId) {
        return bindings.get(Integer.valueOf(swgAideSchematicId));
    }

    public SchematicDefinition resolve(
            SWGSchematic schematic,
            InfinityRuleset ruleset) {
        if (schematic == null) throw new NullPointerException("schematic");
        if (ruleset == null) throw new NullPointerException("ruleset");

        SchematicBinding binding = get(schematic.getID());
        if (binding == null || !binding.getState().isRunnable()) {
            throw new IllegalStateException(
                    "Schematic " + schematic.getID()
                    + " has no runnable Infinity binding");
        }

        SchematicDefinition definition =
                ruleset.getSchematic(binding.getInfinitySchematicId());
        if (definition == null) {
            throw new IllegalStateException(
                    "Binding references missing Infinity schematic: "
                    + binding.getInfinitySchematicId());
        }
        return definition;
    }

    public int getServerId() {
        return serverId;
    }
}
