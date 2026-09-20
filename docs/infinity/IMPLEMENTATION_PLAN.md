# Implementation Plan

## Foundation objective

Create a production-oriented, compile-isolated foundation that Codex can continue with terminal-based build/test execution.

### Foundation scope

- immutable normalized contracts
- provenance and coverage state
- deterministic Resource Laboratory core
- component/combine abstractions
- scenario/result DTOs
- SWGAide adapter boundary
- Infinity-server feature gate
- test vectors that require no third-party test framework
- implementation/coder handoff documentation

### Explicitly out of scope until terminal verification

- changing Maven dependencies
- changing Java target
- changing SWGAide.DAT
- replacing legacy Test Bench behavior
- claiming build/test PASS
- wiring unverified UI actions into legacy screens
- RNG parity claims
- genetic/droid laboratory support
- full Lua extractor

## Acceptance gates

### G0 — repository truth
Branch starts at `7f520ee508221cce3ac3e8871919e74de9fcb0c2`.

### G1 — contracts
Ruleset structures are immutable, source-provenanced, and fail closed.

### G2 — resource laboratory
Pure-Java deterministic formulas match source-derived golden vectors.

### G3 — components
All six combine types have explicit handling and fixtures.

### G4 — first weapon vertical
A complete nested component + exotic component + weapon result is evidence-complete.

### G5 — UI
Additive Infinity Lab can be opened for Infinity server 154 without changing legacy Test Bench.

## Rollback

The branch/PR is fully additive in the foundation stage. Closing the PR restores the exact fork baseline. Runtime feature wiring must later be feature-gated so the legacy application remains usable if Infinity Lab is disabled.
