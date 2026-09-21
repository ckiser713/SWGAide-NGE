# Native Resource Integration

## Goal

Crafting Simulator uses resource data SWGAide already loaded for the selected server.

No duplicate resource feed or resource database is introduced.

## Public source APIs

### Current spawns

`SWGResourceManager.getSpawning(galaxy)`

This is non-blocking and may trigger a background refresh.

### Loaded current + recent/depleted

`SWGResourceManager.getSet(galaxy)`

This represents the server-scoped resource set retained by SWGAide.

### Inventory

`SWGResController.inventory(galaxy)`

Inventory wrappers preserve amount/assignee data and can contain older manually entered
or stockpiled resources that are no longer in the current/recent set.

## Simulator scopes

Recommended UI filters:

- Current
- Current + Recent
- Inventory
- Current + Inventory
- All Available To Simulator

"All Available To Simulator" is a view over native sources. It is not a new database.

When combining sources:

- deduplicate resource identity for display;
- preserve inventory quantities separately;
- preserve source/origin badges;
- do not mark an inventory-only old resource as current;
- preserve the selected server ID on every snapshot.

## Historical search

SWGAide does not currently expose a complete enumerable public history for every cached
resource ever seen.

Do not reach into private caches.

If broader historical search is added later, implement it behind a separate provider with
explicit provenance/completeness labels.

## Updates

Crafting Simulator should refresh or invalidate its view when:

- selected galaxy changes;
- `ResourceUpdate` applies to selected galaxy;
- inventory changes;
- schematic cache changes.

The simulator should snapshot mutable legacy objects into its own immutable calculation
DTOs before arithmetic.
