package swg.crafting.simulator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic provider resolver for the generic crafting simulator.
 *
 * <p>Resolution fails closed when no provider exists or multiple providers
 * claim the same strongest match. Registration order never decides authority.</p>
 */
public final class ServerRulesRegistry {
    private final List<ServerRulesProvider> providers;

    public ServerRulesRegistry(List<ServerRulesProvider> providers) {
        if (providers == null) throw new NullPointerException("providers");
        List<ServerRulesProvider> copied =
                new ArrayList<ServerRulesProvider>(providers.size());
        for (ServerRulesProvider provider : providers) {
            if (provider == null) throw new NullPointerException("provider");
            copied.add(provider);
        }
        this.providers = Collections.unmodifiableList(copied);
    }

    public ServerRulesProvider resolve(ServerIdentity server) {
        if (server == null) throw new NullPointerException("server");

        ServerRulesProvider selected = null;
        ServerRulesMatch selectedMatch = ServerRulesMatch.NONE;

        for (ServerRulesProvider provider : providers) {
            ServerRulesMatch match = provider.match(server);
            if (match == null) {
                throw new IllegalStateException(
                        "Provider returned null match: " + provider.getProviderId());
            }
            if (match.getPriority() > selectedMatch.getPriority()) {
                selected = provider;
                selectedMatch = match;
            } else if (match != ServerRulesMatch.NONE
                    && match.getPriority() == selectedMatch.getPriority()) {
                throw new IllegalStateException(
                        "Ambiguous crafting rules providers for " + server
                        + ": " + selected.getProviderId()
                        + " and " + provider.getProviderId()
                        + " both match as " + match);
            }
        }

        return selected;
    }

    public CraftingRuleset requireRuleset(ServerIdentity server) {
        ServerRulesProvider provider = resolve(server);
        if (provider == null) {
            throw new IllegalStateException(
                    "No verified crafting rules provider for " + server);
        }
        return provider.requireRuleset(server);
    }

    public List<ServerRulesProvider> getProviders() {
        return providers;
    }
}
