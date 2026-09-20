package swg.infinity.loot;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import swg.infinity.contracts.Provenance;

/**
 * Extracted loot-generation configuration. This is data only; the complex
 * generation algorithm remains unsupported until parity fixtures exist.
 */
public final class LootRulesDefinition {
    private final int minimumLevel;
    private final int maximumLevel;
    private final Map<LootRarity, Double> rarityModifiers;
    private final Provenance provenance;

    public LootRulesDefinition(
            int minimumLevel,
            int maximumLevel,
            Map<LootRarity, Double> rarityModifiers,
            Provenance provenance) {
        if (minimumLevel < 1 || maximumLevel < minimumLevel) {
            throw new IllegalArgumentException("invalid loot level range");
        }
        if (rarityModifiers == null) throw new NullPointerException("rarityModifiers");
        if (provenance == null) throw new NullPointerException("provenance");
        EnumMap<LootRarity, Double> copied =
                new EnumMap<LootRarity, Double>(LootRarity.class);
        for (Map.Entry<LootRarity, Double> entry : rarityModifiers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new NullPointerException("rarity modifier");
            }
            double modifier = entry.getValue().doubleValue();
            if (Double.isNaN(modifier)
                    || Double.isInfinite(modifier)
                    || modifier < 0.0d) {
                throw new IllegalArgumentException("invalid rarity modifier");
            }
            copied.put(entry.getKey(), Double.valueOf(modifier));
        }
        this.minimumLevel = minimumLevel;
        this.maximumLevel = maximumLevel;
        this.rarityModifiers = Collections.unmodifiableMap(copied);
        this.provenance = provenance;
    }

    public int getMinimumLevel() { return minimumLevel; }
    public int getMaximumLevel() { return maximumLevel; }
    public Map<LootRarity, Double> getRarityModifiers() { return rarityModifiers; }
    public Provenance getProvenance() { return provenance; }
}
