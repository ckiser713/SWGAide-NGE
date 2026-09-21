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
- compile: UNKNOWN
- self-tests: UNKNOWN
- GUI smoke: UNKNOWN
- CI: INFRASTRUCTURE_BLOCKED before job steps
- real Infinity golden parity: NOT YET ADMITTED
- generic PRE-CU family rules: UNKNOWN / deliberately not registered

No narrative claim upgrades UNKNOWN.
