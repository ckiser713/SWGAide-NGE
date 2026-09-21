# GitHub-Only Foundation Checkpoint

## Architecture decision

Accepted after the initial Infinity foundation:

**Generic crafting engine + server rules modules**

The UI will be a native **Crafting Simulator** child tab inside `SWGSchematicTab`, not a
standalone UI.

## Repository truth

- repository: `ckiser713/SWGAide-NGE`
- base branch: `master`
- base SHA: `7f520ee508221cce3ac3e8871919e74de9fcb0c2`
- feature branch: `feature/infinity-crafting-lab-foundation`
- pull request: `#1`
- Infinity first-module source:
  `swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

Resolve current head from Git before execution.

## Generic architecture added

- provider-neutral `CraftingRuleset`;
- immutable `ServerIdentity`;
- `ServerRulesProvider`;
- deterministic `ServerRulesRegistry`;
- exact > verified-family > none precedence;
- exact Infinity server provider for ID 154;
- native SWGAide selected-server resource catalog;
- immutable server simulation context;
- explicit RESOURCE_ONLY mode when no provider exists;
- no separate resource DB.

## Native resource coverage

Selected server resources come from:

- `SWGResourceManager.getSpawning(galaxy)`
- `SWGResourceManager.getSet(galaxy)`
- `SWGResController.inventory(galaxy)`

Inventory preserves old/manual resource quantities.

Private cache scraping is prohibited.

## Transitional state

Existing source-backed arithmetic remains largely under `swg.infinity.*`.

This is intentional until terminal compile/self-tests exist.

After validation, reusable pieces migrate toward `swg.crafting.simulator.*`; Infinity
source-specific rules/quirks/extractor/bindings/processors stay in the Infinity module.

See `GENERICIZATION_MIGRATION.md`.

## Verification status

- Git branch/ref writes: PASS
- architecture/source research: PASS
- native resource API research: PASS
- PR isolation/additivity: requires final re-audit after latest commits
- compile: **PASS** (terminal evidence 2026-09-20)
- self-tests: **PASS** (terminal evidence 2026-09-20, expanded to 17 with T2)
- generic core boundary: **PASS** (T2, 2026-09-20)
- recursive crafting cycle detection: **PASS** (T2, 2026-09-20)
- GUI smoke: UNKNOWN
- CI: INFRASTRUCTURE_BLOCKED before job steps
- real Infinity golden parity: NOT YET ADMITTED
- generic PRE-CU family rules: UNKNOWN / deliberately not registered

No narrative claim upgrades UNKNOWN.

## Terminal evidence (G0 + G1 + G2)

### Toolchain
- JDK: Temurin 1.8.0_432 at `~/.local/jdk/jdk8u432-b06` (portable, no system change).
- Maven: Apache Maven 3.9.9 at `~/.local/maven/apache-maven-3.9.9` (portable, no system change).
- Env-source script: `~/.local/bin/swgaide-env.sh` exports `JAVA_HOME` and `PATH`.

### G0 baseline reproduction (`7f520ee508221cce3ac3e8871919e74de9fcb0c2`)
- Baseline worktree: `/home/thenexussidekick/swgaide-baseline` (detached HEAD at `7f520ee`).
- `git rev-parse HEAD` → `7f520ee508221cce3ac3e8871919e74de9fcb0c2`.
- `mvn -B -Dstyle.color=never test-compile` → exit 0, `BUILD SUCCESS`.
- 992 source files compiled to `target/classes`.
- Working tree clean except `target/maven-status/` (normal Maven output).

### Dependency resolution (jide-oss:3.7.7)
- `com.jidesoft:jide-oss:3.7.7` is **not available in any public Maven repository** (Jide
  took 3.7.x private when the project went commercial; latest on Maven Central is 3.6.18).
- All mirrors attempted (Maven Central, Aliyun, Google Maven, Apache, JBoss, Atlassian,
  Spring, Cloudera, Sonatype OSSRH, JCenter, jitpack, jpackage) returned 404.
- **Resolution**: an archived exact-version JAR was located at
  `https://sites.imagej.net/Neuroanatomy/jars/jide-oss-3.7.7.jar-20190801071653`
  (ImageJ Neuroanatomy update site, dated 2019-08-01). It is described as an
  archived 3.7.7 binary, **not vendor-checksum-verified**.
  - Download timestamp: 2026-09-21T03:59:13Z.
  - File type: Java archive data (JAR).
  - ZIP integrity: `unzip -t` reports no errors.
  - Byte size: 2,083,660 bytes.
  - SHA-256: `554abc9010c6edacde2b3361cc934b96b5c13948726c54d5c34a94e8c8a1625b`.
  - SHA-1: `7ed476e06b8efb1207afa6e6dcd1ea475e273d5c`.
  - MD5: `7ef555327a6806ac0634f6025336a797`.
