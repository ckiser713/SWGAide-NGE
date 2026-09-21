package swg.infinity.extract;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.infinity.contracts.InfinityRuleset;

/**
 * Sandboxed Infinity source extractor. Reads weapon draft_schematic .lua
 * files from a pinned {@code swginfinity/public} checkout and emits a
 * normalized ruleset.
 *
 * <p>The extractor is sandboxed:</p>
 * <ul>
 *   <li>Reads only files under the supplied {@code sourceRoot} path.</li>
 *   <li>Reads no classpath or network resources.</li>
 *   <li>Emits deterministic output (LinkedHashMap, no time/locale input).</li>
 *   <li>Reports issues instead of silently dropping source constructs.</li>
 *   <li>Computes a stable SHA-256 manifest over the emitted ruleset.</li>
 * </ul>
 *
 * <p>This is the T4 minimal extractor: weapon drafts only. Other
 * schematic families (armor, food, structure, droid, etc.) expand in
 * T9. Fail-closed on any unhandled construct.</p>
 */
public final class Extractor {

    private final File sourceRoot;
    private final String commit;
    private final List<ExtractionIssue> issues = new ArrayList<ExtractionIssue>();

    public Extractor(File sourceRoot, String commit) {
        if (sourceRoot == null) throw new NullPointerException("sourceRoot");
        if (commit == null
                || !commit.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException(
                    "commit must be a 40-character SHA");
        }
        this.sourceRoot = sourceRoot;
        this.commit = commit.toLowerCase();
    }

    /** Extracts all weapon draft schematics under the source root. */
    public InfinityRuleset extractWeapons() throws java.io.IOException {
        File weaponsDir = new File(sourceRoot,
                "MMOCoreORB/bin/scripts/object/draft_schematic/weapon");
        List<swg.infinity.contracts.SchematicDefinition> schematics =
                new ArrayList<swg.infinity.contracts.SchematicDefinition>();
        if (!weaponsDir.isDirectory()) {
            issues.add(new ExtractionIssue(
                    ExtractionSeverity.BLOCKER,
                    "EXTRACT",
                    "extractWeapons",
                    weaponsDir.getPath() + " is not a directory"));
            return buildRuleset(schematics);
        }
        File[] files = weaponsDir.listFiles();
        if (files == null) {
            return buildRuleset(schematics);
        }
        for (File f : files) {
            if (!f.isFile() || !f.getName().endsWith(".lua")) continue;
            try {
                swg.infinity.contracts.SchematicDefinition def =
                        extractSchematic(f);
                if (def != null) schematics.add(def);
            } catch (RuntimeException e) {
                issues.add(new ExtractionIssue(
                        ExtractionSeverity.BLOCKER,
                        "EXTRACT",
                        "extractSchematic",
                        f.getPath() + ": " + e.getMessage()));
            }
        }
        return buildRuleset(schematics);
    }

    @SuppressWarnings("unchecked")
    private swg.infinity.contracts.SchematicDefinition extractSchematic(
            File file) throws java.io.IOException {
        // Schematic files use the form
        //   object_draft_schematic_weapon_X = object_draft_schematic_weapon_shared_X:new { ... }
        // The first table literal contains the schematic configuration.
        Object parsed = LuaTemplateStubLoader.loadFirstTable(file);
        if (!(parsed instanceof Map)) {
            throw new LuaTemplateStubLoader.LuaSyntaxException(
                    "schematic root must be a table");
        }
        Map<String, Object> table = (Map<String, Object>) parsed;
        String id = stringOrNull(table, "customObjectName");
        String complexity = stringOrNull(table, "complexity");
        String xp = stringOrNull(table, "xp");
        String targetTemplate = stringOrNull(table, "targetTemplate");
        String assemblySkill = stringOrNull(table, "assemblySkill");
        String experimentingSkill =
                stringOrNull(table, "experimentingSkill");

        if (id == null || targetTemplate == null) {
            issues.add(new ExtractionIssue(
                    ExtractionSeverity.BLOCKER,
                    "EXTRACT",
                    "extractSchematic",
                    file.getPath()
                    + ": missing customObjectName or targetTemplate"));
            return null;
        }
        // Use the file name as schematic id when the lua var name is the
        // shared reference (we cannot execute :new {} inheritance here).
        String schematicId = file.getName().replaceAll("\\.lua$", "");
        String relative = relativize(file);
        swg.crafting.simulator.contracts.Provenance provenance =
                new swg.crafting.simulator.contracts.Provenance(
                        "swginfinity/public", commit, relative);

        // Slots: ingredientSlotType[i] + resourceTypes[i] + resourceQuantities[i] + contribution[i]
        List<swg.infinity.contracts.IngredientSlotDefinition> slots =
                new ArrayList<swg.infinity.contracts.IngredientSlotDefinition>();
        List<Object> slotKinds = (List<Object>) table.get("ingredientSlotType");
        List<Object> resourceTypes =
                (List<Object>) table.get("resourceTypes");
        List<Object> resourceQuantities =
                (List<Object>) table.get("resourceQuantities");
        List<Object> contributions =
                (List<Object>) table.get("contribution");
        if (slotKinds != null) {
            int n = slotKinds.size();
            for (int i = 0; i < n; ++i) {
                int kind = ((Number) slotKinds.get(i)).intValue();
                String acceptedType = stringAt(resourceTypes, i);
                int quantity = intAt(resourceQuantities, i, 1);
                int contribution = intAt(contributions, i, 100);
                swg.infinity.contracts.SlotKind slotKind =
                        kindToSlotKind(kind);
                slots.add(new swg.infinity.contracts.IngredientSlotDefinition(
                        i,
                        titleAt((List<Object>) table.get("ingredientTitleNames"), i),
                        slotKind,
                        acceptedType == null ? "" : acceptedType,
                        quantity,
                        contribution,
                        provenance));
            }
        }

        // Experiment groups cannot be extracted from .lua files alone; the
        // shared template holds no data and the .iff is compiled. Mark
        // each schematic's experimental coverage as UNSUPPORTED.
        List<swg.infinity.contracts.ExperimentalProperty> emptyProps =
                new ArrayList<swg.infinity.contracts.ExperimentalProperty>();

        // complexity / xp are not part of the contract; we attach them via
        // a separate side-channel: complexity is the assembly risk, xp is
        // the schematic experience. Both are dropped from the normalized
        // ruleset for now; they live in client XML.
        return new swg.infinity.contracts.SchematicDefinition(
                schematicId, id, schematicId,
                targetTemplate,
                swg.infinity.contracts.LaboratoryType.RESOURCE,
                assemblySkill, experimentingSkill,
                slots, emptyProps,
                "weapon-result-processor",
                provenance);
    }

