package swg.infinity.processor;

import swg.infinity.engine.CraftState;

/** Converts generic CraftingValues state into functional item statistics. */
public interface ResultProcessor {
    String getId();
    FunctionalItemResult process(CraftState state);
}