- **No upstream `jide-oss-3.7.7.pom` was located.** A minimal local artifact descriptor
  was generated via `mvn install:install-file -DgeneratePom=true` and installed at
  `~/.m2/repository/com/jidesoft/jide-oss/3.7.7/jide-oss-3.7.7.pom`. This generated
  POM is **not** the historical Jide-published POM; the SWGAide project pom declares
  `com.jidesoft:jide-oss:3.7.7` with no Jide-specific transitive configuration, so the
  generated descriptor is sufficient for resolution.
- The repository `pom.xml` was **not modified**.

### G1 + G2 feature-branch build and self-test
- Feature branch: `feature/infinity-crafting-lab-foundation` (local tracking of
  `origin/feature/infinity-crafting-lab-foundation`).
- HEAD at evidence: `75d221a9dea13384a835dcdc9819d7113b525e0d`.
- `mvn -B -Dstyle.color=never test-compile` → exit 0, `BUILD SUCCESS`.
  - 1076 main source files compiled to `target/classes`.
  - 16 test source files compiled to `target/test-classes`.
- `java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite`
  → exit 0, all 15 self-tests PASS, aggregator prints `FoundationSelfTestSuite PASS`:
  ```
  ServerRulesRegistrySelfTest PASS
  ServerSimulationContextSelfTest PASS
  ResourceLaboratorySelfTest PASS
  ComponentCombinerSelfTest PASS
  CraftedComponentFactorySelfTest PASS
  SchematicBindingRegistrySelfTest PASS
  InfinityCraftEngineSelfTest PASS
  InfinityCraftServiceSelfTest PASS
  CraftComparatorSelfTest PASS
  CraftExplainerSelfTest PASS
  MaterialPlannerSelfTest PASS
  RulesetIntegritySelfTest PASS
  RulesetCatalogSelfTest PASS
  CraftingSkillMathSelfTest PASS
  ExtractionReportSelfTest PASS
  FoundationSelfTestSuite PASS
  ```

### T2 generic core boundary + cycle detection (2026-09-20)

Phases 1–7 of T2 moved 22 files from `swg.infinity.*` to
`swg.crafting.simulator.*`:

- Phase 1: contracts → `swg.crafting.simulator.contracts.*` (already pushed)
- Phase 2: scenarios → `swg.crafting.simulator.scenario.*`
- Phase 3: component DTOs → `swg.crafting.simulator.components.*`
- Phase 4: analysis services → `swg.crafting.simulator.compare.*` and
  `swg.crafting.simulator.explain.*`
- Phase 5: planning → `swg.crafting.simulator.planning.*`
- Phase 6: crafter math → `swg.crafting.simulator.crafter.CraftingSkillMath`
- Phase 7: ruleset → originally moved to `swg.crafting.simulator.ruleset.*`,
  then reverted because the catalog/admission/validator are structurally
  Infinity-specific (keyed by Infinity source commit, validated as
  `InfinityRuleset`). Now in `swg.infinity.ruleset.*` and
  `swg.infinity.contracts.RulesetValidator`.

Two new guards close out T2:

- `RecursiveCraftCycleDetector` + `RecursiveCraftCycleException` in
  `swg.crafting.simulator.components.*`. Generic DAG walk utility that
  raises the typed exception on any in-stack revisit. Default depth bound
  64 is defense in depth; the cycle check is the authoritative gate.
