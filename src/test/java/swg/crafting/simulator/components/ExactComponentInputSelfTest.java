package swg.crafting.simulator.components;

import java.util.List;

/** Dependency-free exact exotic component parsing tests. */
public final class ExactComponentInputSelfTest {
    private ExactComponentInputSelfTest() {
    }

    public static void main(String[] args) {
        List<ComponentProperty> properties =
                ExactComponentPropertyParser.parse(
                        "min damage=43, maxDamage=171, attack speed=-0.8");

        if (properties.size() != 3) {
            throw new AssertionError("expected three exact properties");
        }
        if (!"mindamage".equals(properties.get(0).getAttribute())
                || properties.get(0).getValue() != 43.0d) {
            throw new AssertionError("min damage parse mismatch");
        }
        if (!"maxdamage".equals(properties.get(1).getAttribute())
                || properties.get(1).getValue() != 171.0d) {
            throw new AssertionError("max damage parse mismatch");
        }
        if (!"attackspeed".equals(properties.get(2).getAttribute())
                || Math.abs(properties.get(2).getValue() + 0.8d) > 0.000001d
                || properties.get(2).getPrecision() != 1) {
            throw new AssertionError("attack speed parse mismatch");
        }

        ExactComponentInput input = new ExactComponentInput(
                5, "Rare Gorax Bone Shards", "(SERIAL)", 5, properties);
        if (input.getSlotIndex() != 5
                || input.getAvailableUses() != 5
                || input.getProperties().size() != 3) {
            throw new AssertionError("exact component input mismatch");
        }

        System.out.println("ExactComponentInputSelfTest PASS");
    }
}
