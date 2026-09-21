package swg.infinity.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.Provenance;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;

/**
 * Sandboxed extractor for SWG Infinity weapon tangible-object templates.
 * Reads weapon object .lua files from
 * {@code MMOCoreORB/bin/scripts/object/weapon/...} and emits the
 * experimental-property data plus the baseline weapon fields.
 *
 * <p>The data is what {@link swg.infinity.processor.WeaponResultProcessor}
 * and {@link swg.infinity.engine.ResourceLaboratory} consume to produce
 * deterministic attribute state, weighted scores, and final weapon
 * fields. Until this extractor runs, the .lua weapon templates were the
 * only source of truth and the rule engine produced empty attribute
 * maps (see FUNCTIONAL_PARITY_PENDING in
 * {@code docs/infinity/ACCEPTANCE_MATRIX.md}).</p>
 *
 * <p>The extractor is sandboxed: it reads only files under the supplied
 * {@code sourceRoot}, never accesses the classpath or network, and
 * fails closed on any unhandled Lua construct via
 * {@link LuaTemplateStubLoader.LuaSyntaxException}.</p>
 */
public final class WeaponTangibleTemplateExtractor {

    private final File sourceRoot;
    private final String commit;

    public WeaponTangibleTemplateExtractor(File sourceRoot, String commit) {
        if (sourceRoot == null) throw new NullPointerException("sourceRoot");
        if (commit == null
                || !commit.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException(
                    "commit must be a 40-character SHA");
        }
        this.sourceRoot = sourceRoot;
        this.commit = commit.toLowerCase();
    }

    /**
     * Walks the weapon object template tree and emits one
     * {@link WeaponObjectTemplate} per parsed weapon.
     */
    public Map<String, WeaponObjectTemplate> extract() throws java.io.IOException {
        File weaponsDir = new File(sourceRoot,
                "MMOCoreORB/bin/scripts/object/weapon");
        Map<String, WeaponObjectTemplate> out =
                new LinkedHashMap<String, WeaponObjectTemplate>();
        if (!weaponsDir.isDirectory()) return out;
        walk(weaponsDir, out);
        return out;
    }

