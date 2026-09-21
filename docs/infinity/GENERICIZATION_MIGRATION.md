# Generic-Core Migration Plan

## Why this exists

The initial GitHub-only foundation was written under `swg.infinity.*` because Infinity was
the first researched server and compilation had not yet been established.

The accepted architecture is now:

**generic crafting engine + server rules modules**

Do not add another large vertical to `swg.infinity.*` before finishing the boundary.

## Already genericized

Provider-neutral code now exists under:

```text
swg.crafting.simulator
swg.crafting.simulator.rules
swg.crafting.simulator.resources
swg.crafting.simulator.integration
```

Infinity provider entry point:

```text
swg.crafting.simulator.server.infinity.InfinityServerRulesProvider
```

## After terminal validation

Once baseline + candidate compile and self-tests pass, classify every class currently under
`swg.infinity.*` as one of:

### Generic core

Candidates likely to migrate:

- generic craft scenario/result concepts;
- component instance/use abstractions;
- compare/explain services;
- material planning;
- generic ruleset integrity/catalog patterns;
- provider-neutral evidence states where semantics are shared.

Target area:

`swg.crafting.simulator.*`

### Infinity module

Keep/move Infinity-specific items under the Infinity server module:

- Infinity source extractor;
- Infinity ruleset/schema contracts;
- Infinity laboratory formulas until proven base-generic;
- Infinity experimentation/assembly quirks;
- Infinity component quirks;
- Infinity loot rules;
- Infinity object processors;
- Infinity schematic bindings;
- Infinity source fixture corpus.

Target area:

`swg.crafting.simulator.server.infinity.*`

## Important rule

Do not decide that a formula is "generic PRE-CU" merely because it originates in Core3 or
because it resembles classic SWG documentation.

Genericization requires evidence of common behavior across the intended server family.

## Migration mechanics

Perform package migration as a bounded refactor after compile evidence:

1. classify class;
2. move one coherent package;
3. update imports;
4. run self-tests;
5. commit;
6. repeat.

Do not combine mass package moves with behavioral changes.
