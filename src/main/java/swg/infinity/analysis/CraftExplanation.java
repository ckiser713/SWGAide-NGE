package swg.infinity.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ordered explainability output for a completed deterministic craft. */
public final class CraftExplanation {
    private final List<AttributeExplanation> attributes;

    public CraftExplanation(List<AttributeExplanation> attributes) {
        if (attributes == null) throw new NullPointerException("attributes");
        this.attributes = Collections.unmodifiableList(
                new ArrayList<AttributeExplanation>(attributes));
    }

    public List<AttributeExplanation> getAttributes() {
        return attributes;
    }

    public AttributeExplanation get(String name) {
        for (AttributeExplanation attribute : attributes) {
            if (attribute.getAttribute().equals(name)) return attribute;
        }
        return null;
    }
}
