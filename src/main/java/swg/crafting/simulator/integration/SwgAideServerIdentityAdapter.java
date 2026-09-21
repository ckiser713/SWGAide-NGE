package swg.crafting.simulator.integration;

import swg.crafting.simulator.rules.ServerIdentity;
import swg.model.SWGCGalaxy;

/** Converts SWGAide galaxy metadata into the provider-neutral server identity. */
public final class SwgAideServerIdentityAdapter {
    public ServerIdentity snapshot(SWGCGalaxy galaxy) {
        if (galaxy == null) throw new NullPointerException("galaxy");
        return new ServerIdentity(
                galaxy.id(),
                galaxy.getName(),
                galaxy.getType(),
                galaxy.isCustom());
    }
}
