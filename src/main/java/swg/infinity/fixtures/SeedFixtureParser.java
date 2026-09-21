package swg.infinity.fixtures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-rolled minimal JSON parser sufficient for {@link SeedFixture} seed
 * files. Deliberately does not cover the full JSON grammar: only object,
 * array, string, number, true, false, null, and nested combinations.
 *
 * <p>Fail-closed on any unhandled construct. Deterministic: the parser never
 * relies on locale-sensitive formatters, time, or iteration order.</p>
 *
 * <p>This class lives in the Infinity fixtures module because the Infinity
 * seed fixtures are the only consumer; if a non-Infinity server module ever
 * needs seed fixtures, the parser should be promoted to a generic module.</p>
 */
public final class SeedFixtureParser {

    private final String source;
    private int position;

    private SeedFixtureParser(String source) {
        this.source = source;
        this.position = 0;
    }

    public static Object parse(String text) {
        if (text == null) throw new NullPointerException("text");
        SeedFixtureParser p = new SeedFixtureParser(text);
        p.skipWhitespace();
        Object result = p.parseValue();
        p.skipWhitespace();
        if (p.position < p.source.length()) {
            throw new IllegalArgumentException(
                    "Unexpected trailing content at position " + p.position);
        }
        return result;
    }

    public static Object parseFile(File file) throws IOException {
        if (file == null) throw new NullPointerException("file");
        InputStream in = new FileInputStream(file);
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return parse(sb.toString());
        } finally {
            in.close();
        }
    }

    @SuppressWarnings("unchecked")
    public static SeedFixture parseSeed(Object json) {
        if (!(json instanceof Map)) {
            throw new IllegalArgumentException("seed root must be an object");
        }
        Map<String, Object> root = (Map<String, Object>) json;
        String provenance = stringOrNull(root, "provenance");
        if (provenance == null || !"source-pinned".equals(provenance)) {
            throw new IllegalArgumentException(
                    "seed.provenance must be 'source-pinned' (got: "
                    + provenance + ")");
        }
        String id = requireString(root, "id");
        String sourceSha = requireString(root, "source_sha");
        String sourcePath = requireString(root, "source_path");
        String schematicId = requireString(root, "schematic_id");
        String family = requireString(root, "family");
        String subcategory = requireString(root, "subcategory");
        String name = requireString(root, "name");
        int complexity = requireInt(root, "complexity");
        int size = requireInt(root, "size");
        String xpType = requireString(root, "xp_type");
        int xp = requireInt(root, "xp");
        String assemblySkill = requireString(root, "assembly_skill");
        String experimentingSkill = requireString(root, "experimenting_skill");
        String customizationSkill = requireString(root, "customization_skill");
        String targetTemplate = requireString(root, "target_template");

        List<Object> slotList = requireArray(root, "slots");
        List<SeedFixture.SeedSlot> slots =
                new ArrayList<SeedFixture.SeedSlot>(slotList.size());
        for (Object item : slotList) {
            slots.add(parseSlot(item));
        }

        Object statsRaw = root.get("resource_stats");
        Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
        if (statsRaw != null) {
            if (!(statsRaw instanceof Map)) {
                throw new IllegalArgumentException(
                        "resource_stats must be an object");
            }
            for (Map.Entry<String, Object> e :
                    ((Map<String, Object>) statsRaw).entrySet()) {
                if (!(e.getValue() instanceof Number)) {
                    throw new IllegalArgumentException(
                            "resource_stats value must be a number: "
                            + e.getKey());
                }
                stats.put(e.getKey(),
                        Integer.valueOf(((Number) e.getValue()).intValue()));
            }
        }

        List<SeedFixture.SeedExperimentGroup> groups =
                new ArrayList<SeedFixture.SeedExperimentGroup>();
        Object groupsRaw = root.get("experiment_groups");
        if (groupsRaw != null) {
            List<Object> groupList = requireArray(root, "experiment_groups");
            for (Object item : groupList) {
                groups.add(parseExperimentGroup(item));
            }
        }

        return new SeedFixture(
                id, sourceSha, sourcePath, schematicId,
                family, subcategory, name,
                complexity, size, xpType, xp,
                assemblySkill, experimentingSkill, customizationSkill,
                targetTemplate, slots, stats, groups);
    }

    private static SeedFixture.SeedSlot parseSlot(Object json) {
        Map<String, Object> obj = asObject(json);
        int index = requireInt(obj, "index");
        String title = requireString(obj, "title");
        String resourceType = requireString(obj, "resource_type");
        int quantity = requireInt(obj, "quantity");
        int contribution = requireInt(obj, "contribution");
        int slotKind = requireInt(obj, "slot_kind");
        return new SeedFixture.SeedSlot(
                index, title, resourceType, quantity, contribution, slotKind);
    }

    private static SeedFixture.SeedExperimentGroup parseExperimentGroup(
            Object json) {
        Map<String, Object> obj = asObject(json);
        String attribute = requireString(obj, "attribute");
        String group = requireString(obj, "group");
        double minValue = requireDouble(obj, "min_value");
        double maxValue = requireDouble(obj, "max_value");
        int precision = requireInt(obj, "precision");
        return new SeedFixture.SeedExperimentGroup(
                attribute, group, minValue, maxValue, precision);
    }

    private Object parseValue() {
        skipWhitespace();
        if (position >= source.length()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
        char c = source.charAt(position);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        return parseNumber();
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                position++;
                continue;
            }
            if (c == '}') {
                position++;
                return map;
            }
            throw new IllegalArgumentException(
                    "Expected ',' or '}' at position " + position);
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> list = new ArrayList<Object>();
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return list;
        }
        while (true) {
            list.add(parseValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                position++;
                continue;
            }
            if (c == ']') {
                position++;
                return list;
            }
            throw new IllegalArgumentException(
                    "Expected ',' or ']' at position " + position);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (position < source.length()) {
            char c = source.charAt(position++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (position >= source.length()) {
                    throw new IllegalArgumentException("Bad escape");
                }
                char esc = source.charAt(position++);
                switch (esc) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (position + 4 > source.length()) {
                        throw new IllegalArgumentException("Bad unicode escape");
                    }
                    String hex = source.substring(position, position + 4);
                    position += 4;
                    sb.append((char) Integer.parseInt(hex, 16));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown escape: \\" + esc);
                }
                continue;
            }
            sb.append(c);
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private Boolean parseBoolean() {
        if (source.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (source.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(
                "Invalid literal at position " + position);
    }

    private Object parseNull() {
        if (source.startsWith("null", position)) {
            position += 4;
            return null;
        }
        throw new IllegalArgumentException(
                "Invalid literal at position " + position);
    }

    private Number parseNumber() {
        int start = position;
        if (peek() == '-') position++;
        while (position < source.length()
                && "0123456789.eE+-".indexOf(source.charAt(position)) >= 0) {
            // Advance past digits / exponent / sign.
            // JSON spec allows at most one decimal and one exponent; for the
            // narrow grammar of seed fixtures we accept any digit/sign/dot/
            // e/E sequence and parse with Double.parseDouble at the end.
            position++;
        }
        String text = source.substring(start, position);
        if (text.isEmpty() || "-".equals(text)) {
            throw new IllegalArgumentException(
                    "Invalid number at position " + start);
        }
        if (text.indexOf('.') < 0 && text.indexOf('e') < 0 && text.indexOf('E') < 0) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ignored) {
                // fall through to double
            }
        }
        return Double.valueOf(text);
    }

    private void expect(char c) {
        if (position >= source.length() || source.charAt(position) != c) {
            throw new IllegalArgumentException(
                    "Expected '" + c + "' at position " + position);
        }
        position++;
    }

    private char peek() {
        if (position >= source.length()) {
            throw new IllegalArgumentException(
                    "Unexpected end of input at position " + position);
        }
        return source.charAt(position);
    }

    private void skipWhitespace() {
        while (position < source.length()
                && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }

    private static String requireString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof String)) {
            throw new IllegalArgumentException(
                    key + " must be a string (got: " + (v == null ? "null"
                            : v.getClass().getSimpleName()) + ")");
        }
        return (String) v;
    }

    private static String stringOrNull(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    private static int requireInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof Number)) {
            throw new IllegalArgumentException(
                    key + " must be a number");
        }
        return ((Number) v).intValue();
    }

    private static double requireDouble(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof Number)) {
            throw new IllegalArgumentException(
                    key + " must be a number");
        }
        return ((Number) v).doubleValue();
    }

    private static List<Object> requireArray(
            Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof List)) {
            throw new IllegalArgumentException(key + " must be an array");
        }
        return (List<Object>) v;
    }

    private static Map<String, Object> asObject(Object json) {
        if (!(json instanceof Map)) {
            throw new IllegalArgumentException(
                    "expected object, got " + json.getClass().getSimpleName());
        }
        return (Map<String, Object>) json;
    }
}
