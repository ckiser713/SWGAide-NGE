package swg.infinity.integration;

import java.util.Arrays;

/** Dependency-free tests for real-ID live binding structural signatures. */
public final class LiveBindingSignatureSelfTest {
    private LiveBindingSignatureSelfTest() {
    }

    public static void main(String[] args) {
        LiveBindingSignature nativeSide = LiveBindingSignature.fixture(
                "DL44 Pistol",
                Arrays.asList("metal:20", "metal:8", "metal:4"),
                Arrays.asList(
                        "IDENTICAL_COMPONENT:1",
                        "IDENTICAL_COMPONENT:1",
                        "OPTIONAL_IDENTICAL_COMPONENT:1"));

        LiveBindingSignature infinitySide = LiveBindingSignature.fixture(
                "DL44 Pistol",
                Arrays.asList("metal:4", "metal:20", "metal:8"),
                Arrays.asList(
                        "OPTIONAL_IDENTICAL_COMPONENT:1",
                        "IDENTICAL_COMPONENT:1",
                        "IDENTICAL_COMPONENT:1"));

        if (!nativeSide.alignsWith(infinitySide)) {
            throw new AssertionError(
                    "equivalent live binding signatures did not align");
        }

        LiveBindingSignature wrongQuantity = LiveBindingSignature.fixture(
                "DL44 Pistol",
                Arrays.asList("metal:20", "metal:8", "metal:5"),
                infinitySide.getComponentSlots());
        if (nativeSide.alignsWith(wrongQuantity)) {
            throw new AssertionError(
                    "resource quantity mismatch must fail closed");
        }

        LiveBindingSignature wrongName = LiveBindingSignature.fixture(
                "Different Pistol",
                nativeSide.getResourceSlots(),
                nativeSide.getComponentSlots());
        if (nativeSide.alignsWith(wrongName)) {
            throw new AssertionError("name mismatch must fail closed");
        }

        System.out.println("LiveBindingSignatureSelfTest PASS");
    }
}
