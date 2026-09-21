# Native Crafting Simulator Tab

## Placement

The UI is a new child tab of existing `SWGSchematicTab`.

Target label:

**Crafting Simulator**

Do not create:

- a standalone app;
- a second top-level SWGAide main tab;
- an Infinity-only dialog as the primary workflow.

## SWGSchematicTab integration

Current `SWGSchematicTab.make()` creates:

```java
draftSchems = new SWGDraftTab(this);
laboratory = new SWGLaboratoryTab(this);
todaysAlert = new SWGTodays(this);
resClassUse = new SWGResourceClassUse(this);
```

Target architecture adds:

```java
craftingSimulator = new SWGCraftingSimulatorTab(this);
```

and inserts:

```java
add("Crafting Simulator", craftingSimulator);
```

preferably immediately after The Laboratory.

## Selection synchronization

Current `SWGSchematicTab.schematicSelect(...)` synchronizes Draft Schematics and The
Laboratory.

Extend the same mechanism so Crafting Simulator receives the selected schematic.

Rules:

- no duplicate independent schematic selector state unless needed for filtering;
- selected schematic follows normal SWGAide navigation;
- unsupported/unbound schematic remains visible but displays explicit coverage status;
- do not alter legacy Test Bench selection semantics.

## Selected galaxy

The tab reads `SWGFrame.getSelectedGalaxy()`.

On focus/galaxy change:

1. snapshot selected server identity;
2. snapshot native server resources;
3. resolve `ServerRulesProvider`;
4. load server-specific bindings/rules;
5. update coverage/status banner;
6. preserve selected schematic where legal.

## Status banner

Always show:

```text
Server
Rules provider
Rules source/version
Resource scope
Coverage
```

Examples:

```text
Server: SWG Infinity
Rules: SWG Infinity / exact server
Rules SHA: 6b6ac372...
Resources: Current + Inventory
Coverage: Exact — Weapons
```

Unsupported server:

```text
Server: Example PRE-CU
Rules: No verified provider
Resources: Loaded
Coverage: Resource-only
```

## Initial workbench

- schematic header;
- resource/component slots;
- resource scope filter;
- deterministic assembly/experimentation controls;
- final item result panel;
- Explain;
- Compare;
- material/bottleneck summary.

Exact owned exotic component entry remains a first-class workflow.

## Event subscriptions

Reuse existing update pathways where practical.

The tab must respond to:

- schematic update;
- resource update for selected server;
- inventory changes;
- galaxy selection changes.

Do not poll or create an independent resource refresh loop.
