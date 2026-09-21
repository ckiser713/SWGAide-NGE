package swg.infinity.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.contracts.RulesetManifest;
import swg.crafting.simulator.io.SimpleJson;
import swg.infinity.contracts.CombineType;
import swg.infinity.contracts.ExperimentalProperty;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.contracts.IngredientSlotDefinition;
import swg.infinity.contracts.LaboratoryType;
import swg.infinity.contracts.PropertyWeight;
import swg.infinity.contracts.ResourceStat;
import swg.infinity.contracts.SchematicDefinition;
import swg.infinity.contracts.SlotKind;
import swg.infinity.contracts.RulesetValidator;

/**
 * Deterministic JSON codec for source-free runtime Infinity ruleset artifacts.
 */
public final class InfinityRulesetJsonCodec {
    private InfinityRulesetJsonCodec() {
        throw new AssertionError("Do not instantiate");
    }

    public static void write(InfinityRuleset ruleset, Writer writer)
            throws IOException {
        if (ruleset == null) throw new NullPointerException("ruleset");
        if (writer == null) throw new NullPointerException("writer");
        RulesetValidator.validate(ruleset);
        writer.write(SimpleJson.stringify(toJson(ruleset)));
        writer.flush();
    }

    @SuppressWarnings("unchecked")
    public static InfinityRuleset read(InputStream in) throws IOException {
        Object parsed = SimpleJson.parse(in);
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("ruleset root must be object");
        }
        Map<String, Object> root = (Map<String, Object>) parsed;
        RulesetManifest manifest = parseManifest(object(root, "manifest"));

        List<SchematicDefinition> schematics =
                new ArrayList<SchematicDefinition>();
        for (Object value : array(root, "schematics")) {
            schematics.add(parseSchematic(asObject(value, "schematic")));
        }

        List<CoverageRecord> coverage =
                new ArrayList<CoverageRecord>();
        for (Object value : array(root, "coverage")) {
            coverage.add(parseCoverage(asObject(value, "coverage")));
        }

