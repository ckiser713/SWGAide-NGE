package swg.infinity.extract;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import swg.crafting.simulator.contracts.EvidenceState;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.extract.WeaponTangibleTemplateExtractor.WeaponObjectTemplate;

/** Dependency-free effective-ruleset composition and admission tests. */
public final class InfinityWeaponRulesetComposerSelfTest {
    private InfinityWeaponRulesetComposerSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        File source = new File(
                "/home/thenexussidekick/swgaide-deps/swginfinity-public");
        if (!source.isDirectory()) {
            throw new AssertionError(
                    "Infinity source checkout missing at " + source);
        }
        String commit = "6b6ac3726aaa3c293fca83911850d3a02e2adb4f";

        InfinityRuleset draft = new Extractor(source, commit).extractWeapons();
        Map<String, WeaponObjectTemplate> tangible =
                new WeaponTangibleTemplateExtractor(source, commit).extract();

        Set<String> exact = new LinkedHashSet<String>();
        exact.add("pistol_blaster_dl44");
        InfinityRuleset effective =
                new InfinityWeaponRulesetComposer().compose(
                        draft, tangible, exact);

        SchematicDefinition dl44 =
                effective.getSchematic("pistol_blaster_dl44");
        if (dl44 == null || dl44.getProperties().isEmpty()) {
            throw new AssertionError(
                    "effective DL44 must contain tangible experimental properties");
        }
        if (!"weapon".equals(dl44.getProcessorId())) {
            throw new AssertionError("effective DL44 processor must be weapon");
        }
        if (effective.getCoverage("pistol_blaster_dl44").overall()
                != EvidenceState.EXACT) {
            throw new AssertionError("explicitly admitted DL44 not EXACT");
        }

        boolean sawLinear = false;
        for (swg.infinity.contracts.ExperimentalProperty property :
                dl44.getProperties()) {
            if (property.getCombineType() == CombineType.LINEAR) {
                sawLinear = true;
            }
            double sum = 0.0d;
            for (swg.infinity.contracts.PropertyWeight weight :
                    property.getWeights()) {
                sum += weight.getNormalizedWeight();
            }
            if (!property.getWeights().isEmpty()
                    && Math.abs(sum - 1.0d) > 0.000001d) {
                throw new AssertionError(
                        "property weights not normalized: "
                        + property.getAttribute() + " sum=" + sum);
            }
        }
        if (!sawLinear) {
            throw new AssertionError(
                    "source experimentalCombineType=1 must map to LINEAR");
        }

        if (effective.getManifest().getRulesetHash().equals(
                draft.getManifest().getRulesetHash())) {
            throw new AssertionError(
                    "effective ruleset hash must include tangible properties");
        }

        System.out.println("InfinityWeaponRulesetComposerSelfTest PASS");
    }
}
