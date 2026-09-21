package swg.crafting.simulator.components;

/** Number of uses consumed from one component instance in a schematic slot. */
public final class ComponentUse {
    private final ComponentInstance component;
    private final int uses;

    public ComponentUse(ComponentInstance component, int uses) {
        if (component == null) throw new NullPointerException("component");
        if (uses < 1 || uses > component.getUses()) {
            throw new IllegalArgumentException(
                    "uses must be within [1," + component.getUses() + "]");
        }
        this.component = component;
        this.uses = uses;
    }

    public ComponentInstance getComponent() { return component; }
    public int getUses() { return uses; }
}
