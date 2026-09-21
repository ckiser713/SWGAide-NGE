# Validation

No build or test result in this branch is considered PASS until reproduced from a terminal or CI runner.

The fork baseline currently has no committed test framework and no GitHub Actions workflow. Foundation tests therefore use plain Java `main` entry points and require no new dependency.

## Required Codex baseline

Record:

```text
java -version
mvn -version
git status --short
git rev-parse HEAD
```

Then build the untouched baseline SHA:

`7f520ee508221cce3ac3e8871919e74de9fcb0c2`

Record whether it compiles before attributing any failure to Infinity work.

## Feature branch compile

From branch `feature/infinity-crafting-lab-foundation`:

```text
mvn test-compile
```

Do not change dependencies or Java target merely to make this command pass. Diagnose first.

## Foundation self-test

After successful `mvn test-compile`, run the compiled test suite with a classpath appropriate to the local platform.

Unix-like example:

```text
java -ea -cp target/test-classes:target/classes swg.infinity.FoundationSelfTestSuite
```

Windows example:

```text
java -ea -cp target/test-classes;target/classes swg.infinity.FoundationSelfTestSuite
```

Expected final line:

```text
FoundationSelfTestSuite PASS
```

This expected text is not evidence by itself. Capture the actual command output.

## Current evidence status

At handoff creation:

- source review: performed
- Git commits: reproducible by SHA
- compilation: UNKNOWN
- self-test execution: UNKNOWN
- GUI smoke test: UNKNOWN

Codex must preserve UNKNOWN until commands actually run.
