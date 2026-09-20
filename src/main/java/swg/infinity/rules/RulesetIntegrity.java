package swg.infinity.rules;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** JDK-only SHA-256 helpers for immutable normalized ruleset artifacts. */
public final class RulesetIntegrity {
    private RulesetIntegrity() {
        throw new AssertionError("Do not instantiate");
    }

    public static String sha256(byte[] bytes) {
        if (bytes == null) throw new NullPointerException("bytes");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                out.append(String.format("%02x", value & 0xff));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("JDK SHA-256 unavailable", impossible);
        }
    }

    public static String sha256(String value) {
        if (value == null) throw new NullPointerException("value");
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    public static void requireHash(byte[] bytes, String expectedHex) {
        if (expectedHex == null || !expectedHex.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException(
                    "expectedHex must be a 64-character SHA-256 hex digest");
        }
        String actual = sha256(bytes);
        if (!actual.equalsIgnoreCase(expectedHex)) {
            throw new IllegalStateException(
                    "Ruleset artifact SHA-256 mismatch: expected="
                    + expectedHex.toLowerCase()
                    + " actual=" + actual);
        }
    }
}
