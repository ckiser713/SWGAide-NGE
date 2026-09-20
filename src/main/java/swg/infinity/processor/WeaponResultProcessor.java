package swg.infinity.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swg.infinity.engine.AttributeState;
import swg.infinity.engine.CraftState;

/**
 * Final weapon mapping from pinned WeaponObjectImplementation::updateCraftingValues.
 */
public final class WeaponResultProcessor implements ResultProcessor {
    public static final String ID = "weapon";
    public static final String JEDI_ID = "jedi_weapon";

    private final boolean jediWeapon;

    public WeaponResultProcessor() {
        this(false);
    }

    public WeaponResultProcessor(boolean jediWeapon) {
        this.jediWeapon = jediWeapon;
    }

    @Override
    public String getId() {
        return jediWeapon ? JEDI_ID : ID;
    }

    @Override
    public FunctionalItemResult process(CraftState state) {
        if (state == null) throw new NullPointerException("state");

        Map<String, Double> values = new LinkedHashMap<String, Double>();
        List<String> warnings = new ArrayList<String>(state.getWarnings());

        values.put("mindamage", Double.valueOf(
                Math.max(required(state, "mindamage"), 0.0d)));
        values.put("maxdamage", Double.valueOf(
                Math.max(required(state, "maxdamage"), 0.0d)));
        values.put("attackspeed", Double.valueOf(required(state, "attackspeed")));
        values.put("attackhealthcost", Double.valueOf(
                (int) required(state, "attackhealthcost")));
        values.put("attackactioncost", Double.valueOf(
                (int) required(state, "attackactioncost")));
        values.put("attackmindcost", Double.valueOf(
                (int) required(state, "attackmindcost")));

        copyOptional(state, values, "woundchance", false);
        copyOptional(state, values, "zerorangemod", true);
        copyOptional(state, values, "maxrange", true);
        copyOptional(state, values, "maxrangemod", true);
        copyOptional(state, values, "midrange", true);
        copyOptional(state, values, "midrangemod", true);
        copyOptional(state, values, "hitpoints", true);

        if (jediWeapon) {
            double forceCost = required(state, "forcecost");
            values.put("forcecost", Double.valueOf(round(forceCost, 1)));
            values.put("bladecolor", Double.valueOf(31.0d));
        }

        values.put("conditiondamage", Double.valueOf(0.0d));
        return new FunctionalItemResult(
                getId(), state.getEvidenceState(), values, warnings);
    }

    private double required(CraftState state, String name) {
        AttributeState value = state.getAttribute(name);
        if (value == null) {
            throw new IllegalStateException(
                    "Weapon processor missing required crafting attribute: " + name);
        }
        return value.getCurrentValue();
    }

    private void copyOptional(
            CraftState state,
            Map<String, Double> result,
            String name,
            boolean integerCast) {
        AttributeState value = state.getAttribute(name);
        if (value == null) return;
        double current = value.getCurrentValue();
        result.put(name, Double.valueOf(integerCast ? (int) current : current));
    }

    private double round(double value, int precision) {
        double scale = Math.pow(10.0d, precision);
        return Math.round(value * scale) / scale;
    }
}
