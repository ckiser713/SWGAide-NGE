# SWGAide Crafting Simulator — Architecture

## Decision

The product is **one native SWGAide Crafting Simulator with pluggable server rules**.

It is not an Infinity-only application and it is not a second standalone UI.

SWG Infinity is the first exact server module.

## Native UI placement

The simulator belongs inside the existing `SWGSchematicTab` JTabbedPane.

Current tabs:

1. Draft Schematics
2. The Laboratory
3. Today's Alert
4. Resource Class Use

Target tabs:

1. Draft Schematics
2. The Laboratory
3. **Crafting Simulator**
4. Today's Alert
5. Resource Class Use

The legacy Test Bench remains unchanged.

The simulator must participate in the same selected-schematic synchronization path as
Draft Schematics and The Laboratory. Selecting a schematic in one tab should select the
same schematic in Crafting Simulator where a verified binding exists.

## Selected-server contract

The selected SWGAide galaxy is the root context:

```
SWGFrame.getSelectedGalaxy()
           |
           +-----------------------+
           |                       |
           v                       v
  Native resource catalog    ServerRulesRegistry
           |                       |
           v                       v
 current/recent/inventory   exact/family rules module
           |                       |
           +-----------+-----------+
                       |
                       v
               Crafting Simulator
```

Normal-mode invariant:

```text
resource server id == selected server id == rules provider target server id
```

Cross-server experimentation, if ever added, must be an explicit research mode and may not
masquerade as a normal craft.

## Native resources only

Do not create a second resource database or Infinity-specific resource feed.

Use SWGAide's existing server-scoped sources:

- **CURRENT** — `SWGResourceManager.getSpawning(galaxy)`
- **LOADED_RECENT** — `SWGResourceManager.getSet(galaxy)`
  - current + retained recently depleted resources known to SWGAide
- **INVENTORY** — `SWGResController.inventory(galaxy)`
  - includes user-owned/old/manual inventory resources and quantities

The simulator should expose these as filters and may provide combined views.

Important: SWGAide's private resource cache also contains objects referenced by other
collections, but it does not expose a public enumeration contract for every cache entry.
Do not bypass encapsulation to scrape the private cache. If broader enumeration is needed,
add a narrow read-only resource-manager API later with tests.

## Generic core + server rules modules

Provider-neutral packages are rooted at:

`swg.crafting.simulator.*`

Server-specific code belongs under:

`swg.crafting.simulator.server.<server>.*`

Infinity is the first module:

`swg.crafting.simulator.server.infinity.*`

Existing `swg.infinity.*` code is the first source-backed implementation and a
transitional package. Do not mass-move it before terminal compilation/self-tests.
After foundation validation, reusable pieces should migrate into the generic core while
Infinity-specific rules, extractor, bindings, quirks, and processors remain in the
Infinity module.

## Server rules resolution

Provider precedence is deterministic:

1. `EXACT_SERVER`
2. `VERIFIED_FAMILY`
3. no provider

Registration order never decides authority.

Two providers claiming the same strongest match is an error.

### Exact server provider

Use when a ruleset is verified for one server ID.

Example:

`InfinityServerRulesProvider -> SWGAide server 154`

### Verified family provider

A family module such as a future Core3/PRE-CU base may be used only after that family
behavior has its own pinned authority and parity fixtures.

The SWGAide `galaxy.getType()` value (`precu`, `nge`) is metadata, not evidence.

### Unsupported server

If a server has resources/schematics in SWGAide but no verified crafting rules provider:

- Crafting Simulator tab still exists.
- Native resources can be browsed.
- Existing SWGAide Laboratory/Test Bench remain available.
- Final-item simulation is disabled or clearly marked unsupported.
- Do not silently apply Infinity/Core3 formulas.

## Custom schematics

SWGAide already supports:

- broad base family via `SWGCGalaxy.getType()`
- exact server ID
- `custom_schematics`
- server-specific schematic records
- `override_id` replacement of stock schematics

Crafting Simulator should reuse this navigation behavior.

However, `custom_schematics=true` is only a navigation/data hint. It does not select or
prove a rules module.

## Ruleset composition

Future servers may share a verified base ruleset.

Preferred model:

```
verified base artifact
      +
server override artifact
      =
effective server ruleset
      |
      v
content hash + coverage receipt
```

Avoid deep Java inheritance trees for server behavior.

The effective ruleset must be immutable, versioned, validated, and hashed after
composition.

## Dependency direction

```
existing SWGAide UI/resource/schematic state
                 |
                 v
         narrow snapshot adapters
                 |
                 v
      swg.crafting.simulator core
                 |
          +------+------+
          |             |
          v             v
 generic services   ServerRulesProvider
                        |
                 +------+------+
                 |             |
                 v             v
              Infinity      future server
                 |
                 v
           source-backed rules
```

The arithmetic engine must never import `swg.gui.*`.

Integration adapters may import legacy SWGAide classes.

## Evidence states

- EXACT
- EXACT_WITH_FORCED_OUTCOME
- SIMULATED
- PARTIAL
- UNSUPPORTED
- UNKNOWN

UNKNOWN remains UNKNOWN.

## Non-goals

- no separate application/UI;
- no second resource database;
- no assumption that all PRE-CU servers share Infinity behavior;
- no modification of legacy Test Bench math;
- no runtime dependency on checked-out server source;
- no unsupported processor presented as exact;
- no direct copying of Infinity AGPL implementation code.
