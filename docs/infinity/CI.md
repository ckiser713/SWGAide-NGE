# Infinity Foundation CI

## Current status

**CI_INFRASTRUCTURE_BLOCKED / BUILD_UNKNOWN**

A SHA-pinned Java 8 workflow was authored and GitHub created two runs for candidate
`578a62634598e1a7621733b11a3d9a946f4c0131`:

- push run: `35544275090`
- pull-request run: `35544275575`

Both runs concluded `failure`, but both jobs failed before GitHub recorded any
workflow step. The connector returned no step data, and the generated log blob was
already unavailable when requested. Therefore these failures are **not evidence of
a Java/Maven compile failure**.

Automatic triggers are disabled at the current checkpoint to avoid repeatedly
creating non-diagnostic failed checks. The workflow remains available for manual
execution after Codex/operator diagnosis.

## Supply-chain controls

External actions are pinned to exact commit SHAs:

- actions/checkout v4 ref observed at authoring:
  `11d5960a326750d5838078e36cf38b85af677262`
- actions/setup-java v4 ref observed at authoring:
  `cf277c60eb25467037889841efdb72551f06f6c3`

The workflow grants only:

```yaml
permissions:
  contents: read
```

Checkout disables persisted GitHub credentials.

## Intended jobs

### Baseline compile

Checks out exactly:

`7f520ee508221cce3ac3e8871919e74de9fcb0c2`

and runs:

```text
mvn -B -Dstyle.color=never test-compile
```

### Foundation compile and self-test

Checks out the selected candidate SHA, compiles, and runs:

```text
java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite
```

## Codex priority

Use terminal evidence first:

1. build audited baseline;
2. build PR head;
3. run `FoundationSelfTestSuite`;
4. only after source results are known, diagnose GitHub Actions policy/runner setup.

Do not modify source code merely to make the CI badge green.

## Evidence rule

A future green workflow is evidence only for the exact candidate SHA that ran.
It is not evidence of real Infinity golden-fixture parity, GUI behavior, RNG
parity, unsupported processors, or extractor completeness.
