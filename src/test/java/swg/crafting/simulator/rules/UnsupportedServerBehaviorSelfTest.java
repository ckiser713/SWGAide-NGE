package swg.crafting.simulator.rules;

import java.util.Arrays;
import java.util.Collections;

import swg.crafting.simulator.ServerSimulationContext;
import swg.crafting.simulator.ServerSimulationMode;
import swg.crafting.simulator.resources.LoadedServerResources;
import swg.infinity.integration.InfinityGalaxyBinding;
import swg.infinity.runtime.InfinityRuntimeBootstrap;

/**
 * Verifies the operator invariants for unsupported (NONE-mode) servers.
 * The Crafting Simulator tab must remain present on every server and the
 * resource browser must keep working, but {@code NONE}-mode servers must
 * never apply Infinity rules based on a generic "precu" family label
 * alone.
 *
 * <p>The test asserts:</p>
 * <ol>
 *   <li>{@link ServerRulesRegistry#resolve} returns {@code null} when
 *       no provider claims the server (NONE-only);</li>
 *   <li>{@code InfinityServerRulesProvider.match} never returns a
 *       non-NONE match for any server other than
 *       {@link InfinityGalaxyBinding#SWGAIDE_INFINITY_SERVER_ID};</li>
 *   <li>{@link ServerRulesRegistry#requireRuleset} fails closed with
 *       {@link IllegalStateException} when no verified provider
 *       exists;</li>
 *   <li>two providers both claiming the same strongest non-NONE match
 *       throws — ambiguity is fail-closed;</li>
 *   <li>{@link ServerSimulationContext#resourceOnly} preserves
 *       resource browsing without a rules provider;</li>
 *   <li>the {@link ServerSimulationContext#hasVerifiedRules} accessor
 *       reports the verified rules state correctly.</li>
 * </ol>
 */
public final class UnsupportedServerBehaviorSelfTest {

    private UnsupportedServerBehaviorSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        noneOnlyRegistryReturnsNull();
        infinityProviderRejectsNon154Servers();
        requireRulesetFailsClosedWhenNoProvider();
        requireRulesetFailsClosedWhenOnlyNoneProvider();
        ambiguityRejectedAtStrongestLevel();
        infinityProviderRejectsByFamilyOnly();
        resourceOnlyContextKeepsResources();
        hasVerifiedRulesAccessorMatchesMode();
        System.out.println(
                "UnsupportedServerBehaviorSelfTest PASS (8 invariants)");
    }

    // -----------------------------------------------------------------
    // (1) registry: NONE-only → null
    // -----------------------------------------------------------------
    private static void noneOnlyRegistryReturnsNull() {
        ServerRulesProvider noneOnly = fixture(
                "none-only",
                (ServerIdentity server) -> ServerRulesMatch.NONE);
        ServerRulesRegistry registry = new ServerRulesRegistry(
                Collections.singletonList(noneOnly));
        if (registry.resolve(
                new ServerIdentity(154, "Infinity", "precu", true))
                != null) {
            throw new AssertionError(
                    "NONE-only registry should resolve to null for Infinity");
        }
        if (registry.resolve(
                new ServerIdentity(999, "Other", "precu", false))
                != null) {
            throw new AssertionError(
                    "NONE-only registry should resolve to null for any "
                    + "server when no provider claims it");
        }
    }

    // -----------------------------------------------------------------
    // (2) InfinityServerRulesProvider never claims non-154
    // -----------------------------------------------------------------
    private static void infinityProviderRejectsNon154Servers()
            throws Exception {
        swg.infinity.contracts.InfinityRuleset ruleset =
                InfinityRuntimeBootstrap.loadBundled();
        if (ruleset == null) {
            throw new AssertionError(
                    "Bundled Infinity ruleset not packaged");
        }
        ServerRulesProvider provider = new swg.crafting.simulator.server
                .infinity.InfinityServerRulesProvider(ruleset);

        // Different server id → NONE, regardless of family.
        for (int id = 1; id <= 200; ++id) {
            if (id == InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID) {
                continue;
            }
            ServerIdentity s = new ServerIdentity(id, "x", "precu", false);
            if (provider.match(s) != ServerRulesMatch.NONE) {
                throw new AssertionError(
                        "Infinity provider matched non-154 server: " + s);
            }
        }

        // Same family, name "Infinity", but different server id → still NONE.
        if (provider.match(new ServerIdentity(
                155, "Infinity Mirror", "precu", true))
                != ServerRulesMatch.NONE) {
            throw new AssertionError(
                    "Infinity provider was fooled by name/family alone");
        }

        // Exact server → EXACT_SERVER
        ServerIdentity infinity = new ServerIdentity(
                InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID,
                "Infinity", "precu", true);
        if (provider.match(infinity) != ServerRulesMatch.EXACT_SERVER) {
            throw new AssertionError(
                    "Infinity provider did not match its canonical server");
        }
    }

    // -----------------------------------------------------------------
    // (3) requireRuleset fails closed
    // -----------------------------------------------------------------
    private static void requireRulesetFailsClosedWhenNoProvider() {
        ServerRulesProvider noneOnly = fixture(
                "none-only",
                (ServerIdentity server) -> ServerRulesMatch.NONE);
        ServerRulesRegistry registry = new ServerRulesRegistry(
                Collections.singletonList(noneOnly));
        boolean rejected = false;
        try {
            registry.requireRuleset(
                    new ServerIdentity(154, "Infinity", "precu", true));
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "requireRuleset did not fail closed for NONE-only registry");
        }
    }

    private static void requireRulesetFailsClosedWhenOnlyNoneProvider() {
        ServerRulesRegistry registry = new ServerRulesRegistry(
                Collections.<ServerRulesProvider>emptyList());
        boolean rejected = false;
        try {
            registry.requireRuleset(
                    new ServerIdentity(154, "Infinity", "precu", true));
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "requireRuleset accepted an empty registry");
        }
    }

    // -----------------------------------------------------------------
    // (4) ambiguity rejected at strongest level
    // -----------------------------------------------------------------
    private static void ambiguityRejectedAtStrongestLevel() {
        ServerRulesProvider a = fixture("a",
                (ServerIdentity s) -> ServerRulesMatch.EXACT_SERVER);
        ServerRulesProvider b = fixture("b",
                (ServerIdentity s) -> ServerRulesMatch.EXACT_SERVER);
        ServerRulesRegistry registry = new ServerRulesRegistry(
                Arrays.asList(a, b));
        boolean rejected = false;
        try {
            registry.resolve(new ServerIdentity(154, "Infinity",
                    "precu", true));
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "Two EXACT_SERVER providers on the same server accepted");
        }
    }

    // -----------------------------------------------------------------
    // (5) Infinity provider must NOT claim by family alone
    //     (operator constraint)
    // -----------------------------------------------------------------
    private static void infinityProviderRejectsByFamilyOnly()
            throws Exception {
        swg.infinity.contracts.InfinityRuleset ruleset =
                InfinityRuntimeBootstrap.loadBundled();
        ServerRulesProvider provider = new swg.crafting.simulator.server
                .infinity.InfinityServerRulesProvider(ruleset);
        ServerIdentity[] falsePositives = new ServerIdentity[] {
            new ServerIdentity(99, "Core3 Sandbox", "precu", true),
            new ServerIdentity(42, "PRE-CU Test", "precu", false),
            new ServerIdentity(7, "NGE", "nge", true),
            new ServerIdentity(13, "Restored PRE-CU", "precu", true)
        };
        for (ServerIdentity s : falsePositives) {
            if (provider.match(s) != ServerRulesMatch.NONE) {
                throw new AssertionError(
                        "Infinity provider matched a non-Infinity server by "
                        + "family alone: " + s);
            }
        }
    }

    // -----------------------------------------------------------------
    // (6) RESOURCE_ONLY context keeps resources
    // -----------------------------------------------------------------
    private static void resourceOnlyContextKeepsResources() {
        ServerIdentity server =
                new ServerIdentity(154, "Infinity", "precu", true);
        LoadedServerResources resources = new LoadedServerResources(
                154,
                Collections.<swg.crafting.resources.SWGKnownResource>emptyList(),
                Collections.<swg.crafting.resources.SWGKnownResource>emptyList(),
                Collections.<swg.gui.resources.SWGInventoryWrapper>emptyList());
        ServerSimulationContext context = ServerSimulationContext
                .resourceOnly(server, resources);
        if (context.getMode() != ServerSimulationMode.RESOURCE_ONLY) {
            throw new AssertionError(
                    "Expected RESOURCE_ONLY mode, got " + context.getMode());
        }
        if (context.hasVerifiedRules()) {
            throw new AssertionError(
                    "RESOURCE_ONLY context claims a verified rules provider");
        }
        if (context.getResources() != resources) {
            throw new AssertionError(
                    "RESOURCE_ONLY context lost its resource snapshot");
        }
        boolean rejected = false;
        try {
            context.requireRulesProvider();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "RESOURCE_ONLY context handed out a rules provider");
        }
    }

    private static void hasVerifiedRulesAccessorMatchesMode() {
        ServerIdentity server =
                new ServerIdentity(154, "Infinity", "precu", true);
        LoadedServerResources resources = new LoadedServerResources(
                154,
                Collections.<swg.crafting.resources.SWGKnownResource>emptyList(),
                Collections.<swg.crafting.resources.SWGKnownResource>emptyList(),
                Collections.<swg.gui.resources.SWGInventoryWrapper>emptyList());

        ServerSimulationContext roContext = ServerSimulationContext
                .resourceOnly(server, resources);
        if (roContext.hasVerifiedRules()) {
            throw new AssertionError(
                    "RESOURCE_ONLY context reports hasVerifiedRules()=true");
        }

        ServerRulesProvider provider = fixture("v",
                (ServerIdentity s) -> ServerRulesMatch.EXACT_SERVER);
        ServerSimulationContext vContext = ServerSimulationContext
                .verified(server, resources, provider);
        if (!vContext.hasVerifiedRules()) {
            throw new AssertionError(
                    "VERIFIED_RULES context reports hasVerifiedRules()=false");
        }
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------
    private interface MatchFn {
        ServerRulesMatch match(ServerIdentity server);
    }

    private static ServerRulesProvider fixture(
            final String id, final MatchFn fn) {
        return new ServerRulesProvider() {
            @Override
            public String getProviderId() {
                return id;
            }

            @Override
            public ServerRulesMatch match(ServerIdentity server) {
                return fn.match(server);
            }

            @Override
            public CraftingRuleset requireRuleset(
                    final ServerIdentity selected) {
                return new CraftingRuleset() {
                    public String getRulesRepository() { return "fixture"; }
                    public String getRulesCommit() {
                        return "0000000000000000000000000000000000000000";
                    }
                    public String getRulesetHash() { return "fixture"; }
                    public int getTargetServerId() {
                        return selected.getServerId();
                    }
                };
            }
        };
    }
}
