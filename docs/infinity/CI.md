# Infinity Foundation CI

The foundation PR introduces a narrowly scoped GitHub Actions workflow so build evidence does not depend on narrative claims.

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

## Jobs

### Baseline compile

Checks out exactly:

`7f520ee508221cce3ac3e8871919e74de9fcb0c2`

and runs:

```text
mvn -B -Dstyle.color=never test-compile
```

This distinguishes inherited build failures from feature-branch regressions.

### Foundation compile and self-test

Checks out the candidate PR SHA, records environment/current SHA, compiles, and runs:

```text
java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite
```

## Evidence rule

A green workflow is evidence for compilation/self-test only at the exact candidate SHA that ran. It is not evidence of:

- real Infinity golden-fixture parity;
- GUI runtime behavior;
- RNG parity;
- unsupported processors;
- source extractor completeness.

Those remain separately gated.
