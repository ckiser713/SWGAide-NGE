# Live SWGAide GUI Smoke — Final Pre-Review Gate

## Purpose

This is the final product-level acceptance gate before PR #1 may be changed
from draft to ready-for-review.

The headless/runtime test suite is already accepted. This smoke proves the
feature behaves correctly inside a real SWGAide session with an actual selected
character/galaxy and the normal Swing lifecycle.

Do not merge as part of this procedure.

## Authority

Repository:

`ckiser713/SWGAide-NGE`

PR:

`#1`

Branch:

`feature/infinity-crafting-lab-foundation`

Baseline:

`7f520ee508221cce3ac3e8871919e74de9fcb0c2`

Pinned Infinity source:

`swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

Runtime provider:

`swg-infinity`

Infinity SWGAide server id:

`154`

## Preflight

Record:

```bash
git status --short
git branch --show-current
git rev-parse HEAD
git rev-parse origin/feature/infinity-crafting-lab-foundation

source ~/.local/bin/swgaide-env.sh
java -version
mvn -version

mvn -B -Dstyle.color=never test-compile

java -ea \
  -cp target/test-classes:target/classes \
  swg.infinity.FoundationSelfTestSuite
```

Requirements:

- local HEAD == remote HEAD;
- clean working tree before launch;
- build exits 0;
- foundation suite prints `FoundationSelfTestSuite PASS`;
- no source/test modifications are permitted solely to make the live smoke pass
  without first identifying the defect.

## Launch

Launch SWGAide using the repository's normal application entry point under the
same JDK 8 environment.

Do not launch a synthetic/headless UI harness for this gate.

Capture the exact launch command and any stdout/stderr/log path.

## Smoke A — legacy application sanity

Before testing Crafting Simulator:

1. select the normal Schematics section;
2. verify these child tabs appear in order:
   - Draft Schematics
   - The Laboratory
   - Crafting Simulator
   - Today's Alert
   - Resource Class Use
3. verify Today's Alert tint/highlight, when triggered, targets Today's Alert
   and not Crafting Simulator;
4. switch among all five tabs;
5. confirm no exceptions/errors appear in the application log;
6. verify legacy Laboratory/Test Bench still opens and behaves normally.

PASS requires no legacy regression.

## Smoke B — selected server and module bootstrap

Select a character/galaxy bound to SWG Infinity.

Verify Crafting Simulator banner displays:

- selected Infinity server;
- provider `swg-infinity`;
- pinned source commit;
- non-empty ruleset hash;
- runtime rules successfully loaded.

The installed application must load the packaged runtime artifact.

It must not require the `swginfinity/public` source checkout during normal
runtime.

Temporarily renaming/removing the external Infinity source checkout is allowed
for this smoke if convenient, provided it is restored afterward.

PASS requires the Crafting Simulator to continue loading from the bundled
artifact only.

## Smoke C — live schematic synchronization and binding

Using an admitted weapon from the accepted corpus:

- DL44;
- Geonosian Sonic Carbine;
- Scout Blaster;
- Berserker Rifle.

Select it in Draft Schematics or The Laboratory.

Verify Crafting Simulator automatically receives the same native
`SWGSchematic`.

Record:

- SWGAide numeric schematic id;
- bound Infinity schematic id;
- binding state;
- displayed coverage.

PASS requires:

- actual SWGAide id, not synthetic test ids;
- binding state runnable/verified;
- accepted corpus coverage displayed as Exact;
- no ambiguous/missing binding executed.

Also select at least one non-admitted weapon and verify it is not silently
presented as Exact.

## Smoke D — current resource scope

For one admitted weapon:

1. choose `Current Spawning`;
2. inspect every raw-resource slot;
3. verify each selector lists only resources compatible with that slot's class;
4. verify resource display includes real native stat values;
5. select a resource and run the craft.

Record at least one selected resource name/class/stat snapshot.

PASS requires nonzero real SWGAide stats where the selected resource actually
has those stats.

No all-zero placeholder path is acceptable.

## Smoke E — current + recent resource scope

Switch to `Current + Recent Set`.

Verify:

- recently depleted/retained resources become eligible where appropriate;
- a resource's origin is not falsely displayed as currently spawning;
- selecting one successfully drives the craft engine.

Record one example if available.

If the current local dataset contains no recently depleted compatible resource,
record `NOT PRESENT IN CURRENT DATASET`; do not fabricate one.

## Smoke F — inventory scope and quantity

Switch to `Inventory`.

Verify:

- inventory-only/old stock appears where class-compatible;
- stored quantity is preserved;
- Materials planning uses the captured inventory quantity rather than
  pretending availability is zero/unbounded.

Run one craft with an inventory resource.

If the account has no suitable inventory resource, add/use a legitimate local
SWGAide inventory entry through normal application behavior, or record the
environment blocker explicitly.

## Smoke G — X/Y/Z substitution

For one raw-resource slot with at least three compatible choices:

1. select resource X and capture result A;
2. select Y and capture result B;
3. select Z and capture result C.

Record:

- selected resource names;
- relevant stats;
- final functional weapon values for each result.

Use Compare.

PASS requires:

- the exact user-selected resource is used;
- results are produced by the same exact craft engine;
- meaningful differences appear when source stats should produce differences;
- Compare reflects those result differences.

If fewer than three compatible resources exist in the current scope, combine
Current + Recent / Inventory as needed. Do not inject fake resources merely to
pass the live smoke.

## Smoke H — exact components

Use a weapon with required component slots.

Populate every required component slot independently.

Verify:

- blank required component -> craft rejected;
- blank optional component -> allowed;
- insufficient use count -> rejected;
- sufficient multi-use component -> accepted;
- serial field is retained;
- exact observed property input is accepted.

Use at least one component with values in the form:

```text
mindamage=43,maxdamage=171,attackspeed=-0.8
```

The exact numbers may be replaced by real observed values available to the
operator.

PASS requires the entered component properties to affect the final craft where
the server rules say they should.

Do not reroll or simulate an owned exact component.

## Smoke I — experiment controls

Verify:

- available experiment groups populate for the selected weapon;
- deterministic assembly outcome control affects the scenario;
- assigning experiment points changes the relevant result according to the
  deterministic forced outcome path;
- Explain reflects the changed experimentation state.

Do not claim RNG probability parity; this milestone remains deterministic.

## Smoke J — Explain, Compare, Materials

For a completed craft verify:

### Explain

Shows enough information to reconcile:

- selected inputs;
- weighted score;
- material ceiling;
- percentage;
- resulting attribute/final value.

### Compare

Shows final field deltas between two captured builds.

### Materials

Shows:

- raw resource units;
- component uses;
- requested craft count;
- maximum craft count/stock constraint where quantity data exists.

PASS requires these panels to use the same scenario/result path, not separate
approximate formulas.

## Smoke K — persistence

Save a scenario if the UI exposes scenario persistence at this stage.

Restart/reload and verify the scenario retains:

- server/provider;
- ruleset commit/hash;
- schematic identity;
- resource snapshots;
- component properties/serial/use counts;
- assembly/experiment state.

If scenario Save/Load is not exposed in the native tab yet but the v2
persistence service is intentionally API-only for this milestone, record that
truthfully; do not block weapon simulation unless the current acceptance docs
declare UI Save/Load required.

Never touch `SWGAide.DAT` for simulator state.

## Smoke L — unsupported server

Switch to a SWGAide server/galaxy that has no verified server module.

Verify:

- Crafting Simulator tab still exists;
- native resource browsing remains available;
- banner reports no verified provider / RESOURCE_ONLY;
- final item simulation is refused;
- Infinity formulas are not applied.

PASS requires fail-closed behavior.

## Smoke M — legacy invariants after use

After all tests:

```bash
git status --short

