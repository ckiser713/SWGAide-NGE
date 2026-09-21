package swg.infinity.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-rolled, token-aware Lua template reader. Reads the narrow subset of
 * Lua used by SWG Infinity weapon draft schematics:
 *
 * <ul>
 *   <li>table literals: {@code { key = value, key2 = { ... } }}</li>
 *   <li>string literals (double-quoted)</li>
 *   <li>numbers (integer and decimal, signed)</li>
 *   <li>identifiers (treated as strings: {@code DRAFTSCHEMATIC} -&gt; {@code "DRAFTSCHEMATIC"})</li>
 *   <li>{@code --} line comments and {@code --[[ ... ]]} block comments</li>
 * </ul>
 *
 * <p>Any unhandled construct raises {@link LuaSyntaxException} and refuses
 * to produce partial output. This fail-closed behaviour is required by
 * the plan: the extractor never silently drops source constructs.</p>
 *
 * <p>Output is deterministic: the parser preserves insertion order via
 * {@link LinkedHashMap}; no random, locale, or time-dependent behaviour
 * affects the parsed representation.</p>
 */
public final class LuaTemplateStubLoader {

    private final String text;
    private int position;

    private LuaTemplateStubLoader(String text) {
        this.text = text;
        this.position = 0;
    }

    /** Reads the file at {@code path} and parses the first table literal. */
    public static Object loadFirstTable(File path) throws IOException {
        if (path == null) throw new NullPointerException("path");
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            r.close();
        }
        return new LuaTemplateStubLoader(sb.toString()).parseFirstTable();
    }

    /** Reads the file at {@code path} and parses the i-th table literal. */
    public static Object loadTableAt(File path, int occurrence)
            throws IOException {
        if (path == null) throw new NullPointerException("path");
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            r.close();
        }
        LuaTemplateStubLoader loader = new LuaTemplateStubLoader(sb.toString());
        Object value = null;
        for (int i = 0; i <= occurrence; ++i) {
            value = loader.parseFirstTable();
            loader.skipWhitespaceAndComments();
        }
        return value;
    }

    /**
     * Scans ahead to the first '{' at the top statement level (skipping
     * identifier prefixes and '=' assignment), then parses that table.
     */
    private Object parseFirstTable() {
        skipWhitespaceAndComments();
        // The schematic files have the form:
        //   identifier = identifier:new { ... }
        // Walk through identifier [':' identifier]* 'new' '{'
        while (position < text.length()) {
            char c = text.charAt(position);
            if (c == '{') return parseTable();
            // Skip an identifier or punctuation until we hit '{'.
            // Stop at end of input or unhandled construct.
            if (isIdentStart(c)) {
                parseIdentifier();
            } else if (c == '=' || c == ':' || c == ',') {
                position++;
            } else if (c == '"') {
                parseString();
            } else if (c == '-' || (c >= '0' && c <= '9')) {
                parseNumber();
            } else {
                throw new LuaSyntaxException(
                        "Unexpected character '" + c + "' at " + position
                        + " while scanning for table literal");
            }
            skipWhitespaceAndComments();
        }
        throw new LuaSyntaxException("No table literal found in input");
    }

    private Object parseValue() {
        skipWhitespaceAndComments();
        if (position >= text.length()) {
            throw new LuaSyntaxException("Unexpected end of input");
        }
        char c = text.charAt(position);
        if (c == '{') return parseTable();
        if (c == '"') return parseString();
        if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
        return parseIdentifier();
    }

    private Object parseTable() {
        expect('{');
        skipWhitespaceAndComments();
        if (peek() == '}') {
            position++;
            return new LinkedHashMap<String, Object>();
        }
        // Determine if this is an array (sequential values) or a record
        // (key = value form). Lua tables are unified; arrays are emitted
        // as ArrayList, records as LinkedHashMap.
        boolean isArray = detectArrayStyle();
        if (isArray) {
            return parseArrayTable();
        }
        return parseRecordTable();
    }

    private boolean detectArrayStyle() {
        // Save position, scan ahead until we hit a closing brace or end, or
        // a "key =" form. Restore position.
        int save = position;
        boolean sawEquals = false;
        boolean sawValue = false;
        while (position < text.length()) {
            skipWhitespaceAndComments();
            if (position >= text.length()) break;
            char c = text.charAt(position);
            if (c == '}') break;
            // Try to identify identifier "=" pattern.
            int idStart = position;
            if (isIdentStart(c)) {
                while (position < text.length()
                        && isIdentPart(text.charAt(position))) {
                    position++;
                }
                String ident = text.substring(idStart, position);
                skipWhitespaceAndComments();
                if (position < text.length() && text.charAt(position) == '='
                        && !ident.isEmpty()) {
                    sawEquals = true;
                    break;
                }
                // Not a "key =" form: this is an array element. Skip the value.
                position = idStart;
                sawValue = true;
                try {
                    parseValue();
                } catch (RuntimeException e) {
                    position = save;
                    return !sawEquals;
                }
                skipWhitespaceAndComments();
                if (position < text.length()
                        && text.charAt(position) == ',') {
                    position++;
                }
            } else {
                // Non-identifier start: skip one value, advance.
                sawValue = true;
                try {
                    parseValue();
                } catch (RuntimeException e) {
                    break;
                }
                skipWhitespaceAndComments();
                if (position < text.length()
                        && text.charAt(position) == ',') {
                    position++;
                }
            }
        }
        position = save;
        return !sawEquals && sawValue;
    }

    private Map<String, Object> parseRecordTable() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        while (true) {
            skipWhitespaceAndComments();
            if (peek() == '}') {
                position++;
                return map;
            }
            String key = parseIdentifier();
            skipWhitespaceAndComments();
            expect('=');
            Object value = parseValue();
            map.put(key, value);
            skipWhitespaceAndComments();
            char c = peek();
            if (c == ',') {
                position++;
                continue;
            }
            if (c == '}') {
                position++;
                return map;
            }
            throw new LuaSyntaxException(
                    "expected ',' or '}' at " + position);
        }
    }

    private List<Object> parseArrayTable() {
        List<Object> list = new ArrayList<Object>();
        while (true) {
            skipWhitespaceAndComments();
            if (peek() == '}') {
                position++;
                return list;
            }
            Object value = parseValue();
            list.add(value);
            skipWhitespaceAndComments();
            char c = peek();
            if (c == ',') {
                position++;
                continue;
            }
            if (c == '}') {
                position++;
                return list;
            }
            throw new LuaSyntaxException(
                    "expected ',' or '}' at " + position);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (position >= text.length()) {
                    throw new LuaSyntaxException("Bad escape");
                }
                char esc = text.charAt(position++);
                switch (esc) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                default: sb.append(esc);
                }
                continue;
            }
            sb.append(c);
        }
        throw new LuaSyntaxException("Unterminated string");
    }

    private Number parseNumber() {
        int start = position;
        if (peek() == '-') position++;
        while (position < text.length()
                && "0123456789.".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
        String t = text.substring(start, position);
        if (t.isEmpty() || "-".equals(t)) {
            throw new LuaSyntaxException(
                    "Invalid number at position " + start);
        }
        if (t.indexOf('.') < 0) {
            try {
                return Long.valueOf(t);
            } catch (NumberFormatException ignored) {}
        }
        return Double.valueOf(t);
    }

    private String parseIdentifier() {
        int start = position;
        if (position >= text.length()
                || !isIdentStart(text.charAt(position))) {
            throw new LuaSyntaxException(
                    "Expected identifier at position " + position);
        }
        while (position < text.length()
                && isIdentPart(text.charAt(position))) {
            position++;
        }
        return text.substring(start, position);
    }

    private void expect(char c) {
        if (position >= text.length() || text.charAt(position) != c) {
            throw new LuaSyntaxException(
                    "Expected '" + c + "' at position " + position);
        }
        position++;
    }

    private char peek() {
        if (position >= text.length()) {
            throw new LuaSyntaxException(
                    "Unexpected end at position " + position);
        }
        return text.charAt(position);
    }

    private void skipWhitespaceAndComments() {
        while (position < text.length()) {
            char c = text.charAt(position);
            if (Character.isWhitespace(c)) {
                position++;
                continue;
            }
            if (c == '-' && position + 1 < text.length()
                    && text.charAt(position + 1) == '-') {
                // line comment until end of line
                position += 2;
                if (position + 1 < text.length()
                        && text.charAt(position) == '['
                        && text.charAt(position + 1) == '[') {
                    // block comment
                    position += 2;
                    int end = text.indexOf("]]", position);
                    if (end < 0) {
                        throw new LuaSyntaxException(
                                "Unterminated block comment");
                    }
                    position = end + 2;
                } else {
                    while (position < text.length()
                            && text.charAt(position) != '\n') {
                        position++;
                    }
                }
                continue;
            }
            break;
        }
    }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /** Thrown when the loader encounters an unhandled Lua construct. */
    public static final class LuaSyntaxException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LuaSyntaxException(String message) {
            super(message);
        }
    }
}
