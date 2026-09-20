# SWGAide Infinity Crafting Laboratory — Architecture

## Authority

The Infinity feature is additive. Existing SWGAide behavior remains legacy authority for resource management, inventory, schematic navigation, harvesting, guards, and the existing WAR Test Bench.

Infinity crafting mechanics come from a normalized ruleset derived from:

- repository: `swginfinity/public`
- pinned initial SHA: `6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

SWGAide baseline for this fork:

- repository: `twistedatrocity/SWGAide-NGE`
- pinned baseline SHA: `7f520ee508221cce3ac3e8871919e74de9fcb0c2`

## Dependency direction

```
existing SWGAide resource/schematic state
                |
                v
        narrow adapter DTOs
                |
                v
        swg.infinity.engine
                |
                v
          CraftResult DTO
                |
                v
        additive Swing UI
```

The engine must never import `swg.gui.*`.

## Non-goals

- Do not replace the existing Test Bench.
- Do not modify SWGAide.DAT persistence in foundation work.
- Do not modernize the Java runtime in the same change.
- Do not make runtime depend on a checked-out Infinity server repository.
- Do not claim unsupported processors as exact.
- Do not copy Infinity C++/Lua implementation code into this repository.

## Evidence states

Results and coverage use explicit states:

- EXACT
- EXACT_WITH_FORCED_OUTCOME
- SIMULATED
- PARTIAL
- UNSUPPORTED
- UNKNOWN

UNKNOWN remains UNKNOWN until source-backed evidence and fixtures exist.

## Initial delivery order

1. immutable contracts and provenance
2. deterministic Resource Laboratory
3. component semantics
4. complete weapon vertical
5. additive Infinity Lab UI
6. separate scenario/component persistence
7. source extractor
8. additional profession processors
