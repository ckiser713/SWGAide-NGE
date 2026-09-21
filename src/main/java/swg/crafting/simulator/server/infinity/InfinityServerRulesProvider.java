package swg.crafting.simulator.server.infinity;

import swg.crafting.simulator.rules.CraftingRuleset;
import swg.crafting.simulator.rules.ServerIdentity;
import swg.crafting.simulator.rules.ServerRulesMatch;
import swg.crafting.simulator.rules.ServerRulesProvider;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.integration.InfinityGalaxyBinding;

/**
 * Exact-server rules provider for SWG Infinity.
 *
 * <p>This provider never claims generic PRE-CU family support. A future base
 * Core3/PRE-CU module must be researched, versioned, and registered separately.</p>
 */
public final class InfinityServerRulesProvider implements ServerRulesProvider {
    public static final String PROVIDER_ID = "swg-infinity";

    private final InfinityRuleset ruleset;

    public InfinityServerRulesProvider(InfinityRuleset ruleset) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        if (ruleset.getTargetServerId()
                != InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID) {
            throw new IllegalArgumentException(
                    "Infinity ruleset targets server "
                    + ruleset.getTargetServerId()
                    + ", expected "
                    + InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID);
        }
        this.ruleset = ruleset;
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public ServerRulesMatch match(ServerIdentity server) {
        if (server == null) throw new NullPointerException("server");
        return server.getServerId()
                == InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID
                ? ServerRulesMatch.EXACT_SERVER
                : ServerRulesMatch.NONE;
    }

    @Override
    public CraftingRuleset requireRuleset(ServerIdentity server) {
        if (match(server) != ServerRulesMatch.EXACT_SERVER) {
            throw new IllegalArgumentException(
                    "Infinity provider does not support " + server);
        }
        return ruleset;
    }
}
