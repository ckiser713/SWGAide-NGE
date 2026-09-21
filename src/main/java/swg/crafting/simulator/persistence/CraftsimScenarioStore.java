package swg.crafting.simulator.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.crafting.simulator.components.ComponentProperty;
import swg.crafting.simulator.contracts.Provenance;
import swg.crafting.simulator.scenario.CraftOutcomeTier;
import swg.crafting.simulator.scenario.ExperimentStep;
import swg.infinity.component.ComponentOrigin;
import swg.infinity.engine.ResourceOrigin;
import swg.infinity.contracts.ResourceStat;

/**
 * Isolated, versioned simulator scenario storage. Files live under
 * {@code <baseDir>/scenarios/<id>.craftsim.json}. The store never touches
 * legacy {@code SWGAide.DAT}; it is a new directory tree that may be
 * removed without affecting any existing persistence.
 *
 * <p>Deserialization enforces the ruleset-hash contract: a scenario is
 * rejected if the supplied {@code expectedRulesetHash} does not match
 * the hash pinned in the file. This prevents silently loading scenarios
 * crafted against a different ruleset version.</p>
 *
 * <p>Save is atomic: the JSON is written to {@code <id>.craftsim.json.tmp}
 * and then renamed over the final file. A failed write leaves the prior
 * file (if any) untouched.</p>
 */
public final class CraftsimScenarioStore {

    private final File baseDir;

    public CraftsimScenarioStore(File baseDir) {
        if (baseDir == null) throw new NullPointerException("baseDir");
        this.baseDir = baseDir;
    }

    public File getBaseDir() {
        return baseDir;
    }

    /**
     * Saves the scenario atomically under {@code <id>.craftsim.json}.
     * The id is validated against {@link CraftsimScenario#validateId};
     * the resolved file path is verified to be a child of the scenarios
     * directory.
     */
    public void save(CraftsimScenario scenario) throws IOException {
        if (scenario == null) throw new NullPointerException("scenario");
        CraftsimScenario.validateId(scenario.getId());

        File dir = new File(baseDir, "scenarios");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("cannot create " + dir);
        }
        File finalFile = new File(dir, scenario.getId() + ".craftsim.json");
        ensureChildOf(dir, finalFile);