        InfinityRuleset ruleset =
                new InfinityRuleset(manifest, schematics, coverage);
        RulesetValidator.validate(ruleset);
        return ruleset;
    }

    private static Map<String, Object> toJson(InfinityRuleset ruleset) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        RulesetManifest manifest = ruleset.getManifest();

        Map<String, Object> mf = new LinkedHashMap<String, Object>();
        mf.put("schemaVersion", Integer.valueOf(manifest.getSchemaVersion()));
        mf.put("repository", manifest.getRepository());
        mf.put("commit", manifest.getCommit());
        mf.put("extractorVersion", manifest.getExtractorVersion());
        mf.put("rulesetHash", manifest.getRulesetHash());
        mf.put("swgAideServerId", Integer.valueOf(manifest.getSwgAideServerId()));
        root.put("manifest", mf);

        List<Object> schematics = new ArrayList<Object>();
        for (SchematicDefinition schematic : ruleset.getSchematics()) {
            schematics.add(schematicJson(schematic));
        }
        root.put("schematics", schematics);

        List<Object> coverage = new ArrayList<Object>();
        for (SchematicDefinition schematic : ruleset.getSchematics()) {
            CoverageRecord record = ruleset.getCoverage(schematic.getId());
            if (record != null) coverage.add(coverageJson(record));
        }
        root.put("coverage", coverage);
        return root;
    }

    private static Map<String, Object> schematicJson(
            SchematicDefinition schematic) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("id", schematic.getId());
        out.put("displayName", schematic.getDisplayName());
        out.put("draftTemplate", schematic.getDraftTemplate());
        out.put("targetTemplate", schematic.getTargetTemplate());
        out.put("laboratory", schematic.getLaboratory().name());
        out.put("assemblySkill", schematic.getAssemblySkill());
        out.put("experimentationSkill", schematic.getExperimentationSkill());
        out.put("processorId", schematic.getProcessorId());
        out.put("provenance", provenanceJson(schematic.getProvenance()));

        List<Object> slots = new ArrayList<Object>();
        for (IngredientSlotDefinition slot : schematic.getSlots()) {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("index", Integer.valueOf(slot.getIndex()));
            s.put("title", slot.getTitle());
            s.put("kind", slot.getKind().name());
            s.put("acceptedType", slot.getAcceptedType());
            s.put("quantity", Integer.valueOf(slot.getQuantity()));
            s.put("contribution", Double.valueOf(slot.getContribution()));
            s.put("provenance", provenanceJson(slot.getProvenance()));
            slots.add(s);
        }
        out.put("slots", slots);

        List<Object> properties = new ArrayList<Object>();
        for (ExperimentalProperty property : schematic.getProperties()) {
            Map<String, Object> p = new LinkedHashMap<String, Object>();
            p.put("attribute", property.getAttribute());
            p.put("group", property.getGroup());
            p.put("minValue", Double.valueOf(property.getMinValue()));
            p.put("maxValue", Double.valueOf(property.getMaxValue()));
            p.put("precision", Integer.valueOf(property.getPrecision()));
            p.put("hidden", Boolean.valueOf(property.isHidden()));
            p.put("combineType", property.getCombineType().name());
            p.put("provenance", provenanceJson(property.getProvenance()));
            List<Object> weights = new ArrayList<Object>();
            for (PropertyWeight weight : property.getWeights()) {
                Map<String, Object> w = new LinkedHashMap<String, Object>();
                w.put("stat", weight.getStat().name());
                w.put("rawWeight", Integer.valueOf(weight.getRawWeight()));
                w.put("normalizedWeight",
                        Double.valueOf(weight.getNormalizedWeight()));
                weights.add(w);
            }
            p.put("weights", weights);
            properties.add(p);
        }
        out.put("properties", properties);
        return out;
    }

    private static Map<String, Object> coverageJson(CoverageRecord record) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("schematicId", record.getSchematicId());
        out.put("extraction", record.getExtraction().name());
        out.put("laboratory", record.getLaboratory().name());
        out.put("components", record.getComponents().name());
        out.put("processor", record.getProcessor().name());
        out.put("fixtureIds", new ArrayList<String>(record.getFixtureIds()));
        return out;
    }

    private static Map<String, Object> provenanceJson(Provenance p) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("repository", p.getRepository());
        out.put("commit", p.getCommit());
        out.put("sourcePath", p.getSourcePath());
        return out;
    }

    private static RulesetManifest parseManifest(Map<String, Object> m) {
        return new RulesetManifest(
                integer(m, "schemaVersion"),
                string(m, "repository"),
                string(m, "commit"),
                string(m, "extractorVersion"),
                string(m, "rulesetHash"),
                integer(m, "swgAideServerId"));
    }

    private static SchematicDefinition parseSchematic(Map<String, Object> m) {
        Provenance provenance = parseProvenance(object(m, "provenance"));
        List<IngredientSlotDefinition> slots =
                new ArrayList<IngredientSlotDefinition>();
        for (Object value : array(m, "slots")) {
            Map<String, Object> s = asObject(value, "slot");
            slots.add(new IngredientSlotDefinition(
                    integer(s, "index"),
                    string(s, "title"),
                    SlotKind.valueOf(string(s, "kind")),
                    string(s, "acceptedType"),
                    integer(s, "quantity"),
                    number(s, "contribution"),
                    parseProvenance(object(s, "provenance"))));
        }

        List<ExperimentalProperty> properties =
                new ArrayList<ExperimentalProperty>();
        for (Object value : array(m, "properties")) {
            Map<String, Object> p = asObject(value, "property");
            List<PropertyWeight> weights =
                    new ArrayList<PropertyWeight>();
            for (Object weightValue : array(p, "weights")) {
                Map<String, Object> w = asObject(weightValue, "weight");
                weights.add(new PropertyWeight(
                        ResourceStat.valueOf(string(w, "stat")),
                        integer(w, "rawWeight"),
                        number(w, "normalizedWeight")));
            }
            properties.add(new ExperimentalProperty(
                    string(p, "attribute"),
                    string(p, "group"),
                    number(p, "minValue"),
                    number(p, "maxValue"),
                    integer(p, "precision"),
                    bool(p, "hidden"),
                    CombineType.valueOf(string(p, "combineType")),
                    weights,
                    parseProvenance(object(p, "provenance"))));
        }

        return new SchematicDefinition(
                string(m, "id"),
                string(m, "displayName"),
                string(m, "draftTemplate"),
                string(m, "targetTemplate"),
                LaboratoryType.valueOf(string(m, "laboratory")),
                string(m, "assemblySkill"),
                string(m, "experimentationSkill"),
                slots,
                properties,
                string(m, "processorId"),
                provenance);
    }

    private static CoverageRecord parseCoverage(Map<String, Object> m) {
        List<String> fixtures = new ArrayList<String>();
        for (Object value : array(m, "fixtureIds")) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        "coverage fixture id must be string");
            }
            fixtures.add((String) value);
        }
        return new CoverageRecord(
                string(m, "schematicId"),
                EvidenceState.valueOf(string(m, "extraction")),
                EvidenceState.valueOf(string(m, "laboratory")),
                EvidenceState.valueOf(string(m, "components")),
                EvidenceState.valueOf(string(m, "processor")),
                fixtures);
    }

    private static Provenance parseProvenance(Map<String, Object> m) {
        return new Provenance(
                string(m, "repository"),
                string(m, "commit"),
                string(m, "sourcePath"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(
            Map<String, Object> parent, String key) {
        return asObject(parent.get(key), key);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(
            Object value, String name) {
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(name + " must be object");
        }
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> array(
            Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        if (!(value instanceof List)) {
            throw new IllegalArgumentException(key + " must be array");
        }
        return (List<Object>) value;
    }

    private static String string(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(key + " must be string");
        }
        return (String) value;
    }

    private static int integer(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(key + " must be number");
        }
        return ((Number) value).intValue();
    }

    private static double number(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(key + " must be number");
        }
        return ((Number) value).doubleValue();
    }

    private static boolean bool(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(key + " must be boolean");
        }
        return ((Boolean) value).booleanValue();
    }
}
