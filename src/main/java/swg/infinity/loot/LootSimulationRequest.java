package swg.infinity.loot;

/** Validated inputs for a future source-parity Infinity loot simulator. */
public final class LootSimulationRequest {
    private final LootTemplateDefinition template;
    private final LootRulesDefinition rules;
    private final int level;
    private final LootRarity rarity;

    public LootSimulationRequest(
            LootTemplateDefinition template,
            LootRulesDefinition rules,
            int level,
            LootRarity rarity) {
        if (template == null) throw new NullPointerException("template");
        if (rules == null) throw new NullPointerException("rules");
        if (rarity == null) throw new NullPointerException("rarity");
        if (level < rules.getMinimumLevel()
                || level > rules.getMaximumLevel()) {
            throw new IllegalArgumentException(
                    "level must be within ["
                    + rules.getMinimumLevel() + ","
                    + rules.getMaximumLevel() + "]");
        }
        this.template = template;
        this.rules = rules;
        this.level = level;
        this.rarity = rarity;
    }

    public LootTemplateDefinition getTemplate() { return template; }
    public LootRulesDefinition getRules() { return rules; }
    public int getLevel() { return level; }
    public LootRarity getRarity() { return rarity; }
}
