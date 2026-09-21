package swg.crafting.simulator.scenario;
import swg.infinity.engine.CraftState;

import swg.infinity.processor.FunctionalItemResult;

/** Complete deterministic craft result: intermediate state plus functional item. */
public final class CraftResult {
    private final CraftState craftState;
    private final FunctionalItemResult functionalResult;

    public CraftResult(
            CraftState craftState,
            FunctionalItemResult functionalResult) {
        if (craftState == null) throw new NullPointerException("craftState");
        if (functionalResult == null) throw new NullPointerException("functionalResult");
        this.craftState = craftState;
        this.functionalResult = functionalResult;
    }

    public CraftState getCraftState() { return craftState; }
    public FunctionalItemResult getFunctionalResult() { return functionalResult; }
}
