package swg.crafting.simulator.components;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import swg.crafting.simulator.contracts.Provenance;

/**
 * Parses exact owned-component properties from comma/newline separated
 * {@code attribute=value} pairs.
 */
public final class ExactComponentPropertyParser {
    private static final Provenance MANUAL_PROVENANCE =
            new Provenance("manual", "observed", "crafting-simulator-ui");

    private ExactComponentPropertyParser() {
        throw new AssertionError("Do not instantiate");
    }

    public static List<ComponentProperty> parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] tokens = text.split("[,;\\n]+");
        List<ComponentProperty> out =
                new ArrayList<ComponentProperty>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            int eq = trimmed.indexOf('=');
            if (eq <= 0 || eq + 1 >= trimmed.length()) {
                throw new IllegalArgumentException(
                        "component property must be attribute=value: " + trimmed);
            }

            String attribute = normalizeAttribute(
                    trimmed.substring(0, eq));
            String number = trimmed.substring(eq + 1).trim();
            BigDecimal parsed;
            try {
                parsed = new BigDecimal(number);
            } catch (NumberFormatException badNumber) {
                throw new IllegalArgumentException(
                        "invalid component property value: " + trimmed,
                        badNumber);
            }

            int precision = Math.max(0, parsed.stripTrailingZeros().scale());
            out.add(new ComponentProperty(
                    attribute,
                    parsed.doubleValue(),
                    precision,
                    "",
                    false,
                    MANUAL_PROVENANCE));
        }
        return Collections.unmodifiableList(out);
    }

    private static String normalizeAttribute(String input) {
        String key = input == null
                ? "" : input.toLowerCase(Locale.ENGLISH).trim();
        key = key.replaceAll("[^a-z0-9]+", "");
        if ("mindamage".equals(key) || "minimumdamage".equals(key)) {
            return "mindamage";
        }
        if ("maxdamage".equals(key) || "maximumdamage".equals(key)) {
            return "maxdamage";
        }
        if ("attackspeed".equals(key) || "speed".equals(key)) {
            return "attackspeed";
        }
        if ("hitpoints".equals(key) || "condition".equals(key)) {
            return "hitpoints";
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "component property attribute is blank");
        }
        return key;
    }
}
