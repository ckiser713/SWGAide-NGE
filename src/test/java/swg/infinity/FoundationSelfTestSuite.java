package swg.infinity;

import swg.infinity.analysis.CraftComparatorSelfTest;
import swg.infinity.analysis.CraftExplainerSelfTest;
import swg.infinity.component.ComponentCombinerSelfTest;
import swg.infinity.component.CraftedComponentFactorySelfTest;
import swg.infinity.engine.InfinityCraftEngineSelfTest;
import swg.infinity.engine.InfinityCraftServiceSelfTest;
import swg.infinity.engine.ResourceLaboratorySelfTest;
import swg.infinity.integration.SchematicBindingRegistrySelfTest;
import swg.infinity.planning.MaterialPlannerSelfTest;

/**
 * Dependency-free foundation suite for repositories that have not yet admitted
 * a unit-test framework.
 */
public final class FoundationSelfTestSuite {
    private FoundationSelfTestSuite() {
    }

    public static void main(String[] args) {
        ResourceLaboratorySelfTest.main(args);
        ComponentCombinerSelfTest.main(args);
        CraftedComponentFactorySelfTest.main(args);
        SchematicBindingRegistrySelfTest.main(args);
        InfinityCraftEngineSelfTest.main(args);
        InfinityCraftServiceSelfTest.main(args);
        CraftComparatorSelfTest.main(args);
        CraftExplainerSelfTest.main(args);
        MaterialPlannerSelfTest.main(args);
        System.out.println("FoundationSelfTestSuite PASS");
    }
}
