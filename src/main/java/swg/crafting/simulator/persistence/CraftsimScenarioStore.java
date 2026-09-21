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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public void save(CraftsimScenario scenario) throws IOException {
        if (scenario == null) throw new NullPointerException("scenario");
        File dir = new File(baseDir, "scenarios");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("cannot create " + dir);
        }
        File file = new File(dir, scenario.getId() + ".craftsim.json");
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8));
        try {
            w.write(toJson(scenario));
        } finally {
            w.close();
        }
    }

    public CraftsimScenario load(
            String id, String expectedRulesetHash) throws IOException {
        if (id == null) throw new NullPointerException("id");
        if (expectedRulesetHash == null) {
            throw new NullPointerException("expectedRulesetHash");
        }
        File file = new File(
                new File(baseDir, "scenarios"), id + ".craftsim.json");
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

    static String toJson(CraftsimScenario s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(s.getVersion()).append(",\n");
        sb.append("  \"id\": \"").append(jsonEscape(s.getId())).append("\",\n");
        sb.append("  \"rules_provider_id\": \"")
                .append(jsonEscape(s.getRulesProviderId())).append("\",\n");
        sb.append("  \"ruleset_commit\": \"")
                .append(jsonEscape(s.getRulesetCommit())).append("\",\n");
        sb.append("  \"ruleset_hash\": \"")
                .append(jsonEscape(s.getRulesetHash())).append("\",\n");
        sb.append("  \"name\": \"").append(jsonEscape(s.getName())).append("\",\n");
        sb.append("  \"saved_epoch_seconds\": ")
                .append(s.getSavedEpochSeconds()).append(",\n");
        sb.append("  \"attributes\": {\n");
        boolean first = true;
        for (Map.Entry<String, String> e : s.getAttributes().entrySet()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    \"")
                    .append(jsonEscape(e.getKey()))
                    .append("\": \"")
                    .append(jsonEscape(e.getValue()))
                    .append("\"");
        }
        sb.append("\n  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    static CraftsimScenario fromJson(String text) {
        Map<String, Object> map = MiniJson.parseObject(text);
        int version = ((Number) map.get("version")).intValue();
        String id = (String) map.get("id");
        String rulesProviderId = (String) map.get("rules_provider_id");
        String rulesetCommit = (String) map.get("ruleset_commit");
        String rulesetHash = (String) map.get("ruleset_hash");
        String name = (String) map.get("name");
        long savedEpoch = ((Number) map.get("saved_epoch_seconds")).longValue();
        @SuppressWarnings("unchecked")
        Map<String, String> attrs =
                (Map<String, String>) map.get("attributes");
        if (attrs == null) attrs = new LinkedHashMap<String, String>();
        CraftsimScenario s = new CraftsimScenario(
                id, rulesProviderId, rulesetCommit, rulesetHash,
                name, savedEpoch, attrs);
        if (version != CraftsimScenario.CURRENT_VERSION) {
            return CraftsimMigration.migrate(s, CraftsimScenario.CURRENT_VERSION);
        }
        return s;
    }

    static String jsonEscape(String s) {
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
            if (text.charAt(pos[0]) == '}') {
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
            // number
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

        private static String readString(String text, int[] pos) {
            if (text.charAt(pos[0]) != '"') {
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
