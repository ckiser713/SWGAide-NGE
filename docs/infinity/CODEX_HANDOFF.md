# Codex Continuation Handoff

## Controlling objective

Continue SWG Infinity Crafting Laboratory development on the existing draft PR
without weakening the evidence/authority model.

Repository:

`ckiser713/SWGAide-NGE`

Pull request:

`#1 — feat: add SWG Infinity crafting laboratory foundation`

Branch:

`feature/infinity-crafting-lab-foundation`

Base:

`master@7f520ee508221cce3ac3e8871919e74de9fcb0c2`

SWG Infinity authority:

`swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

**Resolve the current PR head with Git before doing any work. Do not trust a
copied SHA in prose if the branch has moved.**

## First terminal actions

Run and preserve exact output:

```text
git status --short
git branch --show-current
git rev-parse HEAD
java -version
mvn -version
```

### Baseline evidence

Use a separate clean worktree or checkout to test:

`7f520ee508221cce3ac3e8871919e74de9fcb0c2`

Run:

```text
mvn -B -Dstyle.color=never test-compile
```

Record the result before attributing any build issue to the feature branch.

### Candidate evidence

On the exact PR head:

```text
mvn -B -Dstyle.color=never test-compile
```

If compilation succeeds:

Unix-like:

```text
java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite
```

Windows:

```text
java -ea -cp target/test-classes;target/classes swg.infinity.FoundationSelfTestSuite
```

Capture the full output.

## CI receipt

GitHub-created runs `35544275090` and `35544275575` for candidate
`578a62634598e1a7621733b11a3d9a946f4c0131` both failed before any workflow
step was recorded. Treat GitHub-hosted CI as INFRASTRUCTURE_BLOCKED, not as
compile failure. Automatic triggers are disabled until diagnosed.

## Do not do these while fixing build issues

- do not weaken fail-closed validation;
- do not remove source quirks because they look unintuitive;
- do not change legacy `SWGTestBench`;
- do not modify `SWGAide.DAT`;
- do not change Java version;
- do not add dependencies;
- do not copy Infinity AGPL implementation source;
- do not convert UNKNOWN into PASS.

If one of those becomes necessary, stop and report the evidence.

## Existing implementation areas

Review these packages before extending:

```text
swg.infinity.contracts
swg.infinity.engine
swg.infinity.component
swg.infinity.integration
swg.infinity.processor
swg.infinity.analysis
swg.infinity.planning
swg.infinity.rules
swg.infinity.crafter
swg.infinity.loot
swg.infinity.extract
```

Key handoff documents:

```text
docs/infinity/SOURCE_OF_TRUTH.md
docs/infinity/ARCHITECTURE.md
docs/infinity/ACCEPTANCE_MATRIX.md
docs/infinity/GOLDEN_FIXTURE_PLAN.md
docs/infinity/TASK_GRAPH.yaml
docs/infinity/UNKNOWN_REGISTER.md
docs/infinity/CI.md
docs/infinity/CHECKPOINT.md
```

Machine-readable contracts:

```text
docs/infinity/ruleset.schema.json
docs/infinity/bindings.schema.json
docs/infinity/scenario.schema.json
```

## Next implementation order after compile/self-tests

### 1. Static/code review remediation

Resolve compile failures narrowly. Add tests for every remediation.

### 2. Real golden fixtures

Implement the weapon corpus in `GOLDEN_FIXTURE_PLAN.md`. Synthetic smoke tests
do not justify Exact support.

### 3. Source extractor

Build the sandboxed inheritance-resolving extractor only after normalized
contracts are compile-verified. Regex-only Lua scraping is prohibited.

### 4. Schematic bindings

Generate VERIFIED/AMBIGUOUS/MISSING bindings for SWGAide server 154. Ambiguous
bindings do not run as Exact.

### 5. Complete weapon vertical

Prove resource → crafted component → exact exotic component → final weapon.

### 6. Additive Swing workbench

Only after the engine/golden path is evidence-complete. Keep legacy Test Bench
available and unchanged.

## Required completion report

For every Codex tranche return:

- objective and scope;
- current branch/head SHA;
- changed files;
- commits/SHAs;
- commands executed;
- tests/build results with output location;
- golden fixtures/evidence;
- coverage changes;
- unresolved UNKNOWNs/blockers;
- rollback;
- smallest next action.

A clean narrative is not acceptance evidence.
