package swg.infinity;

import swg.crafting.simulator.GenericCoreBoundarySelfTest;
import swg.crafting.simulator.resources.ResourceSnapshotSelfTest;
import swg.crafting.simulator.resources.ResourceCandidateMatcherSelfTest;
import swg.crafting.simulator.ServerSimulationContextSelfTest;
import swg.crafting.simulator.components.RecursiveCraftCycleSelfTest;
import swg.crafting.simulator.rules.ServerRulesRegistrySelfTest;
import swg.infinity.analysis.CraftComparatorSelfTest;
import swg.infinity.analysis.CraftExplainerSelfTest;
import swg.infinity.component.ComponentCombinerSelfTest;
import swg.infinity.component.CraftedComponentFactorySelfTest;
import swg.infinity.crafter.CraftingSkillMathSelfTest;
import swg.infinity.engine.InfinityCraftEngineSelfTest;
import swg.infinity.engine.InfinityCraftServiceSelfTest;
import swg.infinity.engine.ResourceLaboratorySelfTest;
import swg.infinity.extract.ExtractionReportSelfTest;
import swg.infinity.extract.ExtractorSelfTest;
import swg.infinity.extract.InfinityWeaponRulesetComposerSelfTest;
import swg.infinity.extract.WeaponFunctionalParitySelfTest;
import swg.infinity.fixtures.SeedFixtureParseSelfTest;
import swg.infinity.fixtures.WeaponVerticalParitySelfTest;
import swg.infinity.integration.BindingClassificationSelfTest;
import swg.infinity.integration.LiveBindingSignatureSelfTest;
import swg.infinity.integration.BindingsSeedWriterSelfTest;
import swg.infinity.integration.SchematicBindingRegistrySelfTest;
import swg.infinity.planning.MaterialPlannerSelfTest;
import swg.infinity.rules.RulesetCatalogSelfTest;
import swg.infinity.rules.RulesetIntegritySelfTest;
import swg.gui.schematics.craftsim.SWGCraftingSimulatorTabHeadlessSmoke;
import swg.gui.schematics.craftsim.SimEngineFacadeRuntimeSelfTest;
import swg.crafting.simulator.persistence.CraftsimPersistenceSelfTest;

/**
 * Dependency-free foundation suite for repositories that have not yet admitted
 * a unit-test framework.
 */
public final class FoundationSelfTestSuite {
    private FoundationSelfTestSuite() {
    }

    public static void main(String[] args) throws Exception {
        GenericCoreBoundarySelfTest.main(args);
        ResourceSnapshotSelfTest.main(args);
        ResourceCandidateMatcherSelfTest.main(args);
        RecursiveCraftCycleSelfTest.main(args);
        ServerRulesRegistrySelfTest.main(args);
        ServerSimulationContextSelfTest.main(args);
        ResourceLaboratorySelfTest.main(args);
        ComponentCombinerSelfTest.main(args);
        CraftedComponentFactorySelfTest.main(args);
        SchematicBindingRegistrySelfTest.main(args);
        BindingClassificationSelfTest.main(args);
        LiveBindingSignatureSelfTest.main(args);
        BindingsSeedWriterSelfTest.main(args);
        InfinityCraftEngineSelfTest.main(args);
        InfinityCraftServiceSelfTest.main(args);
        CraftComparatorSelfTest.main(args);
        CraftExplainerSelfTest.main(args);
        MaterialPlannerSelfTest.main(args);
        RulesetIntegritySelfTest.main(args);
        RulesetCatalogSelfTest.main(args);
        CraftingSkillMathSelfTest.main(args);
        ExtractionReportSelfTest.main(args);
        ExtractorSelfTest.main(args);
        InfinityWeaponRulesetComposerSelfTest.main(args);
        SeedFixtureParseSelfTest.main(args);
        WeaponVerticalParitySelfTest.main(args);
        WeaponFunctionalParitySelfTest.main(args);
        SWGCraftingSimulatorTabHeadlessSmoke.main(args);
        SimEngineFacadeRuntimeSelfTest.main(args);
        CraftsimPersistenceSelfTest.main(args);
        System.out.println("FoundationSelfTestSuite PASS");
    }
}