- `GenericCoreBoundarySelfTest` in `swg.crafting.simulator.*`. Asserts
  the generic core does not import Infinity behaviour classes. Pure-DTO
  imports from `swg.infinity.contracts.*` remain permitted because
  Infinity-specific DTOs are still housed in the Infinity module per
  `GENERICIZATION_MIGRATION.md`.

T2 commits pushed to `origin/feature/infinity-crafting-lab-foundation`:

- `b77c6a7` refactor(simulator): move component DTOs to swg.crafting.simulator.components
- `61a6ecd` refactor(simulator): move engine scenarios to swg.crafting.simulator.scenario
- `a9dda03` refactor(simulator): move analysis services to compare/ and explain/
- `57220aa` refactor(simulator): move planning, crafter, and ruleset to swg.crafting.simulator
- `04b5134` feat(simulator): add cycle detection and boundary self-test

Foundation suite now runs 17 self-tests, all PASS. `mvn test-compile` exit 0.

### T3 source-backed seed fixture corpus (2026-09-20)

- `SeedFixture` and `SeedFixtureParser` added in `swg.infinity.fixtures.*`.
  Hand-rolled JSON reader, fail-closed, requires `provenance: source-pinned`.
- Four source-pinned weapon seeds under
  `src/test/resources/swg/infinity/fixtures/seeds/`:
  `pistol_blaster_dl44`, `pistol_blaster_scout_trooper`, `carbine_geo`,
  `rifle_berserker`. Each captures the canonical slot layout, resource
  requirements, and target template from the pinned Infinity source.
- `SeedFixtureParseSelfTest` asserts parser round-trip + provenance
  enforcement.
- Foundation suite now runs 18 self-tests, all PASS.

### T7 functional native Crafting Simulator tab (2026-09-20)

- `SWGCraftingSimulatorTab` lives in
  `swg.gui.schematics.craftsim.*` (new package). Tab title
  `"Crafting Simulator"`, mnemonic `VK_C`.
- `SimEngineFacade` wraps the Infinity engine for run / render / compare
  / explain. `CraftedComponentFactory` powers the recursive component
  round-trip.
- Banner renders selected server, rules provider, ruleset SHA, coverage
  state. Schematic selector listens to `schematicSelect(...)` from
  `SWGSchematicTab` and falls back to a manual selector.
- Native resources pulled from `SWGResourceManager.getSet(galaxy)`,
  `getSpawning(galaxy)`, and `SWGResController.inventory(galaxy)` with
  a scope selector.
- Run, Compare, Explain, and Materials planning are wired to the
  engine. Exotic component input and recursive crafted components are
  supported. RESOURCE_ONLY mode renders the banner but disables the
  final simulation on unsupported providers.
- Legacy `SWGSchematicTab.java` edits remain minimal: one new field,
  one new `add(...)` call in `make()`, one new branch in
  `schematicSelect(...)`. Existing tabs, mnemonic indices, behaviour,
  and keyboard shortcuts are preserved.
- `SWGCraftingSimulatorTabHeadlessSmoke` asserts tab registration,
  mnemonic, and `SWGTestBench.java` byte-identity to baseline.
- Foundation suite now runs 19 self-tests, all PASS.

### T8 versioned simulator persistence (2026-09-20)

- `CraftsimScenario`, `CraftsimMigration`, `CraftsimScenarioStore` live
  in `swg.crafting.simulator.persistence.*`.
- Storage: `<baseDir>/scenarios/<id>.craftsim.json`. New, isolated,
  versioned. Does not modify `SWGAide.DAT`. No new third-party
  dependencies.
- Scenario pins `rulesProviderId`, `rulesetCommit`, and `rulesetHash`;
  hash mismatch on load rejects deserialization.
- Forward-only migration registry keyed by `schemaVersion`.
- `CraftsimPersistenceSelfTest` exercises round-trip, hash rejection,
  migration, and confirms `SWGAide.DAT` invariant.
- Foundation suite now runs 20 self-tests, all PASS.

### T4 sandboxed Infinity extractor (2026-09-20)

- `LuaTemplateStubLoader` is a hand-rolled, token-aware Lua reader in
  `swg.infinity.extract.*`. Supports table literals (records and
  arrays), double-quoted strings, numbers, identifiers, line comments
  (`--`) and block comments (`--[[ ... ]]`). Array values are emitted as
  `ArrayList<Object>`, records as `LinkedHashMap<String, Object>`.
  `parseFirstTable` walks past leading identifier + `=` + method-call
  prefix (`object_X = parent:new { ... }`) to find the table literal.
  Fail-closed on unhandled constructs.
