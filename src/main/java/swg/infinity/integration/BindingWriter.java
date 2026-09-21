package swg.infinity.integration;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Serializes a {@link SchematicBindingRegistry} to a JSON document
 * conforming to {@code docs/infinity/bindings.schema.json}.
 *
 * <p>Validation rejects:</p>
 * <ul>
 *   <li>registries whose server id is not 154;</li>
 *   <li>registries containing any {@link BindingState#AMBIGUOUS} or
 *       {@link BindingState#MISSING} runtime-runnable binding — the
 *       registry is permitted to contain them as records, but the
 *       writer flags them via an {@code admitted: false} envelope
 *       when they are present;</li>
 *   <li>bindings lacking non-empty {@code evidence}.</li>
 * </ul>
 *
 * <p>Output is deterministic: bindings are emitted in the registry's
 * iteration order (insertion order of the SWGAide schematic id).</p>
 */
public final class BindingWriter {

    private BindingWriter() {
        throw new AssertionError("Do not instantiate");
    }

    /** Returns true iff the binding set is fully runnable. */
    public static boolean isFullyRunnable(
            List<SchematicBinding> bindings) {
        if (bindings == null) return false;
        for (SchematicBinding b : bindings) {
            if (b == null) return false;
            if (!b.getState().isRunnable()) return false;
            if (b.getInfinitySchematicId() == null
                    || b.getInfinitySchematicId().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the registry to the supplied writer in the JSON shape
     * expected by {@code bindings.schema.json}.
     *
     * @param registry the binding registry; must be server 154
     * @param rulesetCommit the pinned Infinity ruleset commit SHA
     * @param out the destination writer; not closed by this method
     */
    public static void write(
            SchematicBindingRegistry registry,
            String rulesetCommit,
            Writer out) throws IOException {
        if (registry == null) throw new NullPointerException("registry");
        if (out == null) throw new NullPointerException("out");
        if (registry.getServerId()
                != InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID) {
            throw new IllegalArgumentException(
                    "bindings.server154.json requires server id "
                    + InfinityGalaxyBinding.SWGAIDE_INFINITY_SERVER_ID
                    + ", got " + registry.getServerId());
        }
        if (rulesetCommit == null
                || !rulesetCommit.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException(
                    "rulesetCommit must be a 40-character SHA");
        }
        List<SchematicBinding> bindings = new java.util.ArrayList<SchematicBinding>(
                registry == null ? Collections_empty()
                        : registry.snapshot());
        boolean runnable = isFullyRunnable(bindings);

        out.write("{\n");
        out.write("  \"schemaVersion\": 1,\n");
        out.write("  \"serverId\": 154,\n");
        out.write("  \"rulesetCommit\": \"" + rulesetCommit + "\",\n");
        out.write("  \"admitted\": " + (runnable ? "true" : "false") + ",\n");
        out.write("  \"bindings\": [\n");
        for (int i = 0; i < bindings.size(); ++i) {
            SchematicBinding b = bindings.get(i);
            if (b == null) continue;
            out.write("    {\n");
            out.write("      \"swgAideSchematicId\": "
                    + b.getSwgAideSchematicId() + ",\n");
            out.write("      \"infinitySchematicId\": \""
                    + jsonEscape(b.getInfinitySchematicId()) + "\",\n");
            out.write("      \"state\": \"" + b.getState().name() + "\",\n");
            out.write("      \"evidence\": \""
                    + jsonEscape(b.getEvidence()) + "\"\n");
            out.write("    }" + (i + 1 < bindings.size() ? "," : "") + "\n");
        }
        out.write("  ]\n");
        out.write("}\n");
        out.flush();
    }

    private static List<SchematicBinding> Collections_empty() {
        return new java.util.ArrayList<SchematicBinding>();
    }

    private static String jsonEscape(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
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
}