    private void walk(File dir, Map<String, WeaponObjectTemplate> out)
            throws java.io.IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                walk(f, out);
                continue;
            }
            if (!f.getName().endsWith(".lua")) continue;
            try {
                Object parsed = LuaTemplateStubLoader.loadFirstTable(f);
                if (!(parsed instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> table = (Map<String, Object>) parsed;
                WeaponObjectTemplate template = parseOne(f, table);
                if (template != null) {
                    out.put(template.targetTemplate, template);
                }
            } catch (LuaTemplateStubLoader.LuaSyntaxException lse) {
                // Fail closed per plan — record nothing for unparsed.
            }
        }
    }

    @SuppressWarnings("unchecked")
    private WeaponObjectTemplate parseOne(File file, Map<String, Object> table) {
        Object etObj = table.get("experimentalProperties");
        Object ecObj = table.get("experimentalCombineType");
        Object minObj = table.get("experimentalMin");
        Object maxObj = table.get("experimentalMax");
        Object precObj = table.get("experimentalPrecision");
        Object grpObj = table.get("experimentalGroupTitles");
        Object subObj = table.get("experimentalSubGroupTitles");
        Object wtObj = table.get("experimentalWeights");
        Object nObj = table.get("numberExperimentalProperties");
        if (!(etObj instanceof List)
                || !(minObj instanceof List)
                || !(maxObj instanceof List)) {
            return null;
        }
        if (!(grpObj instanceof List)
                || !(subObj instanceof List)
                || !(nObj instanceof List)) {
            return null;
        }
        List<Object> props = (List<Object>) etObj;
        List<Object> mins = (List<Object>) minObj;
        List<Object> maxs = (List<Object>) maxObj;
        List<Object> precs = precObj instanceof List
                ? (List<Object>) precObj
                : Collections.emptyList();
        List<Object> groups = (List<Object>) grpObj;
        List<Object> subs = (List<Object>) subObj;
        List<Object> weights = wtObj instanceof List
                ? (List<Object>) wtObj : Collections.emptyList();
        List<Object> combineTypes = ecObj instanceof List
                ? (List<Object>) ecObj : Collections.emptyList();
        List<Object> counts = (List<Object>) nObj;

        Provenance prov = new Provenance(
                "swginfinity/public", commit,
                relativize(file));

        // Walk groups (numberExperimentalProperties[i] rows per group).
        // experimentalProperties/experimentalWeights are flat across rows;
        // min/max/precision/combine type are per group.
        int groupCount = Math.min(
                Math.min(groups.size(), subs.size()), counts.size());
        List<ExperimentalProperty> experimental =
                new ArrayList<ExperimentalProperty>();
        int flatCursor = 0;
        for (int g = 0; g < groupCount; ++g) {
            int rows = (int) numAt(counts, g);
            String sub = strAt(subs, g);
            String group = strAt(groups, g);
            if (sub == null || sub.isEmpty()
                    || "null".equals(sub)) {
                flatCursor += rows;
                continue;
            }

            double minVal = numAt(mins, g);
            double maxVal = numAt(maxs, g);
            int prec = (int) numAt(precs, g);
            int combine = (int) numAt(combineTypes, g);

            CombineType ct;
            try {
                ct = CombineType.fromSourceId(combine);
            } catch (IllegalArgumentException badCombine) {
                throw new LuaTemplateStubLoader.LuaSyntaxException(
                        "unknown experimentalCombineType " + combine
                        + " in " + file.getPath());
            }

            List<ResourceStat> statsForGroup =
                    new ArrayList<ResourceStat>();
            List<Integer> rawWeights =
                    new ArrayList<Integer>();
            int rawTotal = 0;

            for (int r = 0; r < rows; ++r) {
                int idx = flatCursor + r;
                String statCode = strAt(props, idx);
                if (statCode == null || "XX".equals(statCode)) continue;

                ResourceStat stat;
                try {
                    stat = ResourceStat.valueOf(statCode);
                } catch (IllegalArgumentException unknownStat) {
                    throw new LuaTemplateStubLoader.LuaSyntaxException(
                            "unknown resource stat " + statCode
                            + " in " + file.getPath());
                }

                int raw = (int) Math.round(numAt(weights, idx));
                if (raw <= 0) {
                    throw new LuaTemplateStubLoader.LuaSyntaxException(
                            "non-positive experimental weight " + raw
                            + " for " + statCode + " in " + file.getPath());
                }
                statsForGroup.add(stat);
                rawWeights.add(Integer.valueOf(raw));
                rawTotal += raw;
            }

            List<PropertyWeight> pws = new ArrayList<PropertyWeight>();
            if (rawTotal > 0) {
                for (int i = 0; i < statsForGroup.size(); ++i) {
                    int raw = rawWeights.get(i).intValue();
                    pws.add(new PropertyWeight(
                            statsForGroup.get(i),
                            raw,
                            (double) raw / (double) rawTotal));
                }
            }

            // Some fixed/baseline groups have no resource weights and are
            // represented separately in baselineFields.
            if (!pws.isEmpty()) {
                String attribute = normalizeAttributeName(sub);
                experimental.add(new ExperimentalProperty(
                        attribute,
                        group == null ? "" : group,
                        minVal, maxVal, prec, false,
                        ct,
                        Collections.unmodifiableList(pws),
                        prov));
            }
            flatCursor += rows;
        }

        // The target iff path is appended by ObjectTemplates:addTemplate(...)
        // and equals the path under object/weapon/.../iff.
        String rel = relativize(file);
        String targetTemplate = rel
                .replace("MMOCoreORB/bin/scripts/", "")
                .replaceAll("\\.lua$", ".iff");

        Map<String, Double> baseline = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, String> e : BASELINE_FIELDS.entrySet()) {
            Object v = table.get(e.getKey());
            if (v instanceof Number) {
                baseline.put(e.getValue(), Double.valueOf(
                        ((Number) v).doubleValue()));
            }
        }

        return new WeaponObjectTemplate(
                targetTemplate, prov, experimental, baseline, counts);
    }

    private static String normalizeAttributeName(String name) {
        // exp_durability group maps to hitpoints attribute in the engine.
        if ("exp_durability".equals(name)) return "hitpoints";
        return name;
    }

    private static String strAt(List<Object> list, int i) {
        if (list == null || i >= list.size()) return null;
        Object v = list.get(i);
        return v == null ? null : v.toString();
    }

    private static double numAt(List<Object> list, int i) {
        if (list == null || i >= list.size()) return 0.0d;
        Object v = list.get(i);
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); }
        catch (NumberFormatException nfe) { return 0.0d; }
    }

    private String relativize(File f) {
        String root = sourceRoot.getAbsolutePath();
        String full = f.getAbsolutePath();
        if (full.startsWith(root)) {
            return full.substring(root.length() + 1);
        }
        return full;
    }

    /** Baseline weapon fields emitted alongside the experimental list. */
    private static final Map<String, String> BASELINE_FIELDS;
    static {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("minDamage", "mindamage");
        m.put("maxDamage", "maxdamage");
        m.put("attackSpeed", "attackspeed");
        m.put("healthAttackCost", "attackhealthcost");
        m.put("actionAttackCost", "attackactioncost");
        m.put("mindAttackCost", "attackmindcost");
        m.put("forceCost", "forcecost");
        m.put("maxRange", "maxrange");
        m.put("idealRange", "idealrange");
        m.put("pointBlankRange", "pointblankrange");
        m.put("woundsRatio", "woundchance");
        BASELINE_FIELDS = Collections.unmodifiableMap(m);
    }

    /** Source-derived weapon tangible template. */
    public static final class WeaponObjectTemplate {
        private final String targetTemplate;
        private final Provenance provenance;
        private final List<ExperimentalProperty> experimentalProperties;
        private final Map<String, Double> baselineFields;
        @SuppressWarnings("unused")
        private final List<Object> numberExperimentalProperties;

        WeaponObjectTemplate(
                String targetTemplate,
                Provenance provenance,
                List<ExperimentalProperty> experimentalProperties,
                Map<String, Double> baselineFields,
                List<Object> numberExperimentalProperties) {
            this.targetTemplate = targetTemplate;
            this.provenance = provenance;
            this.experimentalProperties = Collections.unmodifiableList(
                    new ArrayList<ExperimentalProperty>(experimentalProperties));
            this.baselineFields = Collections.unmodifiableMap(
                    new LinkedHashMap<String, Double>(baselineFields));
            this.numberExperimentalProperties = Collections.unmodifiableList(
                    new ArrayList<Object>(numberExperimentalProperties));
        }

        public String getTargetTemplate() { return targetTemplate; }
        public Provenance getProvenance() { return provenance; }
        public List<ExperimentalProperty> getExperimentalProperties() {
            return experimentalProperties;
        }
        public Map<String, Double> getBaselineFields() { return baselineFields; }
    }
}
