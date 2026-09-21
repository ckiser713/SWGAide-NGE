package swg.crafting.simulator.scenario;

/**
 * Generic linear interpolation across a min/max range, parameterized by a
 * property group identifier. Extracted from the Infinity resource-labratory
 * internals so generic scenario DTOs (e.g. {@link AttributeState}) can compute
 * interpolated values without importing Infinity-specific classes.
 *
 * <p>This is provider-neutral math; the Infinity engine continues to call this
 * utility through {@code ResourceLaboratory.interpolate}, so the behaviour
 * observed by Infinity code is unchanged.
 */
public final class Interpolation {

    private Interpolation() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Interpolates a value within a {@code [min, max]} range using a percentage
     * in {@code [0.0, 1.0]}. The {@code group} argument is preserved as an
     * Infinity-style property-group tag: when present and non-empty, the value
     * is interpolated from {@code min} to {@code max}; when absent or empty,
     * the larger of {@code min}/{@code max} is returned.
     *
     * <p>If {@code max < min}, the interpolation is inverted: percentage 0.0
     * yields {@code max}, percentage 1.0 yields {@code min}. This matches the
     * SWG behaviour for inverted attributes such as attack speed.
     *
     * @param group property group tag; may be {@code null} or empty
     * @param min   range minimum (must be finite)
     * @param max   range maximum (must be finite)
     * @param percentage interpolation percentage (must be finite and {@code >= 0})
     * @return the interpolated value
     */
    public static double interpolate(
            String group, double min, double max, double percentage) {
        requireFiniteNonNegative(percentage, "percentage");
        if (group == null || group.isEmpty()) {
            return max > min ? max : min;
        }
        if (max > min) {
            return percentage * (max - min) + min;
        }
        if (max < min) {
            return (1.0d - percentage) * (min - max) + max;
        }
        return max;
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        if (value < 0.0d) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
    }
}
