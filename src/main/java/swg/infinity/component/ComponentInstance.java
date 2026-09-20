package swg.infinity.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable exact component instance or hypothetical component candidate.
 *
 * <p>Property order is preserved because the current Infinity generic
 * component application path has order-sensitive modified-state behavior.</p>
 */
public final class ComponentInstance {
    private final String id;
    private final String templateId;
    private final String serial;
    private final int uses;
    private final ComponentOrigin origin;
    private final List<ComponentProperty> properties;

    public ComponentInstance(
            String id,
            String templateId,
            String serial,
            int uses,
            ComponentOrigin origin,
            List<ComponentProperty> properties) {
        this.id = requireText(id, "id");
        this.templateId = requireText(templateId, "templateId");
        this.serial = serial == null ? "" : serial;
        if (uses < 1) throw new IllegalArgumentException("uses must be >= 1");
        if (origin == null) throw new NullPointerException("origin");
        if (properties == null) throw new NullPointerException("properties");
        this.uses = uses;
        this.origin = origin;
        this.properties = Collections.unmodifiableList(
                new ArrayList<ComponentProperty>(properties));
    }

    public String getId() { return id; }
    public String getTemplateId() { return templateId; }
    public String getSerial() { return serial; }
    public int getUses() { return uses; }
    public ComponentOrigin getOrigin() { return origin; }
    public List<ComponentProperty> getProperties() { return properties; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
