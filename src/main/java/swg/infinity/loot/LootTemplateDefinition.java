package swg.infinity.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import swg.infinity.contracts.Provenance;

/** Normalized source definition for one Infinity loot component template. */
public final class LootTemplateDefinition {
    private final String id;
    private final String directObjectTemplate;
    private final List<LootPropertyRange> properties;
    private final Provenance provenance;

    public LootTemplateDefinition(
            String id,
            String directObjectTemplate,
            List<LootPropertyRange> properties,
            Provenance provenance) {
        this.id = requireText(id, "id");
        this.directObjectTemplate =
                requireText(directObjectTemplate, "directObjectTemplate");
        if (properties == null) throw new NullPointerException("properties");
        if (provenance == null) throw new NullPointerException("provenance");
        this.properties = Collections.unmodifiableList(
                new ArrayList<LootPropertyRange>(properties));
        this.provenance = provenance;
    }

    public String getId() { return id; }
    public String getDirectObjectTemplate() { return directObjectTemplate; }
    public List<LootPropertyRange> getProperties() { return properties; }
    public Provenance getProvenance() { return provenance; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
