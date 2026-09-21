package swg.crafting.simulator.integration;

import swg.crafting.simulator.ServerSimulationContext;
import swg.crafting.simulator.resources.LoadedServerResources;
import swg.crafting.simulator.rules.ServerIdentity;
import swg.crafting.simulator.rules.ServerRulesProvider;
import swg.crafting.simulator.rules.ServerRulesRegistry;
import swg.model.SWGCGalaxy;

/**
 * Builds the generic Crafting Simulator context from SWGAide's selected galaxy.
 *
 * <p>This is an application/integration boundary. Swing panels should consume
 * the returned immutable context rather than independently resolving resources
 * and server rules.</p>
 */
public final class SwgAideCraftingSimulatorContextFactory {
    private final SwgAideServerIdentityAdapter identityAdapter;
    private final SwgAideServerResourceCatalog resourceCatalog;
    private final ServerRulesRegistry rulesRegistry;

    public SwgAideCraftingSimulatorContextFactory(
            ServerRulesRegistry rulesRegistry) {
        this(new SwgAideServerIdentityAdapter(),
                new SwgAideServerResourceCatalog(),
                rulesRegistry);
    }

    SwgAideCraftingSimulatorContextFactory(
            SwgAideServerIdentityAdapter identityAdapter,
            SwgAideServerResourceCatalog resourceCatalog,
            ServerRulesRegistry rulesRegistry) {
        if (identityAdapter == null) throw new NullPointerException("identityAdapter");
        if (resourceCatalog == null) throw new NullPointerException("resourceCatalog");
        if (rulesRegistry == null) throw new NullPointerException("rulesRegistry");
        this.identityAdapter = identityAdapter;
        this.resourceCatalog = resourceCatalog;
        this.rulesRegistry = rulesRegistry;
    }

    public ServerSimulationContext create(SWGCGalaxy galaxy) {
        if (galaxy == null) throw new NullPointerException("galaxy");

        ServerIdentity server = identityAdapter.snapshot(galaxy);
        LoadedServerResources resources = resourceCatalog.snapshot(galaxy);
        ServerRulesProvider provider = rulesRegistry.resolve(server);

        return provider == null
                ? ServerSimulationContext.resourceOnly(server, resources)
                : ServerSimulationContext.verified(server, resources, provider);
    }
}
