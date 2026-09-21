package swg.infinity.rules;

import swg.infinity.ruleset.RulesetIntegrity;

/** Dependency-free ruleset hash utility tests. */
public final class RulesetIntegritySelfTest {
    private RulesetIntegritySelfTest() {
    }

    public static void main(String[] args) {
        String digest = RulesetIntegrity.sha256("abc");
        String expected =
                "ba7816bf8f01cfea414140de5dae2223"
                + "b00361a396177a9cb410ff61f20015ad";
        if (!expected.equals(digest)) {
            throw new AssertionError(
                    "SHA-256 mismatch: " + digest);
        }

        RulesetIntegrity.requireHash("abc".getBytes(), expected);

        boolean rejected = false;
        try {
            RulesetIntegrity.requireHash(
                    "abd".getBytes(), expected);
        } catch (IllegalStateException expectedFailure) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("hash mismatch accepted");
        System.out.println("RulesetIntegritySelfTest PASS");
    }
}
