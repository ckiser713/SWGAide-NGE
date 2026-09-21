package swg.infinity.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import swg.infinity.engine.UnsupportedInfinityRuleException;

/** Explicit processor registry; unknown processors fail closed. */
public final class ProcessorRegistry {
    private final Map<String, ResultProcessor> processors =
            new LinkedHashMap<String, ResultProcessor>();

    public ProcessorRegistry() {
        register(new GenericResultProcessor());
        register(new WeaponResultProcessor(false));
        register(new WeaponResultProcessor(true));
    }

    public void register(ResultProcessor processor) {
        if (processor == null) throw new NullPointerException("processor");
        if (processors.put(processor.getId(), processor) != null) {
            throw new IllegalArgumentException(
                    "Duplicate processor id: " + processor.getId());
        }
    }

    public ResultProcessor require(String processorId) {
        ResultProcessor processor = processors.get(processorId);
        if (processor == null) {
            throw new UnsupportedInfinityRuleException(
                    "No accepted final-item processor: " + processorId);
        }
        return processor;
    }
}
