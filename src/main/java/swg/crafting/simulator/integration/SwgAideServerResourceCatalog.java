package swg.crafting.simulator.integration;

import java.util.ArrayList;
import java.util.List;

import swg.crafting.resources.SWGKnownResource;
import swg.crafting.resources.SWGResourceSet;
import swg.crafting.simulator.resources.LoadedServerResources;
import swg.gui.resources.SWGInventoryWrapper;
import swg.gui.resources.SWGResController;
import swg.model.SWGCGalaxy;
import swg.swgcraft.SWGResourceManager;

/**
 * Native SWGAide resource source for the Crafting Simulator.
 *
 * <p>No separate resource database or server-specific feed is introduced.
 * Current and loaded/recent resources come from SWGResourceManager for the
 * selected galaxy; inventory comes from SWGResController for the same galaxy.</p>
 */
public final class SwgAideServerResourceCatalog {

    public LoadedServerResources snapshot(SWGCGalaxy galaxy) {
        if (galaxy == null) throw new NullPointerException("galaxy");

        SWGResourceSet current = SWGResourceManager.getSpawning(galaxy);
        SWGResourceSet loadedRecent = SWGResourceManager.getSet(galaxy);
        List<SWGInventoryWrapper> inventory = SWGResController.inventory(galaxy);

        return new LoadedServerResources(
                galaxy.id(),
                copy(current),
                copy(loadedRecent),
                inventory);
    }

    private List<SWGKnownResource> copy(SWGResourceSet set) {
        List<SWGKnownResource> resources = new ArrayList<SWGKnownResource>();
        for (SWGKnownResource resource : set) {
            resources.add(resource);
        }
        return resources;
    }
}
