package swg.infinity;

import swg.infinity.component.ComponentCombinerSelfTest;
import swg.infinity.component.CraftedComponentFactorySelfTest;
import swg.infinity.engine.InfinityCraftEngineSelfTest;
import swg.infinity.engine.InfinityCraftServiceSelfTest;
import swg.infinity.engine.ResourceLaboratorySelfTest;
import swg.infinity.integration.SchematicBindingRegistrySelfTest;

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
        System.out.println("FoundationSelfTestSuite PASS");
    }
}
