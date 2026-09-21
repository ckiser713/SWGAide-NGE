package swg.crafting.simulator;

import swg.crafting.simulator.resources.LoadedServerResources;
import swg.crafting.simulator.rules.ServerIdentity;
import swg.crafting.simulator.rules.ServerRulesProvider;

/**
 * Immutable UI/application context for one selected SWGAide server.
 *
 * <p>The resource catalog is always available. A rules provider is optional:
 * absence means RESOURCE_ONLY mode, never an inferred approximate ruleset.</p>
 */
public final class ServerSimulationContext {
    private final ServerIdentity server;
    private final LoadedServerResources resources;
    private final ServerRulesProvider rulesProvider;
    private final ServerSimulationMode mode;

    private ServerSimulationContext(
            ServerIdentity server,
            LoadedServerResources resources,
            ServerRulesProvider rulesProvider,
            ServerSimulationMode mode) {
        if (server == null) throw new NullPointerException("server");
        if (resources == null) throw new NullPointerException("resources");
        if (mode == null) throw new NullPointerException("mode");
        if (resources.getServerId() != server.getServerId()) {
            throw new IllegalArgumentException(
                    "Resource snapshot server does not match selected server");
        }
        if (mode == ServerSimulationMode.VERIFIED_RULES && rulesProvider == null) {
            throw new IllegalArgumentException(
                    "VERIFIED_RULES mode requires a rules provider");
        }
        if (mode == ServerSimulationMode.RESOURCE_ONLY && rulesProvider != null) {
            throw new IllegalArgumentException(
                    "RESOURCE_ONLY mode must not carry a rules provider");
        }
        this.server = server;
        this.resources = resources;
        this.rulesProvider = rulesProvider;
        this.mode = mode;
    }

    public static ServerSimulationContext verified(
            ServerIdentity server,
            LoadedServerResources resources,
            ServerRulesProvider provider) {
        return new ServerSimulationContext(
                server, resources, provider, ServerSimulationMode.VERIFIED_RULES);
    }

    public static ServerSimulationContext resourceOnly(
            ServerIdentity server,
            LoadedServerResources resources) {
        return new ServerSimulationContext(
                server, resources, null, ServerSimulationMode.RESOURCE_ONLY);
    }

    public ServerIdentity getServer() { return server; }
    public LoadedServerResources getResources() { return resources; }
    public ServerSimulationMode getMode() { return mode; }

    public boolean hasVerifiedRules() {
        return rulesProvider != null;
    }

    public ServerRulesProvider requireRulesProvider() {
        if (rulesProvider == null) {
            throw new IllegalStateException(
                    "No verified crafting rules provider for " + server);
        }
        return rulesProvider;
    }
}
