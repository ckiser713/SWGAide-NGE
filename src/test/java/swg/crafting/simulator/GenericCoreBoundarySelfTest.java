package swg.crafting.simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Asserts the generic simulator core does not import Infinity implementation
 * classes that contain behaviour (engines, processors, combiners, registries,
 * providers, bindings). Pure-DTO imports from {@code swg.infinity.*} remain
 * permitted because Infinity-specific DTOs (ResourceInput, ResourceSlotAssignment,
 * CraftState, FunctionalItemResult, ComponentSlotAssignment, ComponentOrigin)
 * are still housed in the Infinity module per GENERICIZATION_MIGRATION.md.
 *
 * <p>The bridge layer ({@code swg.crafting.simulator.server.infinity.*}) is
 * allowed to import anything from {@code swg.infinity.*}; that is its purpose.</p>
 *
 * <p>Future work moves the Infinity-specific DTOs to
 * {@code swg.crafting.simulator.contracts.*}; until then this test enforces
 * the architecturally meaningful part of the boundary: the generic core
 * does not depend on Infinity behaviour.</p>
 */
public final class GenericCoreBoundarySelfTest {
    private GenericCoreBoundarySelfTest() {
        throw new AssertionError("Do not instantiate");
    }

    /** Behaviour-bearing Infinity classes that must not leak into the generic core. */
    private static final List<String> FORBIDDEN_INFINITY_CLASSES = Arrays.asList(
            "swg.infinity.engine.InfinityCraftEngine",
            "swg.infinity.engine.InfinityCraftService",
            // ResourceLaboratory is intentionally accessible to the explainer
            // service: CraftExplainer is an analysis tool that needs the lab's
            // weighted-value math. Future work extracts a generic
            // ResourceLaboratoryMath helper.
            // "swg.infinity.engine.ResourceLaboratory",
            "swg.infinity.engine.UnsupportedInfinityRuleException",
            "swg.infinity.component.ComponentCombiner",
            "swg.infinity.component.CraftedComponentFactory",
            "swg.infinity.processor.ProcessorRegistry",
            "swg.infinity.processor.ResultProcessor",
            "swg.infinity.contracts.InfinityRuleset",
            "swg.infinity.integration.InfinityGalaxyBinding",
            "swg.infinity.integration.SchematicBindingRegistry",
            "swg.infinity.integration.SchematicBindingBuilder",
            "swg.infinity.integration.BindingWriter",
            "swg.infinity.integration.BindingOverride",
            "swg.infinity.extract.Extractor",
            "swg.infinity.extract.ExtractionReport",
            "swg.infinity.extract.InheritanceResolver",
            "swg.infinity.extract.LuaTemplateStubLoader",
            "swg.infinity.extract.CppHeaderReader",
            "swg.infinity.loot.",
            "swg.infinity.fixture.",
            "swg.infinity.analysis.ResourceCandidateAnalyzer",
            "swg.infinity.analysis.ResourceCandidateEvaluation",
            "swg.crafting.simulator.server.infinity.");

    public static void main(String[] args) throws Exception {
        File root = new File("src/main/java/swg/crafting/simulator");
        if (!root.isDirectory()) {
            throw new AssertionError(
                    "Cannot locate generic core at " + root.getAbsolutePath());
        }

        List<String> offenders = new ArrayList<String>();
        walk(root, "", offenders);

        if (!offenders.isEmpty()) {
            StringBuilder msg = new StringBuilder(
                    "Generic core imports Infinity behaviour classes:");
            for (String offender : offenders) {
                msg.append("\n  ").append(offender);
            }
            throw new AssertionError(msg.toString());
        }

        System.out.println("GenericCoreBoundarySelfTest PASS");
    }

    private static void walk(
            File root, String relPath, List<String> offenders) throws Exception {
        File[] entries = root.listFiles();
        if (entries == null) return;
        for (File entry : entries) {
            String child = relPath + entry.getName();
            if (entry.isDirectory()) {
                walk(entry, child + "/", offenders);
                continue;
            }
            if (!entry.getName().endsWith(".java")) continue;
            if (child.startsWith("server/infinity/")) continue;
            String text = readAll(entry);
            String[] lines = text.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("import swg.")) continue;
                if (trimmed.startsWith("import swg.crafting.simulator.server.infinity.")) {
                    offenders.add(child + "  ->  " + trimmed);
                    continue;
                }
                for (String forbidden : FORBIDDEN_INFINITY_CLASSES) {
                    if (trimmed.startsWith("import " + forbidden)) {
                        offenders.add(child + "  ->  " + trimmed);
                    }
                }
            }
        }
    }

    private static String readAll(File f) throws Exception {
        java.io.FileInputStream in = new java.io.FileInputStream(f);
        try {
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) >= 0) {
                out.write(buf, 0, r);
            }
            return new String(out.toByteArray(), "UTF-8");
        } finally {
            in.close();
        }
    }
}
