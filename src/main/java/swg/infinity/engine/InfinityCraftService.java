package swg.infinity.engine;
import swg.crafting.simulator.scenario.CraftScenario;
import swg.crafting.simulator.scenario.CraftResult;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.RulesetValidator;
import swg.infinity.contracts.SchematicDefinition;

/**
 * Fail-closed production entry point above the arithmetic engine.
 *
 * <p>Raw InfinityCraftEngine remains useful for golden tests. Application code
 * should prefer this service so incomplete coverage cannot be presented as an
 * exact Infinity result.</p>
 */
public final class InfinityCraftService {
    private final InfinityCraftEngine engine;

    public InfinityCraftService() {
        this(new InfinityCraftEngine());
    }

    public InfinityCraftService(InfinityCraftEngine engine) {
        if (engine == null) throw new NullPointerException("engine");
        this.engine = engine;
    }

    public CraftResult executeExact(
            InfinityRuleset ruleset,
            CraftScenario scenario) {
        if (ruleset == null) throw new NullPointerException("ruleset");
        if (scenario == null) throw new NullPointerException("scenario");

        RulesetValidator.validate(ruleset);
        SchematicDefinition canonical =
                ruleset.getSchematic(scenario.getSchematic().getId());
        if (canonical == null) {
            throw new IllegalStateException(
                    "Scenario schematic is not present in active ruleset: "
                    + scenario.getSchematic().getId());
        }
        if (!canonical.getProvenance().getCommit().equals(
                ruleset.getManifest().getCommit())) {
            throw new IllegalStateException(
                    "Schematic provenance commit differs from ruleset manifest");
        }
        if (!scenario.getSchematic().getProvenance().getCommit().equals(
                canonical.getProvenance().getCommit())) {
            throw new IllegalStateException(
                    "Scenario schematic provenance differs from active ruleset");
        }

        CoverageRecord coverage =
                ruleset.getCoverage(canonical.getId());
        if (coverage == null) {
            throw new IllegalStateException(
                    "Missing coverage for " + canonical.getId());
        }
        if (coverage.overall() != EvidenceState.EXACT) {
            throw new UnsupportedInfinityRuleException(
                    "Exact execution rejected; coverage is "
                    + coverage.overall() + " for " + canonical.getId());
        }

        CraftResult result = engine.execute(scenario);
        if (result.getCraftState().getEvidenceState()
                != EvidenceState.EXACT_WITH_FORCED_OUTCOME) {
            throw new IllegalStateException(
                    "Deterministic engine returned unexpected evidence state: "
                    + result.getCraftState().getEvidenceState());
        }
        return result;
    }
}