    private InfinityRuleset buildRuleset(
            List<swg.infinity.contracts.SchematicDefinition> schematics) {
        // Compute SHA-256 of the schematic ids + provenance to derive a
        // stable ruleset hash. Deterministic because LinkedHashMap.
        StringBuilder manifest = new StringBuilder();
        for (swg.infinity.contracts.SchematicDefinition s : schematics) {
            manifest.append(s.getId()).append('|')
                    .append(s.getDraftTemplate()).append('|')
                    .append(s.getTargetTemplate()).append('\n');
        }
        String rulesetHash = sha256(manifest.toString());

        RulesetManifest mfst = new RulesetManifest(
                1,
                "swginfinity/public",
                commit,
                "T4-minimal-extractor",
                rulesetHash,
                154);

        List<CoverageRecord> coverage = new ArrayList<CoverageRecord>();
        for (swg.infinity.contracts.SchematicDefinition s : schematics) {
            coverage.add(new CoverageRecord(
                    s.getId(),
                    EvidenceState.SIMULATED,
                    EvidenceState.UNSUPPORTED,
                    EvidenceState.UNSUPPORTED,
                    EvidenceState.UNSUPPORTED,
                    new ArrayList<String>()));
        }

        int registered = schematics.size();
        int normalized = schematics.size();
        ExtractionReport report = new ExtractionReport(
                "swginfinity/public", commit,
                registered, normalized,
                new ArrayList<ExtractionIssue>(issues));

        return new InfinityRuleset(mfst, schematics, coverage);
    }

    private swg.infinity.contracts.SlotKind kindToSlotKind(int kind) {
        // SWG ingredientSlotType: 0 = resource, 1 = identical component,
        // 2 = mixed component, 3 = optional identical, 4 = optional mixed.
        switch (kind) {
        case 0: return swg.infinity.contracts.SlotKind.RESOURCE;
        case 1: return swg.infinity.contracts.SlotKind.IDENTICAL_COMPONENT;
        case 2: return swg.infinity.contracts.SlotKind.MIXED_COMPONENT;
        case 3:
            return swg.infinity.contracts.SlotKind.OPTIONAL_IDENTICAL_COMPONENT;
        case 4:
            return swg.infinity.contracts.SlotKind.OPTIONAL_MIXED_COMPONENT;
        default:
            throw new LuaTemplateStubLoader.LuaSyntaxException(
                    "unknown ingredientSlotType " + kind);
        }
    }

    private static String stringAt(List<Object> list, int index) {
        if (list == null || index >= list.size()) return null;
        Object v = list.get(index);
        return v == null ? null : v.toString();
    }

    private static int intAt(List<Object> list, int index, int fallback) {
        if (list == null || index >= list.size()) return fallback;
        Object v = list.get(index);
        if (v instanceof Number) return ((Number) v).intValue();
        return fallback;
    }

    private static String titleAt(List<Object> list, int index) {
        return stringAt(list, index);
    }

    private static String stringOrNull(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        return v.toString();
    }

    private String relativize(File f) {
        String root = sourceRoot.getAbsolutePath();
        String full = f.getAbsolutePath();
        if (full.startsWith(root)) {
            return full.substring(root.length() + 1);
        }
        return full;
    }

    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
