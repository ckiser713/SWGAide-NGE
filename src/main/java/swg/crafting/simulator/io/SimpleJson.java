package swg.crafting.simulator.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal dependency-free JSON codec for simulator artifacts.
 *
 * <p>Supports objects, arrays, strings, numbers, booleans and null. Parsing is
 * fail-closed on trailing content or malformed escapes. Maps preserve insertion
 * order for deterministic artifact generation.</p>
 */
public final class SimpleJson {
    private SimpleJson() {
        throw new AssertionError("Do not instantiate");
    }

    public static Object parse(InputStream in) throws IOException {
        if (in == null) throw new NullPointerException("in");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            text.append(line).append('\n');
        }
        return parse(text.toString());
    }

    public static Object parse(String text) {
        if (text == null) throw new NullPointerException("text");
        Parser parser = new Parser(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw new IllegalArgumentException(
                    "trailing JSON content at " + parser.position);
        }
        return value;
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 0);
        out.append('\n');
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object value, StringBuilder out, int depth) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            quote((String) value, out);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value.toString());
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            out.append('{');
            if (!map.isEmpty()) out.append('\n');
            int index = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                indent(out, depth + 1);
                quote(entry.getKey(), out);
                out.append(": ");
                write(entry.getValue(), out, depth + 1);
                if (++index < map.size()) out.append(',');
                out.append('\n');
            }
            if (!map.isEmpty()) indent(out, depth);
            out.append('}');
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            out.append('[');
            if (!list.isEmpty()) out.append('\n');
            for (int i = 0; i < list.size(); ++i) {
                indent(out, depth + 1);
                write(list.get(i), out, depth + 1);
                if (i + 1 < list.size()) out.append(',');
                out.append('\n');
            }
            if (!list.isEmpty()) indent(out, depth);
            out.append(']');
        } else {
            throw new IllegalArgumentException(
                    "unsupported JSON value type: " + value.getClass());
        }
    }

    private static void quote(String value, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
            case '"': out.append("\\\""); break;
            case '\\': out.append("\\\\"); break;
            case '\b': out.append("\\b"); break;
            case '\f': out.append("\\f"); break;
            case '\n': out.append("\\n"); break;
            case '\r': out.append("\\r"); break;
            case '\t': out.append("\\t"); break;
            default:
                if (c < 0x20) {
                    out.append(String.format("\\u%04x", (int) c));
                } else {
                    out.append(c);
                }
            }
        }
        out.append('"');
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth; ++i) out.append("  ");
    }

    private static final class Parser {
        private final String source;
        private int position;

        Parser(String source) {
            this.source = source;
        }

        boolean atEnd() {
            return position >= source.length();
        }

        void skipWhitespace() {
            while (!atEnd()
                    && Character.isWhitespace(source.charAt(position))) {
                ++position;
            }
        }

        Object readValue() {
            skipWhitespace();
            if (atEnd()) throw fail("unexpected end of JSON");
            char c = source.charAt(position);
            if (c == '{') return readObject();
            if (c == '[') return readArray();
            if (c == '"') return readString();
            if (c == 't') return literal("true", Boolean.TRUE);
            if (c == 'f') return literal("false", Boolean.FALSE);
            if (c == 'n') return literal("null", null);
            return readNumber();
        }

        private Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> out = new LinkedHashMap<String, Object>();
            skipWhitespace();
            if (peek('}')) {
                ++position;
                return out;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                out.put(key, readValue());
                skipWhitespace();
                if (peek(',')) {
                    ++position;
                    continue;
                }
                expect('}');
                return out;
            }
        }

        private List<Object> readArray() {
            expect('[');
            List<Object> out = new ArrayList<Object>();
            skipWhitespace();
            if (peek(']')) {
                ++position;
                return out;
            }
            while (true) {
                out.add(readValue());
                skipWhitespace();
                if (peek(',')) {
                    ++position;
                    continue;
                }
                expect(']');
                return out;
            }
        }

        private String readString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (!atEnd()) {
                char c = source.charAt(position++);
                if (c == '"') return out.toString();
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (atEnd()) throw fail("unterminated escape");
                char escaped = source.charAt(position++);
                switch (escaped) {
                case '"': out.append('"'); break;
                case '\\': out.append('\\'); break;
                case '/': out.append('/'); break;
                case 'b': out.append('\b'); break;
                case 'f': out.append('\f'); break;
                case 'n': out.append('\n'); break;
                case 'r': out.append('\r'); break;
                case 't': out.append('\t'); break;
                case 'u':
                    if (position + 4 > source.length()) {
                        throw fail("short unicode escape");
                    }
                    String hex = source.substring(position, position + 4);
                    position += 4;
                    out.append((char) Integer.parseInt(hex, 16));
                    break;
                default:
                    throw fail("unknown escape \\" + escaped);
                }
            }
            throw fail("unterminated string");
        }

        private Object readNumber() {
            int start = position;
            if (peek('-')) ++position;
            while (!atEnd() && Character.isDigit(source.charAt(position))) {
                ++position;
            }
            if (!atEnd() && source.charAt(position) == '.') {
                ++position;
                while (!atEnd()
                        && Character.isDigit(source.charAt(position))) {
                    ++position;
                }
            }
            if (!atEnd()
                    && (source.charAt(position) == 'e'
                        || source.charAt(position) == 'E')) {
                ++position;
                if (!atEnd()
                        && (source.charAt(position) == '+'
                            || source.charAt(position) == '-')) {
                    ++position;
                }
                while (!atEnd()
                        && Character.isDigit(source.charAt(position))) {
                    ++position;
                }
            }
            if (start == position) throw fail("expected value");
            String token = source.substring(start, position);
            if (token.indexOf('.') < 0
                    && token.indexOf('e') < 0
                    && token.indexOf('E') < 0) {
                try {
                    return Long.valueOf(token);
                } catch (NumberFormatException ignored) {
                }
            }
            return Double.valueOf(token);
        }

        private Object literal(String token, Object value) {
            if (!source.startsWith(token, position)) {
                throw fail("expected " + token);
            }
            position += token.length();
            return value;
        }

        private boolean peek(char expected) {
            return !atEnd() && source.charAt(position) == expected;
        }

        private void expect(char expected) {
            if (!peek(expected)) {
                throw fail("expected '" + expected + "'");
            }
            ++position;
        }

        private IllegalArgumentException fail(String message) {
            return new IllegalArgumentException(
                    message + " at JSON position " + position);
        }
    }
}