        File tmp = new File(dir, scenario.getId() + ".craftsim.json.tmp");
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(tmp), StandardCharsets.UTF_8));
            w.write(toJson(scenario));
            w.flush();
            w.close();
            w = null;
            if (!tmp.renameTo(finalFile)) {
                // Fall back: delete existing then rename again
                if (finalFile.exists() && !finalFile.delete()) {
                    throw new IOException(
                            "cannot replace " + finalFile);
                }
                if (!tmp.renameTo(finalFile)) {
                    throw new IOException("cannot rename " + tmp);
                }
            }
        } finally {
            if (w != null) {
                try { w.close(); } catch (IOException ignored) {}
            }
            // Best-effort cleanup of leftover tmp
            if (tmp.exists()) tmp.delete();
        }
    }

    public CraftsimScenario load(
            String id, String expectedRulesetHash) throws IOException {
        if (id == null) throw new NullPointerException("id");
        if (expectedRulesetHash == null) {
            throw new NullPointerException("expectedRulesetHash");
        }
        CraftsimScenario.validateId(id);

        File dir = new File(baseDir, "scenarios");
        File file = new File(dir, id + ".craftsim.json");
        ensureChildOf(dir, file);
        if (!file.isFile()) {
            throw new IOException("missing scenario: " + file);
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            r.close();
        }
        CraftsimScenario s = fromJson(sb.toString());
        if (!s.getRulesetHash().equalsIgnoreCase(expectedRulesetHash)) {
            throw new IllegalStateException(
                    "ruleset hash mismatch: expected="
                    + expectedRulesetHash
                    + " actual=" + s.getRulesetHash());
        }
        return s;
    }

    public List<String> listIds() {
        File dir = new File(baseDir, "scenarios");
        File[] files = dir.listFiles();
        if (files == null) return Collections.emptyList();
        List<String> ids = new ArrayList<String>(files.length);
        for (File f : files) {
            String n = f.getName();
            if (n.endsWith(".craftsim.json")) {
                ids.add(n.substring(0, n.length() - ".craftsim.json".length()));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    public boolean delete(String id) {
        if (id == null) return false;
        try {
            CraftsimScenario.validateId(id);
        } catch (RuntimeException re) {
            return false;
        }
        File dir = new File(baseDir, "scenarios");
        File file = new File(dir, id + ".craftsim.json");
        ensureChildOf(dir, file);
        return file.delete();
    }

    private static void ensureChildOf(File parent, File candidate) {
        try {
            String parentPath = parent.getCanonicalPath();
            String candPath = candidate.getCanonicalPath();
            if (!candPath.equals(parentPath)
                    && !candPath.startsWith(parentPath + File.separator)) {
                throw new IllegalArgumentException(
                        "file escapes scenarios directory: "
                        + candidate.getPath());
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    static String toJson(CraftsimScenario s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(s.getVersion()).append(",\n");
        sb.append("  \"id\": \"").append(jsonEscape(s.getId())).append("\",\n");
        sb.append("  \"name\": \"").append(jsonEscape(s.getName())).append("\",\n");
        sb.append("  \"saved_epoch_seconds\": ")
                .append(s.getSavedEpochSeconds()).append(",\n");
        sb.append("  \"server_id\": ")
                .append(s.getServer().getServerId()).append(",\n");
        sb.append("  \"server_name\": \"")
                .append(jsonEscape(s.getServer().getServerName()))
                .append("\",\n");
        sb.append("  \"rules_provider_id\": \"")
                .append(jsonEscape(s.getRulesProviderId())).append("\",\n");
        sb.append("  \"ruleset_commit\": \"")
                .append(jsonEscape(s.getRulesetCommit())).append("\",\n");
        sb.append("  \"ruleset_hash\": \"")
                .append(jsonEscape(s.getRulesetHash())).append("\",\n");
        sb.append("  \"schematic_id\": \"")
                .append(jsonEscape(s.getSchematicId())).append("\",\n");
        sb.append("  \"schematic_name\": \"")
                .append(jsonEscape(s.getSchematicName())).append("\",\n");
        sb.append("  \"assembly_outcome\": \"")
                .append(s.getAssemblyOutcome().name()).append("\",\n");

        sb.append("  \"resources\": [\n");
        boolean first = true;
        for (CraftsimScenario.SavedResource r : s.getResources()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append(resourceJson(r));
        }
        sb.append("\n  ],\n");

        sb.append("  \"components\": [\n");
        first = true;
        for (CraftsimScenario.SavedComponentSlot slot : s.getComponents()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append(componentSlotJson(slot));
        }
        sb.append("\n  ],\n");

        sb.append("  \"experiments\": [\n");
        first = true;
        for (ExperimentStep e : s.getExperiments()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    {\"group\": \"")
                    .append(jsonEscape(e.getGroup()))
                    .append("\", \"points\": ").append(e.getPoints())
                    .append(", \"outcome\": \"")
                    .append(e.getOutcome().name()).append("\"}");
        }
        sb.append("\n  ]\n");

        sb.append("}\n");
        return sb.toString();
    }

    private static String resourceJson(CraftsimScenario.SavedResource r) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {")
                .append("\"slot_index\": ").append(r.getSlotIndex())
                .append(", \"name\": \"")
                .append(jsonEscape(r.getName())).append("\"")
                .append(", \"resource_type\": \"")
                .append(jsonEscape(r.getResourceType())).append("\"")
                .append(", \"origin\": \"")
                .append(r.getOrigin().name()).append("\"")
                .append(", \"available_quantity\": ")
                .append(r.getAvailableQuantity())
                .append(", \"stats\": {");
        boolean first = true;
        for (Map.Entry<ResourceStat, Integer> e : r.getStats().entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("\"").append(e.getKey().name()).append("\": ")
                    .append(e.getValue().intValue());
        }
        sb.append("}}");
        return sb.toString();
    }

    private static String componentSlotJson(
            CraftsimScenario.SavedComponentSlot slot) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\"slot_index\": ").append(slot.getSlotIndex())
                .append(", \"uses\": [");
        boolean first = true;
        for (CraftsimScenario.SavedComponentUse u : slot.getUses()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("{\"id\": \"").append(jsonEscape(u.getId()))
                    .append("\", \"template_id\": \"")
                    .append(jsonEscape(u.getTemplateId()))
                    .append("\", \"serial\": \"")
                    .append(jsonEscape(u.getSerial()))
                    .append("\", \"uses\": ").append(u.getUses())
                    .append(", \"origin\": \"")
                    .append(u.getOrigin().name()).append("\"");
            sb.append(", \"properties\": [");
            boolean fp = true;
            for (ComponentProperty p : u.getProperties()) {
                if (!fp) sb.append(", ");
                fp = false;
                sb.append("{\"name\": \"")
                        .append(jsonEscape(p.getAttribute()))
                        .append("\", \"value\": ")
                        .append(p.getValue())
                        .append(", \"precision\": ")
                        .append(p.getPrecision())
                        .append(", \"hidden\": ")
                        .append(p.isHidden())
                        .append("}");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    static CraftsimScenario fromJson(String text) {
        Map<String, Object> map = MiniJson.parseObject(text);
        int version = ((Number) map.get("version")).intValue();

        if (version == CraftsimScenario.CURRENT_VERSION) {
            return fromJsonV2(map);
        }
        if (version < CraftsimScenario.CURRENT_VERSION) {
            CraftsimScenario v1 = fromJsonV1(map);
            return CraftsimMigration.migrate(
                    v1, CraftsimScenario.CURRENT_VERSION);
        }
        throw new IllegalStateException(
                "Scenario on-disk version is newer than supported: " + version);
    }

    @SuppressWarnings("unchecked")
    private static CraftsimScenario fromJsonV2(Map<String, Object> map) {
        String id = (String) map.get("id");
        String name = (String) map.get("name");
        long epoch = ((Number) map.get("saved_epoch_seconds")).longValue();
        int serverId = ((Number) map.get("server_id")).intValue();
        String serverName = (String) map.get("server_name");
        String provider = (String) map.get("rules_provider_id");
        String commit = (String) map.get("ruleset_commit");
        String hash = (String) map.get("ruleset_hash");
        String schemId = (String) map.get("schematic_id");
        String schemName = (String) map.get("schematic_name");
        String outcomeStr = (String) map.get("assembly_outcome");
        CraftOutcomeTier outcome = CraftOutcomeTier.valueOf(outcomeStr);

        List<CraftsimScenario.SavedResource> resources =
                new ArrayList<CraftsimScenario.SavedResource>();
        Object resObj = map.get("resources");
        if (resObj instanceof List) {
            for (Object o : (List<Object>) resObj) {
                Map<String, Object> rm = (Map<String, Object>) o;
                int idx = ((Number) rm.get("slot_index")).intValue();
                String rn = (String) rm.get("name");
                String rt = (String) rm.get("resource_type");
                String ro = (String) rm.get("origin");
                long aq = ((Number) rm.get("available_quantity")).longValue();
                Map<ResourceStat, Integer> stats =
                        new EnumMap<ResourceStat, Integer>(ResourceStat.class);
                Object sobj = rm.get("stats");
                if (sobj instanceof Map) {
                    for (Map.Entry<String, Object> e :
                            ((Map<String, Object>) sobj).entrySet()) {
                        stats.put(ResourceStat.valueOf(e.getKey()),
                                Integer.valueOf(((Number) e.getValue()).intValue()));
                    }
                }
                resources.add(new CraftsimScenario.SavedResource(
                        idx, rn, rt, stats,
                        ro == null ? ResourceOrigin.MANUAL
                                : ResourceOrigin.valueOf(ro),
                        aq));
            }
        }

        List<CraftsimScenario.SavedComponentSlot> components =
                new ArrayList<CraftsimScenario.SavedComponentSlot>();
        Object cobj = map.get("components");
        if (cobj instanceof List) {
            for (Object o : (List<Object>) cobj) {
                Map<String, Object> cm = (Map<String, Object>) o;
                int idx = ((Number) cm.get("slot_index")).intValue();
                List<CraftsimScenario.SavedComponentUse> uses =
                        new ArrayList<CraftsimScenario.SavedComponentUse>();
                Object ubj = cm.get("uses");
                if (ubj instanceof List) {
                    for (Object uo : (List<Object>) ubj) {
                        Map<String, Object> um = (Map<String, Object>) uo;
                        String uid = (String) um.get("id");
                        String tpl = (String) um.get("template_id");
                        String serial = (String) um.get("serial");
                        int u = ((Number) um.get("uses")).intValue();
                        String orig = (String) um.get("origin");
                        List<ComponentProperty> props =
                                new ArrayList<ComponentProperty>();
                        Object pobj = um.get("properties");
                        if (pobj instanceof List) {
                            for (Object po : (List<Object>) pobj) {
                                Map<String, Object> pm =
                                        (Map<String, Object>) po;
                                String pa = (String) pm.get("name");
                                double pv = ((Number) pm.get("value")).doubleValue();
                                props.add(new ComponentProperty(
                                        pa, pv, 2, "", false,
                                        new Provenance(
                                                "swg.crafting.simulator.persistence",
                                                "0000000000000000000000000000000000000000",
                                                "saved-component-property")));
                            }
                        }
                        uses.add(new CraftsimScenario.SavedComponentUse(
                                uid, tpl, serial, u,
                                orig == null ? ComponentOrigin.MANUAL
                                        : ComponentOrigin.valueOf(orig),
                                props));
                    }
                }
                components.add(
                        new CraftsimScenario.SavedComponentSlot(idx, uses));
            }
        }

        List<ExperimentStep> experiments = new ArrayList<ExperimentStep>();
        Object eobj = map.get("experiments");
        if (eobj instanceof List) {
            for (Object eo : (List<Object>) eobj) {
                Map<String, Object> em = (Map<String, Object>) eo;
                String g = (String) em.get("group");
                int p = ((Number) em.get("points")).intValue();
                String on = (String) em.get("outcome");
                experiments.add(new ExperimentStep(
                        g, p, CraftOutcomeTier.valueOf(on)));
            }
        }

        return new CraftsimScenario(
                id,
                new CraftsimScenario.ServerInfo(serverId, serverName),
                provider, commit, hash,
                schemId, schemName,
                outcome,
                resources, components, experiments,
                name, epoch);
    }

    /**
     * Reads a v1 legacy format. The legacy envelope was
     * {@code id, rules_provider_id, ruleset_commit, ruleset_hash, name,
     * saved_epoch_seconds, attributes: Map<String,String>}. The minimal
     * migration shapes it into the v2 model so the migration framework
     * has something concrete to operate on.
     */
    @SuppressWarnings("unchecked")
    private static CraftsimScenario fromJsonV1(Map<String, Object> map) {
        String id = (String) map.get("id");
        String provider = (String) map.get("rules_provider_id");
        String commit = (String) map.get("ruleset_commit");
        String hash = (String) map.get("ruleset_hash");
        String name = (String) map.get("name");
        long epoch = ((Number) map.get("saved_epoch_seconds")).longValue();
        Map<String, String> attrs = (Map<String, String>) map.get("attributes");
        if (attrs == null) attrs = new LinkedHashMap<String, String>();

        String schemId = attrs.containsKey("schematic")
                ? attrs.get("schematic") : "";
        CraftsimScenario v1 = new CraftsimScenario(
                id,
                new CraftsimScenario.ServerInfo(154, "legacy"),
                provider, commit, hash,
                schemId, schemId,
                CraftOutcomeTier.GREAT,
                Collections.<CraftsimScenario.SavedResource>emptyList(),
                Collections.<CraftsimScenario.SavedComponentSlot>emptyList(),
                Collections.<ExperimentStep>emptyList(),
                name, epoch);
        // Mark as the on-disk version 1 so the migration framework
        // picks the v1 -> v2 migration.
        return v1.atVersion(1);
    }

    static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
            case '"': sb.append("\\\""); break;
            case '\\': sb.append("\\\\"); break;
            case '\n': sb.append("\\n"); break;
            case '\r': sb.append("\\r"); break;
            case '\t': sb.append("\\t"); break;
            default:
                if (c < 0x20) {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /** Computes SHA-256 of the supplied text, lowercase hex. */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Tiny JSON reader used only by the scenario loader. */
    static final class MiniJson {
        static Map<String, Object> parseObject(String text) {
            int[] pos = new int[] { 0 };
            skipWhitespace(text, pos);
            if (pos[0] >= text.length() || text.charAt(pos[0]) != '{') {
                throw new IllegalArgumentException("expected '{'");
            }
            pos[0]++;
            return readObject(text, pos);
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> readObject(String text, int[] pos) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            skipWhitespace(text, pos);
            if (pos[0] < text.length() && text.charAt(pos[0]) == '}') {
                pos[0]++;
                return map;
            }
            while (true) {
                skipWhitespace(text, pos);
                String key = readString(text, pos);
                skipWhitespace(text, pos);
                if (text.charAt(pos[0]) != ':') {
                    throw new IllegalArgumentException("expected ':'");
                }
                pos[0]++;
                Object value = readValue(text, pos);
                map.put(key, value);
                skipWhitespace(text, pos);
                if (pos[0] >= text.length()) {
                    throw new IllegalArgumentException("unterminated object");
                }
                char c = text.charAt(pos[0]);
                if (c == ',') {
                    pos[0]++;
                    continue;
                }
                if (c == '}') {
                    pos[0]++;
                    return map;
                }
                throw new IllegalArgumentException(
                        "expected ',' or '}' at " + pos[0]);
            }
        }

        private static Object readValue(String text, int[] pos) {
            skipWhitespace(text, pos);
            char c = text.charAt(pos[0]);
            if (c == '{') {
                pos[0]++;
                return readObject(text, pos);
            }
            if (c == '[') {
                pos[0]++;
                return readArray(text, pos);
            }
            if (c == '"') {
                return readString(text, pos);
            }
            if (c == 't' || c == 'f') {
                if (text.startsWith("true", pos[0])) {
                    pos[0] += 4;
                    return Boolean.TRUE;
                }
                pos[0] += 5;
                return Boolean.FALSE;
            }
            if (c == 'n') {
                pos[0] += 4;
                return null;
            }
            int start = pos[0];
            if (c == '-') pos[0]++;
            while (pos[0] < text.length()
                    && "0123456789.eE+-".indexOf(text.charAt(pos[0])) >= 0) {
                pos[0]++;
            }
            String t = text.substring(start, pos[0]);
            if (t.indexOf('.') < 0 && t.indexOf('e') < 0 && t.indexOf('E') < 0) {
                try {
                    return Long.valueOf(t);
                } catch (NumberFormatException ignored) {}
            }
            return Double.valueOf(t);
        }

        private static List<Object> readArray(String text, int[] pos) {
            List<Object> list = new ArrayList<Object>();
            skipWhitespace(text, pos);
            if (pos[0] < text.length() && text.charAt(pos[0]) == ']') {
                pos[0]++;
                return list;
            }
            while (true) {
                skipWhitespace(text, pos);
                list.add(readValue(text, pos));
                skipWhitespace(text, pos);
                if (pos[0] >= text.length()) {
                    throw new IllegalArgumentException("unterminated array");
                }
                char c = text.charAt(pos[0]);
                if (c == ',') {
                    pos[0]++;
                    continue;
                }
                if (c == ']') {
                    pos[0]++;
                    return list;
                }
                throw new IllegalArgumentException(
                        "expected ',' or ']' at " + pos[0]);
            }
        }

        private static String readString(String text, int[] pos) {
            if (pos[0] >= text.length()
                    || text.charAt(pos[0]) != '"') {
                throw new IllegalArgumentException("expected '\"'");
            }
            pos[0]++;
            StringBuilder sb = new StringBuilder();
            while (pos[0] < text.length()) {
                char c = text.charAt(pos[0]++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char esc = text.charAt(pos[0]++);
                    switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: throw new IllegalArgumentException(
                            "unknown escape: \\" + esc);
                    }
                    continue;
                }
                sb.append(c);
            }
            throw new IllegalArgumentException("unterminated string");
        }

        private static void skipWhitespace(String text, int[] pos) {
            while (pos[0] < text.length()
                    && Character.isWhitespace(text.charAt(pos[0]))) {
                pos[0]++;
            }
        }
    }
}
