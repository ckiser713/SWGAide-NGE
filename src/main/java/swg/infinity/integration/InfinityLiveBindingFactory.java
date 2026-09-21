package swg.infinity.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.schematics.SWGSchematic;
import swg.crafting.schematics.SWGSchematicsManager;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SchematicDefinition;
import swg.model.SWGCGalaxy;

/**
 * Creates runtime bindings using the real SWGAide schematic IDs for the
 * selected server.
 *
 * <p>A binding is VERIFIED only when name + raw-resource slot
 * class/quantity signature + component kind/quantity signature identify one
 * unique Infinity schematic. Partial or multiple matches fail closed.</p>
 */
public final class InfinityLiveBindingFactory {

    public SchematicBindingRegistry create(
            SWGCGalaxy galaxy,
            InfinityRuleset ruleset) {
        if (galaxy == null) throw new NullPointerException("galaxy");
        if (ruleset == null) throw new NullPointerException("ruleset");
        if (galaxy.id() != ruleset.getTargetServerId()) {
            throw new IllegalArgumentException(
                    "selected server " + galaxy.id()
                    + " differs from ruleset target "
                    + ruleset.getTargetServerId());
        }

        List<SchematicBinding> bindings =
                new ArrayList<SchematicBinding>();
        for (SWGSchematic nativeSchematic :
                SWGSchematicsManager.getSchematics(galaxy)) {
            if (nativeSchematic == null) continue;

            LiveBindingSignature nativeSignature =
                    LiveBindingSignature.fromSwgAide(nativeSchematic);
            List<String> candidates = new ArrayList<String>();

            for (SchematicDefinition definition : ruleset.getSchematics()) {
                if (definition == null) continue;
                if (nativeSignature.alignsWith(
                        LiveBindingSignature.fromInfinity(definition))) {
                    candidates.add(definition.getId());
                }
            }

            Collections.sort(candidates);
            if (candidates.size() == 1) {
                bindings.add(new SchematicBinding(
                        galaxy.id(),
                        nativeSchematic.getID(),
                        candidates.get(0),
                        BindingState.VERIFIED,
                        "live name+resource+component structural match"));
            } else if (candidates.size() > 1) {
                bindings.add(new SchematicBinding(
                        galaxy.id(),
                        nativeSchematic.getID(),
                        "",
                        BindingState.AMBIGUOUS,
                        "live structural candidates=" + candidates));
            } else {
                bindings.add(new SchematicBinding(
                        galaxy.id(),
                        nativeSchematic.getID(),
                        "",
                        BindingState.MISSING,
                        "live structural signature matched no server schematic"));
            }
        }

        return new SchematicBindingRegistry(galaxy.id(), bindings);
    }
}
