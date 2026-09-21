package swg.crafting.simulator.rules;

/**
 * Server-specific crafting rules module loaded by the generic simulator.
 */
public interface ServerRulesProvider {
    String getProviderId();

    /**
     * Returns the evidence-backed match level for a server.
     *
     * <p>Implementations must return NONE rather than infer behavior from a
     * similar server name, family, or schematic set.</p>
     */
    ServerRulesMatch match(ServerIdentity server);

    /**
     * Returns the immutable ruleset for a supported server.
     */
    CraftingRuleset requireRuleset(ServerIdentity server);
}
