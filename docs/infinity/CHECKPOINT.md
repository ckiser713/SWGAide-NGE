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
