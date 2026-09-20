package swg.infinity.contracts;

/** Ingredient slot semantics normalized from Infinity draft schematic data. */
public enum SlotKind {
    RESOURCE,
    IDENTICAL_COMPONENT,
    MIXED_COMPONENT,
    OPTIONAL_IDENTICAL_COMPONENT,
    OPTIONAL_MIXED_COMPONENT;

    public boolean isOptional() {
        return this == OPTIONAL_IDENTICAL_COMPONENT
                || this == OPTIONAL_MIXED_COMPONENT;
    }

    public boolean isComponent() {
        return this != RESOURCE;
    }

    public boolean requiresIdenticalComponents() {
        return this == IDENTICAL_COMPONENT
                || this == OPTIONAL_IDENTICAL_COMPONENT;
    }
}
