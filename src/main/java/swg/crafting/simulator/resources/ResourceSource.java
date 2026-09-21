package swg.crafting.simulator.resources;

/** Origin of a provider-neutral resource snapshot used by the simulator. */
public enum ResourceSource {
    CURRENT,
    LOADED_RECENT,
    INVENTORY,
    MANUAL,
    HISTORICAL_REMOTE,
    HYPOTHETICAL
}
