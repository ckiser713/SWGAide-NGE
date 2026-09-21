package swg.crafting.simulator;

import java.util.Collections;

import swg.crafting.simulator.resources.LoadedServerResources;
import swg.crafting.simulator.rules.CraftingRuleset;
import swg.crafting.simulator.rules.ServerIdentity;
import swg.crafting.simulator.rules.ServerRulesMatch;
import swg.crafting.simulator.rules.ServerRulesProvider;

/** Dependency-free context invariant tests. */
public final class ServerSimulationContextSelfTest {
    private ServerSimulationContextSelfTest() {
    }

    public static void main(String[] args) {
        ServerIdentity server =
                new ServerIdentity(154, "Infinity", "precu", true);
        LoadedServerResources resources =
                new LoadedServerResources(
                        154,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());

        ServerSimulationContext resourceOnly =
                ServerSimulationContext.resourceOnly(server, resources);
        if (resourceOnly.getMode() != ServerSimulationMode.RESOURCE_ONLY
                || resourceOnly.hasVerifiedRules()) {
            throw new AssertionError("resource-only context invalid");
        }

        ServerRulesProvider provider = new ServerRulesProvider() {
            public String getProviderId() { return "fixture"; }
            public ServerRulesMatch match(ServerIdentity ignored) {
                return ServerRulesMatch.EXACT_SERVER;
            }
            public CraftingRuleset requireRuleset(final ServerIdentity selected) {
                return new CraftingRuleset() {
                    public String getRulesRepository() { return "fixture"; }
                    public String getRulesCommit() {
                        return "0000000000000000000000000000000000000000";
                    }
                    public String getRulesetHash() { return "fixture"; }
                    public int getTargetServerId() { return selected.getServerId(); }
                };
            }
        };

        ServerSimulationContext verified =
                ServerSimulationContext.verified(server, resources, provider);
        if (verified.getMode() != ServerSimulationMode.VERIFIED_RULES
                || verified.requireRulesProvider() != provider) {
            throw new AssertionError("verified context invalid");
        }

        boolean rejected = false;
        try {
            ServerSimulationContext.resourceOnly(
                    server,
                    new LoadedServerResources(
                            999,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("cross-server resource context accepted");

        System.out.println("ServerSimulationContextSelfTest PASS");
    }
}