git diff --stat \
  7f520ee508221cce3ac3e8871919e74de9fcb0c2..HEAD \
  -- src/main/java/swg/gui/schematics/SWGTestBench.java

git diff \
  7f520ee508221cce3ac3e8871919e74de9fcb0c2..HEAD \
  -- src/main/java/swg/gui/schematics/SWGTestBench.java
```

Requirements:

- TestBench diff empty;
- no unexpected repository files generated by the smoke;
- user/runtime data changes are limited to normal SWGAide data locations.

## Defect handling

If any smoke step fails:

1. capture exact reproduction;
2. capture logs/screenshots where useful;
3. classify as product defect vs environment/data limitation;
4. fix only product defects on the existing branch;
5. commit/push normally;
6. rerun compile + full foundation suite;
7. rerun the failed smoke step and any dependent steps.

Do not force-push.

Do not suppress an error or downgrade an Exact gate merely to complete the
smoke.

## Final evidence receipt

Return:

- branch;
- exact final HEAD;
- launch command;
- JDK/Maven versions;
- build result;
- foundation-suite count/result;
- A through M smoke result, one line each;
- actual Infinity galaxy/server id;
- actual bound SWGAide schematic IDs tested;
- X/Y/Z resource names + relevant stats + final result values;
- exact component example + resulting final-field delta;
- persistence result;
- unsupported-server result;
- log/screenshot/evidence paths;
- `SWGTestBench.java` identity result;
- any remaining UNKNOWN/environment limitations;
- final recommendation: keep draft OR ready-for-review.

Do not merge.
