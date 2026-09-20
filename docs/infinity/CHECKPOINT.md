# GitHub-Only Foundation Checkpoint

## Objective

Implement as much production-oriented SWG Infinity Crafting Lab foundation as
possible on the user's SWGAide fork without terminal access or legacy behavior
changes.

## Repository truth

- repository: `ckiser713/SWGAide-NGE`
- base branch: `master`
- base SHA: `7f520ee508221cce3ac3e8871919e74de9fcb0c2`
- feature branch: `feature/infinity-crafting-lab-foundation`
- pull request: `#1`
- Infinity source authority:
  `swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

The final handoff SHA is recorded in `CODEX_HANDOFF.md` after this checkpoint
commit is created.

## Implemented greenfield layers

- immutable ruleset/provenance/coverage contracts
- fail-closed validation and exact-coverage service
- deterministic Resource Laboratory
- component identity/use/combine behavior
- custom resource-like ingredient weighting
- recursive crafted-component conversion
- generic and weapon final processors
- SWGAide resource snapshot adapter
- server-154 gate and schematic binding registry
- comparison, explainability, X/Y/Z resource substitution analysis
- material consumption and craft-capacity planning
- ruleset SHA-256 integrity/catalog/admission
- crafter deterministic pre-RNG arithmetic
- future loot data contracts (no unverified simulator)
- extractor receipt/failure contracts (no fake regex extractor)
- JSON schemas and machine-readable task graph
- dependency-free self-test suite
- SHA-pinned CI workflow retained as manual-only after infra-blocked attempt

## Deliberately not implemented

- legacy Test Bench changes
- SWGAide.DAT changes
- new Maven dependencies
- Java upgrade
- RNG outcome probabilities
- Genetic/Droid laboratory algorithms
- full Infinity Lua/template extractor
- real SWGAide↔Infinity binding corpus
- real source-backed golden weapon corpus
- Swing workbench wiring
- armor/medicine/food/structure/JTL/Jedi processors
- hypothetical loot-roll algorithm

Those items remain gated by source fixtures, terminal verification, or operator
approval.

## Verification status

- Git branch/ref writes: PASS
- PR isolation/additivity audit: PASS at earlier checkpoint; re-audit required at final head
- source review: PASS
- compile: UNKNOWN
- self-tests: UNKNOWN
- GUI smoke: UNKNOWN
- CI attempt: INFRASTRUCTURE_BLOCKED before job steps
- real Infinity parity fixtures: NOT YET ADMITTED

No narrative claim upgrades UNKNOWN.
