package swg.infinity.integration;

import java.util.Arrays;
import java.util.Collections;

/** Dependency-free contract tests for binding registry construction. */
public final class SchematicBindingRegistrySelfTest {
    private SchematicBindingRegistrySelfTest() {
    }

    public static void main(String[] args) {
        shouldAcceptVerifiedBinding();
        shouldRejectDuplicateBinding();
        shouldRejectRunnableBindingWithoutInfinityId();
        System.out.println("SchematicBindingRegistrySelfTest PASS");
    }

    private static void shouldAcceptVerifiedBinding() {
        SchematicBinding binding = new SchematicBinding(
                154, 100, "object/draft_schematic/example.iff",
                BindingState.VERIFIED, "fixture");
        SchematicBindingRegistry registry =
                new SchematicBindingRegistry(154, Collections.singletonList(binding));
        if (registry.get(100) != binding) {
            throw new AssertionError("verified binding not indexed");
        }
    }

    private static void shouldRejectDuplicateBinding() {
        boolean rejected = false;
        try {
            new SchematicBindingRegistry(
                    154,
                    Arrays.asList(
                            new SchematicBinding(
                                    154, 100, "a", BindingState.VERIFIED, ""),
                            new SchematicBinding(
                                    154, 100, "b", BindingState.VERIFIED, "")));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) throw new AssertionError("duplicate binding accepted");
    }

    private static void shouldRejectRunnableBindingWithoutInfinityId() {
        boolean rejected = false;
        try {
            new SchematicBinding(
                    154, 100, "", BindingState.VERIFIED, "");
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new AssertionError(
                    "runnable binding without Infinity source id accepted");
        }
    }
}
