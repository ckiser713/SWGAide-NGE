# Server Rules Modules

## Provider contract

The generic simulator resolves a `ServerRulesProvider` from the selected SWGAide server.

Provider match levels:

- `EXACT_SERVER`
- `VERIFIED_FAMILY`
- `NONE`

Exact always outranks family.

Equal strongest matches fail closed.

## Infinity module

Current first provider:

`swg.crafting.simulator.server.infinity.InfinityServerRulesProvider`

Target:

- SWGAide server ID: `154`
- authority: `swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`
- match: `EXACT_SERVER`

It intentionally returns `NONE` for every other PRE-CU server.

## Future Core3/PRE-CU base

Do not create a `VERIFIED_FAMILY` PRE-CU provider merely because Infinity derives from
Core3 or because SWGAide labels multiple servers `precu`.

A family provider requires:

1. pinned canonical source/version;
2. documented scope of servers it is valid for;
3. golden fixture corpus;
4. semantic differences register;
5. explicit provider registration;
6. coverage state.

A server with a verified family module plus server-specific overrides should produce one
effective ruleset artifact whose hash is recorded in saved scenarios.

## Future modules

Possible future modules:

```text
server.core3_base
server.infinity
server.swgemu
server.restoration
server.<other>
```

Names are illustrative; do not register unsupported modules just to populate a list.

## Server module owns

- rules extraction/normalization;
- server-specific laboratory behavior;
- assembly/experimentation differences;
- component quirks;
- loot generation;
- final object processors;
- schematic bindings;
- ruleset provenance;
- semantic source diff handling;
- coverage fixtures.

## Server module does not own

- selected SWGAide galaxy;
- current resource download;
- SWGAide resource inventory;
- generic compare/explain/material planning;
- generic tab layout;
- generic scenario persistence contract.

## No provider behavior

No verified provider means no final simulation.

The UI should show:

```text
Server: <selected server>
Resources: available from SWGAide
Crafting rules: unsupported / no verified provider
Final simulation: unavailable
```

This is preferable to using an approximate or nearby ruleset.
