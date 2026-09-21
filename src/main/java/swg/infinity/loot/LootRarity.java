package swg.infinity.loot;

/**
 * Rarity classification for hypothetical Infinity loot modeling.
 *
 * <p>This enum does not itself assign multipliers because the authoritative
 * configuration belongs in an extracted LootRulesDefinition.</p>
 */
public enum LootRarity {
    NORMAL,
    YELLOW,
    EXCEPTIONAL,
    LEGENDARY
}