- `Extractor` reads `MMOCoreORB/bin/scripts/object/draft_schematic/weapon/*.lua`
  from a pinned `swginfinity/public` checkout. Each parsed schematic
  becomes a `SchematicDefinition` with provenance (`repository`,
  `commit`, relative path). Slot kinds are mapped from
  `ingredientSlotType` integer codes (0=RESOURCE, 1=IDENTICAL,
  2=MIXED, 3=OPTIONAL_IDENTICAL, 4=OPTIONAL_MIXED).
- `InfinityRuleset` ruleset hash is the SHA-256 of schematic ids +
  draft templates + target templates. Deterministic: only
  `LinkedHashMap` + `ArrayList` ordering, no time/locale input.
- Coverage records report `SIMULATED` for slot data and `UNSUPPORTED`
  for the experimental groups (those live in compiled `.iff`, not
  source `.lua`). One quest schematic (`rifle_quest_rebel_longrifle`)
  is excluded by the BLOCKER gate because it omits `customObjectName`
  and `targetTemplate`; the remaining 75 weapon drafts are admitted.
- `ExtractorSelfTest` runs against
  `/home/thenexussidekick/swgaide-deps/swginfinity-public`. Asserts
  ≥1 schematic extracted, manifest fields match the pinned source,
  ruleset hash is deterministic across two extractions, DL44 has
  exactly 6 slots, DL44 coverage is present, and the Lua reader fails
  closed on the synthetic `bad_construct.lua` fixture.
- Foundation suite now runs 21 self-tests, all PASS.

T4 commits:

- `a5171b4` feat(infinity): add seed fixture framework with 4 source-pinned weapon seeds (T3)
- `a6efb86` feat(gui): add native Crafting Simulator tab inside SWGSchematicTab (T7)
- `8893ef9` feat(simulator): add isolated versioned craftsim scenario persistence (T8)
- (this commit) feat(extract): add sandboxed Infinity source extractor (T4)

### T5 multi-signal structural schematic bindings (2026-09-20)

- `BindingFingerprint` lives in `swg.infinity.integration.*`. It computes
  a SHA-256 hex over canonicalized structural signals: resource slot
  count, sorted resource slot kinds, sorted experiment-group titles,
  and normalized target template. Two structurally equivalent
  schematics produce the same hash regardless of input ordering.
  `alignsWith` returns true iff every primary signal matches; partial
  alignment is rejected.
- `SchematicCatalogEntry` is a provider-neutral SWGAide-side record
  (id, name, slot count, slot kinds, group titles, target-template
  hint) that decouples the binding pipeline from `SWGSchematic`.
- `BindingBuilder.build(catalog, ruleset)` produces a `SchematicBinding`
  per catalog entry: VERIFIED on exactly one fingerprint match,
  AMBIGUOUS on multiple (evidence lists every candidate id), MISSING
  on none.
- `BindingOverride` requires non-empty `evidence` and
  `operatorSignature`. Validation rejects otherwise.
- `SchematicBindingRegistry` gained a `snapshot()` accessor.
- `BindingWriter.write(registry, commit, out)` emits JSON conforming
  to `docs/infinity/bindings.schema.json` with `serverId = 154`,
  `schemaVersion = 1`, pinned `rulesetCommit`, and `admitted`
  envelope flag.
- `BindingClassificationSelfTest` exercises VERIFIED, AMBIGUOUS,
  MISSING, MANUAL_OVERRIDE, the never-runnable AMBIGUOUS invariant,
  and the writer's required-field output.
- `BindingsSeedWriterSelfTest` runs the Extractor + 4 source-pinned
  seeds + BindingBuilder end-to-end and writes
  `target/bindings.server154.json`. All 4 seed weapons are admitted
  as VERIFIED; slot counts cross-check between seed and extractor.
- Foundation suite now runs 23 self-tests, all PASS.

T5 commits:

- (this commit) feat(integration): add multi-signal structural binding classifier
