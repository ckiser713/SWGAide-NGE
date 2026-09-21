package swg.infinity.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import swg.crafting.simulator.scenario.AttributeState;
import swg.infinity.engine.CraftState;

/** Generic diagnostic processor exposing current crafting attributes verbatim. */
public final class GenericResultProcessor implements ResultProcessor {
    public static final String ID = "generic";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public FunctionalItemResult process(CraftState state) {
        if (state == null) throw new NullPointerException("state");
        Map<String, Double> values = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, AttributeState> entry :
                state.getAttributes().entrySet()) {
            values.put(
                    entry.getKey(),
                    Double.valueOf(entry.getValue().getCurrentValue()));
        }
        return new FunctionalItemResult(
                ID, state.getEvidenceState(), values, state.getWarnings());
    }
}
