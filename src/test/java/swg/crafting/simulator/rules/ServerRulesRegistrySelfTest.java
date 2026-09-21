package swg.crafting.simulator.rules;

import java.util.Arrays;
import java.util.Collections;

/** Dependency-free provider resolution tests. */
public final class ServerRulesRegistrySelfTest {
    private ServerRulesRegistrySelfTest() {
    }

    public static void main(String[] args) {
        shouldPreferExactServerOverVerifiedFamily();
        shouldRejectAmbiguousStrongestMatch();
        shouldReturnNullWhenUnsupported();
        System.out.println("ServerRulesRegistrySelfTest PASS");
    }

    private static void shouldPreferExactServerOverVerifiedFamily() {
        ServerIdentity server = new ServerIdentity(154, "Infinity", "precu", true);
        ServerRulesProvider family = provider(
                "family", ServerRulesMatch.VERIFIED_FAMILY, 0);
        ServerRulesProvider exact = provider(
                "exact", ServerRulesMatch.EXACT_SERVER, 154);

        ServerRulesRegistry registry =
                new ServerRulesRegistry(Arrays.asList(family, exact));
        if (registry.resolve(server) != exact) {
            throw new AssertionError("exact server provider did not win");
        }
    }

    private static void shouldRejectAmbiguousStrongestMatch() {
        ServerIdentity server = new ServerIdentity(154, "Infinity", "precu", true);
        ServerRulesRegistry registry = new ServerRulesRegistry(Arrays.asList(
                provider("one", ServerRulesMatch.EXACT_SERVER, 154),
                provider("two", ServerRulesMatch.EXACT_SERVER, 154)));
        boolean rejected = false;
        try {
            registry.resolve(server);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("ambiguous providers accepted");
    }

    private static void shouldReturnNullWhenUnsupported() {
        ServerRulesRegistry registry = new ServerRulesRegistry(
                Collections.singletonList(
                        provider("infinity", ServerRulesMatch.EXACT_SERVER, 154)));
        if (registry.resolve(
                new ServerIdentity(999, "Other", "precu", false)) != null) {
            throw new AssertionError("unsupported server resolved");
        }
    }

    private static ServerRulesProvider provider(
            final String id,
            final ServerRulesMatch declared,
            final int exactServerId) {
        return new ServerRulesProvider() {
            @Override
            public String getProviderId() {
                return id;
            }

            @Override
            public ServerRulesMatch match(ServerIdentity server) {
                if (declared == ServerRulesMatch.EXACT_SERVER
                        && server.getServerId() != exactServerId) {
                    return ServerRulesMatch.NONE;
                }
                if (declared == ServerRulesMatch.VERIFIED_FAMILY
                        && !"precu".equals(server.getFamily())) {
                    return ServerRulesMatch.NONE;
                }
                return declared;
            }

            @Override
            public CraftingRuleset requireRuleset(ServerIdentity server) {
                return new CraftingRuleset() {
                    public String getRulesRepository() { return "fixture"; }
                    public String getRulesCommit() {
                        return "0000000000000000000000000000000000000000";
                    }
                    public String getRulesetHash() { return "fixture"; }
                    public int getTargetServerId() { return server.getServerId(); }
                };
            }
        };
    }
}
