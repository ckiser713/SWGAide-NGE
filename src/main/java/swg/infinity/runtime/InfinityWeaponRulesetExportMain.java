package swg.infinity.runtime;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.extract.Extractor;
import swg.infinity.extract.InfinityWeaponRulesetComposer;
import swg.infinity.extract.WeaponTangibleTemplateExtractor;
import swg.infinity.extract.WeaponTangibleTemplateExtractor.WeaponObjectTemplate;

/**
 * Developer/operator tool that generates the source-free runtime weapon
 * ruleset artifact from an exact Infinity checkout.
 *
 * Usage:
 * <pre>
 * java ... InfinityWeaponRulesetExportMain <sourceRoot> <outputJson>
 *      <exactSchematicId> [<exactSchematicId> ...]
 * </pre>
 */
public final class InfinityWeaponRulesetExportMain {
    private InfinityWeaponRulesetExportMain() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "usage: <sourceRoot> <outputJson> <exactId> [exactId...]");
        }

        File sourceRoot = new File(args[0]);
        File output = new File(args[1]);
        if (!sourceRoot.isDirectory()) {
            throw new IllegalArgumentException(
                    "sourceRoot is not directory: " + sourceRoot);
        }

        String commit = InfinityRuntimeBootstrap.PINNED_COMMIT;
        InfinityRuleset draft =
                new Extractor(sourceRoot, commit).extractWeapons();
        Map<String, WeaponObjectTemplate> tangible =
                new WeaponTangibleTemplateExtractor(
                        sourceRoot, commit).extract();

        Set<String> exact = new LinkedHashSet<String>();
        for (int i = 2; i < args.length; ++i) {
            exact.add(args[i]);
        }

        InfinityRuleset effective =
                new InfinityWeaponRulesetComposer().compose(
                        draft, tangible, exact);

        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IllegalStateException(
                    "cannot create output directory: " + parent);
        }

        FileWriter writer = new FileWriter(output);
        try {
            InfinityRulesetJsonCodec.write(effective, writer);
        } finally {
            writer.close();
        }

        System.out.println(
                "InfinityWeaponRulesetExportMain PASS schematics="
                + effective.getSchematics().size()
                + " hash=" + effective.getRulesetHash()
                + " output=" + output.getAbsolutePath());
    }
}
