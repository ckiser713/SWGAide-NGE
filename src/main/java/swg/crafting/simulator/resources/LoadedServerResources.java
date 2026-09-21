package swg.crafting.simulator.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.crafting.resources.SWGKnownResource;
import swg.gui.resources.SWGInventoryWrapper;

/**
 * Read-only snapshot of resource data already available to SWGAide for one
 * selected server.
 *
 * <p>LOADED_RECENT is SWGAide's current + retained recently depleted set.
 * INVENTORY is kept as wrappers so quantities/assignees are not lost.</p>
 */
public final class LoadedServerResources {
    private final int serverId;
    private final List<SWGKnownResource> current;
    private final List<SWGKnownResource> loadedRecent;
    private final List<SWGInventoryWrapper> inventory;

    public LoadedServerResources(
            int serverId,
            List<SWGKnownResource> current,
            List<SWGKnownResource> loadedRecent,
            List<SWGInventoryWrapper> inventory) {
        if (serverId <= 0) throw new IllegalArgumentException("serverId must be > 0");
        if (current == null) throw new NullPointerException("current");
        if (loadedRecent == null) throw new NullPointerException("loadedRecent");
        if (inventory == null) throw new NullPointerException("inventory");
        this.serverId = serverId;
        this.current = Collections.unmodifiableList(
                new ArrayList<SWGKnownResource>(current));
        this.loadedRecent = Collections.unmodifiableList(
                new ArrayList<SWGKnownResource>(loadedRecent));
        this.inventory = Collections.unmodifiableList(
                new ArrayList<SWGInventoryWrapper>(inventory));
    }

    public int getServerId() { return serverId; }
    public List<SWGKnownResource> getCurrent() { return current; }
    public List<SWGKnownResource> getLoadedRecent() { return loadedRecent; }
    public List<SWGInventoryWrapper> getInventory() { return inventory; }
}
