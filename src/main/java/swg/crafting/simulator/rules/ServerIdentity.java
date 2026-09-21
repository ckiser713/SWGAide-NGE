package swg.crafting.simulator.rules;

/**
 * Immutable SWGAide server identity used to select a crafting rules module.
 *
 * <p>Family values such as "precu" or "nge" are descriptive selectors only.
 * They are never proof that two servers share identical crafting behavior.</p>
 */
public final class ServerIdentity {
    private final int serverId;
    private final String name;
    private final String family;
    private final boolean customSchematics;

    public ServerIdentity(
            int serverId,
            String name,
            String family,
            boolean customSchematics) {
        if (serverId <= 0) {
            throw new IllegalArgumentException("serverId must be > 0");
        }
        this.serverId = serverId;
        this.name = requireText(name, "name");
        this.family = requireText(family, "family");
        this.customSchematics = customSchematics;
    }

    public int getServerId() { return serverId; }
    public String getName() { return name; }
    public String getFamily() { return family; }
    public boolean hasCustomSchematics() { return customSchematics; }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    @Override
    public String toString() {
        return name + "[" + serverId + "," + family
                + (customSchematics ? ",custom" : "") + "]";
    }
}
