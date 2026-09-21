package swg.crafting.simulator.rules;

/**
 * Minimal provider-neutral identity exposed by any server crafting ruleset.
 *
 * <p>Concrete server modules keep their richer contracts internally. The
 * generic simulator uses this interface for version/provenance binding and
 * provider resolution without depending on a particular server implementation.</p>
 */
public interface CraftingRuleset {
    String getRulesRepository();
    String getRulesCommit();
    String getRulesetHash();
    int getTargetServerId();
}
