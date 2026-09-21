# Codex Continuation Handoff

## Controlling architecture

**Generic crafting engine + server rules modules**

Do not continue this project as an Infinity-only UI or a separate application.

Repository:

`ckiser713/SWGAide-NGE`

Pull request:

`#1`

Branch:

`feature/infinity-crafting-lab-foundation`

Base:

`master@7f520ee508221cce3ac3e8871919e74de9fcb0c2`

Infinity first-module authority:

`swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

Resolve current PR HEAD with Git before work.

## Product/UI contract

The final UI is a **new native child tab inside existing `SWGSchematicTab`**.

Tab label:

`Crafting Simulator`

Place after `The Laboratory`.

Do not create:

- standalone app;
- second top-level SWGAide application tab;
- Infinity-only primary dialog.

Extend existing selected-schematic synchronization to the new tab.

## Resource contract

Use the currently selected SWGAide server.

Native resource sources:

```text
CURRENT:
SWGResourceManager.getSpawning(galaxy)

LOADED_RECENT:
SWGResourceManager.getSet(galaxy)

INVENTORY:
SWGResController.inventory(galaxy)
```

No second resource DB/feed.

Do not enumerate SWGAide's private cache by reflection or encapsulation bypass.

## Rules module contract

Generic provider interfaces live under:

`swg.crafting.simulator.*`

Infinity first provider:

`swg.crafting.simulator.server.infinity.InfinityServerRulesProvider`

Resolution precedence:

1. EXACT_SERVER
2. VERIFIED_FAMILY
3. no provider

Family metadata (`precu`, `nge`) alone is not evidence.

Infinity provider must only claim SWGAide server 154.

A future Core3/PRE-CU base provider requires separate source/fixture acceptance before it
may return VERIFIED_FAMILY.

Unsupported servers remain usable in legacy SWGAide and may show resources in the new
tab, but final-item simulation must be unavailable/resource-only.

## Transitional package rule

Existing `swg.infinity.*` contains the first source-backed implementation.

Do not perform a blind package-wide rename before terminal compilation evidence.

After G0/G1/G2 pass, migrate truly reusable services toward
`swg.crafting.simulator.*`, while keeping Infinity-specific:

- extractor;
- source provenance;
- formulas/quirks not proven universal;
- server-specific processors;
- loot;
- bindings;
- fixtures;

inside the Infinity module.

## First terminal actions

```text
git status --short
git branch --show-current
git rev-parse HEAD
java -version
mvn -version
```

Build untouched baseline first:

```text
git checkout/worktree 7f520ee508221cce3ac3e8871919e74de9fcb0c2
mvn -B -Dstyle.color=never test-compile
```

Then exact PR head:

```text
mvn -B -Dstyle.color=never test-compile
```

If successful:

Unix-like:

```text
java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite
```

Windows:

```text
java -ea -cp target/test-classes;target/classes swg.infinity.FoundationSelfTestSuite
```

Capture all output.

## CI receipt

Prior runs `35544275090` and `35544275575` failed before any recorded workflow
step. Classify CI as INFRASTRUCTURE_BLOCKED, not compilation failure.

## Read before implementation

```text
docs/infinity/ARCHITECTURE.md
docs/infinity/SERVER_MODULES.md
docs/infinity/RESOURCE_INTEGRATION.md
docs/infinity/UI_INTEGRATION.md
docs/infinity/TASK_GRAPH.yaml
docs/infinity/SOURCE_OF_TRUTH.md
docs/infinity/ACCEPTANCE_MATRIX.md
docs/infinity/GOLDEN_FIXTURE_PLAN.md
docs/infinity/UNKNOWN_REGISTER.md
docs/infinity/CHECKPOINT.md
```

## Do not do while remediating

- do not weaken fail-closed validation;
- do not make PRE-CU == Infinity/Core3;
- do not create a duplicate resource store;
- do not modify legacy `SWGTestBench`;
- do not modify `SWGAide.DAT`;
- do not change Java version;
- do not add dependencies;
- do not copy Infinity AGPL implementation source;
- do not turn UNKNOWN into PASS.

## Post-validation order

1. compile remediation only;
2. foundation self-tests;
3. generic-core/server-module boundary completion;
4. real Infinity golden fixtures;
5. Infinity extractor;
6. server-154 bindings;
7. complete Infinity weapon vertical;
8. native Crafting Simulator tab;
9. versioned simulator persistence;
10. additional Infinity verticals / independently verified server modules.

## Required report

Every tranche must report:

- objective/scope;
- current branch/head SHA;
- changed files;
- commit SHAs;
- commands/results;
- evidence locations;
- coverage changes;
- unresolved UNKNOWNs;
- rollback;
- smallest next action.


## Immediate continuation after operator greenfield hardening

The branch has moved beyond the prior terminal-certified SHA
`1a4336438de43dd3551a44afba61b41b56ef63a2`.

Before any module expansion, perform these actions on the exact current head:

1. Resolve `git rev-parse HEAD` and verify remote == local.
2. Run `mvn -B -Dstyle.color=never test-compile`.
3. Run `swg.infinity.FoundationSelfTestSuite`.
4. Fix any compile/self-test regression narrowly; do not weaken fail-closed
   contracts.
5. Generate the packaged Infinity weapon ruleset using
   `InfinityWeaponRulesetExportMain` from the pinned
   `swginfinity/public@6b6ac372...` checkout. Admit only the four currently
   parity-certified schematic IDs:
   - `pistol_blaster_dl44`
   - `carbine_geo`
   - `pistol_blaster_scout_trooper`
   - `rifle_berserker`
6. Install the generated artifact at:
   `src/main/resources/swg/crafting/simulator/server/infinity/weapon-ruleset.json`.
7. Run `InfinityBundledRulesetSelfTest`.
8. Re-run the full foundation suite.
9. Run the facade runtime test through `runExact`; do not use the raw
   `InfinityCraftEngine` UI path.
10. Launch SWGAide and verify:
    - Infinity server 154 automatically loads provider `swg-infinity`;
    - live bindings use actual SWGAide schematic IDs;
    - an admitted weapon shows EXACT coverage;
    - a non-admitted weapon fails closed / is not presented as Exact;
    - each raw-resource slot has a user-selectable candidate combo;
    - changing X/Y/Z resources changes the craft result through the same engine;
    - inventory quantity is visible/preserved for planning;
    - unsupported servers remain resource-only;
    - Today's Alert tint still targets tab index 3;
    - `SWGTestBench.java` remains byte-identical to baseline.

Read `docs/infinity/RUNTIME_ARTIFACT.md` for the exact artifact-generation
contract.


## Final pre-review gate — live SWGAide GUI smoke

The reconciled production/test source tree at `22238c0` passed the JDK8 build and
35-test foundation suite. The later `796d9c1` commit is documentation-only.

Before changing PR #1 from draft to ready-for-review, execute the live GUI smoke
defined in:

`docs/infinity/LIVE_GUI_SMOKE.md`

This smoke is now the only required product-level gate for the accepted
Infinity weapon milestone.

Do not substitute headless registration tests for this live smoke.
Do not merge as part of the smoke.
